/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.e2data;

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.plugin.PluginUtils;
import org.apache.flink.runtime.e2data.handler.StartFlinkHandler;
import org.apache.flink.runtime.e2data.utils.E2DataFrontendBootstrap;
import org.apache.flink.runtime.e2data.utils.E2DataServerStaticFileServerHandler;
import org.apache.flink.runtime.io.network.netty.SSLHandlerFactory;
import org.apache.flink.runtime.net.SSLUtils;
import org.apache.flink.runtime.rest.handler.router.Router;
import org.apache.flink.runtime.rest.messages.DashboardConfiguration;
import org.apache.flink.runtime.security.SecurityConfiguration;
import org.apache.flink.runtime.security.SecurityUtils;
import org.apache.flink.runtime.util.EnvironmentInformation;
import org.apache.flink.util.ExceptionUtils;
import org.apache.flink.util.FileUtils;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.ShutdownHookUtil;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The E2DataServer provides a WebInterface and REST API for E2Data related tasks.
 *
 *
 * <p>All configuration options are defined in{@link E2DataServerOptions}.
 *
 * <p>The REST API is limited to
 * <ul>
 *     <li>/config</li>
 *     <li>/e2data/start/</li>
 *     <li>/e2data/stop/</li>
 * </ul>
 * and relies on static files that are served by the {@link E2DataServerStaticFileServerHandler}.
 */
public class E2DataServer {

	private static final Logger LOG = LoggerFactory.getLogger(E2DataServer.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final Configuration config;

	private final String webAddress;
	private final int webPort;
	private final long webRefreshIntervalMillis;
	private final File webDir;

	@Nullable
	private final SSLHandlerFactory serverSSLFactory;
	private E2DataFrontendBootstrap netty;

	private final Object startupShutdownLock = new Object();
	private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
	private final Thread shutdownHook;

	public static void main(String[] args) throws Exception {
		EnvironmentInformation.logEnvironmentInfo(LOG, "E2DataServer", args);

		ParameterTool pt = ParameterTool.fromArgs(args);
		String configDir = pt.getRequired("configDir");

		LOG.info("Loading configuration from {}", configDir);
		final Configuration flinkConfig = GlobalConfiguration.loadConfiguration(configDir);

		FileSystem.initialize(flinkConfig, PluginUtils.createPluginManagerFromRootFolder(flinkConfig));

		// run the e2data server
		SecurityUtils.install(new SecurityConfiguration(flinkConfig));

		try {
			SecurityUtils.getInstalledContext().runSecured(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					E2DataServer hs = new E2DataServer(flinkConfig);
					hs.run();
					return 0;
				}
			});
			System.exit(0);
		} catch (Throwable t) {
			final Throwable strippedThrowable = ExceptionUtils.stripException(t, UndeclaredThrowableException.class);
			LOG.error("Failed to run E2DataServer.", strippedThrowable);
			strippedThrowable.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates E2DataServer instance.
	 * @param config configuration
	 * @throws IOException When creation of SSL factory failed
	 */
	public E2DataServer(
			Configuration config
	) throws IOException {
		Preconditions.checkNotNull(config);

		this.config = config;
		if (E2DataServerUtils.isSSLEnabled(config)) {
			LOG.info("Enabling SSL for the E2Data server.");
			try {
				this.serverSSLFactory = SSLUtils.createRestServerSSLEngineFactory(config);
			} catch (Exception e) {
				throw new IOException("Failed to initialize SSLContext for the E2Data server.", e);
			}
		} else {
			this.serverSSLFactory = null;
		}

		webAddress = config.getString(E2DataServerOptions.E2DATA_SERVER_WEB_ADDRESS);
		webPort = config.getInteger(E2DataServerOptions.E2DATA_SERVER_WEB_PORT);
		webRefreshIntervalMillis = config.getLong(E2DataServerOptions.E2DATA_SERVER_WEB_REFRESH_INTERVAL);

		String webDirectory = config.getString(E2DataServerOptions.E2DATA_SERVER_WEB_DIR);
		if (webDirectory == null) {
			webDirectory = System.getProperty("java.io.tmpdir") + File.separator + "flink-web-e2data-" + UUID.randomUUID();
		}
		webDir = new File(webDirectory);

		this.shutdownHook = ShutdownHookUtil.addShutdownHook(
			E2DataServer.this::stop,
			E2DataServer.class.getSimpleName(),
			LOG);
	}

	@VisibleForTesting
	int getWebPort() {
		return netty.getServerPort();
	}

	public void run() {
		try {
			start();
			new CountDownLatch(1).await();
		} catch (Exception e) {
			LOG.error("Failure while running E2DataServer.", e);
		} finally {
			stop();
		}
	}

	// ------------------------------------------------------------------------
	// Life-cycle
	// ------------------------------------------------------------------------

	void start() throws IOException, InterruptedException {
		synchronized (startupShutdownLock) {
			LOG.info("Starting E2Data server.");

			Files.createDirectories(webDir.toPath());
			LOG.info("Using directory {} as local cache.", webDir);

			Router router = new Router();
			router.addGet("/e2data/:command", new StartFlinkHandler(router));
			router.addGet("/:*", new E2DataServerStaticFileServerHandler(webDir));

			if (!webDir.exists() && !webDir.mkdirs()) {
				throw new IOException("Failed to create local directory " + webDir.getAbsoluteFile() + ".");
			}

			createDashboardConfigFile();

			netty = new E2DataFrontendBootstrap(router, LOG, serverSSLFactory, webAddress, webPort, config);
		}
	}

	void stop() {
		if (shutdownRequested.compareAndSet(false, true)) {
			synchronized (startupShutdownLock) {
				LOG.info("Stopping E2Data server.");

				try {
					netty.shutdown();
				} catch (Throwable t) {
					LOG.warn("Error while shutting down E2DataFrontendBootstrap.", t);
				}

				try {
					LOG.info("Removing E2Data web dashboard root cache directory {}", webDir);
					FileUtils.deleteDirectory(webDir);
				} catch (Throwable t) {
					LOG.warn("Error while deleting E2Data web root directory {}", webDir, t);
				}

				LOG.info("Stopped E2Data server.");

				// Remove shutdown hook to prevent resource leaks
				ShutdownHookUtil.removeShutdownHook(shutdownHook, getClass().getSimpleName(), LOG);
			}
		}
	}

	// ------------------------------------------------------------------------
	// File generation
	// ------------------------------------------------------------------------

	static FileWriter createOrGetFile(File folder, String name) throws IOException {
		File file = new File(folder, name + ".json");
		if (!file.exists()) {
			Files.createFile(file.toPath());
		}
		FileWriter fr = new FileWriter(file);
		return fr;
	}

	private void createDashboardConfigFile() throws IOException {
		try (FileWriter fw = createOrGetFile(webDir, "config")) {
			fw.write(createConfigJson(DashboardConfiguration.from(webRefreshIntervalMillis, ZonedDateTime.now(), false)));
			fw.flush();
		} catch (IOException ioe) {
			LOG.error("Failed to write config file.");
			throw ioe;
		}
	}

	private static String createConfigJson(DashboardConfiguration dashboardConfiguration) throws IOException {
		return OBJECT_MAPPER.writeValueAsString(dashboardConfiguration);
	}

	/**
	 * Container for the {@link Path} and {@link FileSystem} of a refresh directory.
	 */
	static class RefreshLocation {
		private final Path path;
		private final FileSystem fs;

		private RefreshLocation(Path path, FileSystem fs) {
			this.path = path;
			this.fs = fs;
		}

		public Path getPath() {
			return path;
		}

		public FileSystem getFs() {
			return fs;
		}
	}
}
