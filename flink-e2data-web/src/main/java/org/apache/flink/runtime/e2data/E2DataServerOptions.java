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

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.SecurityOptions;

import static org.apache.flink.configuration.ConfigOptions.key;

/**
 * The set of configuration options relating to the E2DataServer.
 */
@PublicEvolving
public class E2DataServerOptions {

	/**
	 * The local directory used by the E2DataServer web-frontend.
	 */
	public static final ConfigOption<String> E2DATA_SERVER_WEB_DIR =
		key("e2dataserver.web.tmpdir")
			.noDefaultValue()
			.withDescription("This configuration parameter allows defining the Flink web directory to be used by the" +
				" E2Data server web interface. The web interface will copy its static files into the directory.");

	/**
	 * The address under which the E2Data web-frontend is accessible.
	 */
	public static final ConfigOption<String> E2DATA_SERVER_WEB_ADDRESS =
		key("e2dataserver.web.address")
			.noDefaultValue()
			.withDescription("Address of the E2DATAServer's web interface.");

	/**
	 * The port under which the E2DataServer web-frontend is accessible.
	 */
	public static final ConfigOption<Integer> E2DATA_SERVER_WEB_PORT =
		key("e2dataserver.web.port")
			.defaultValue(8082)
			.withDescription("Port of the E2DataServer's web interface.");

	/**
	 * The refresh interval for the E2DataServer web-frontend in milliseconds.
	 */
	public static final ConfigOption<Long> E2DATA_SERVER_WEB_REFRESH_INTERVAL =
		key("e2dataserver.web.refresh-interval")
			.defaultValue(10000L)
			.withDescription("The refresh interval for the E2DataServer web-frontend in milliseconds.");

	/**
	 * Enables/Disables SSL support for the E2DataServer web-frontend. Only relevant if
	 * {@link SecurityOptions#SSL_REST_ENABLED} is enabled.
	 */
	public static final ConfigOption<Boolean> E2DATA_SERVER_WEB_SSL_ENABLED =
		key("e2dataserver.web.ssl.enabled")
			.defaultValue(false)
			.withDescription("Enable HTTPs access to the E2DataServer web frontend. This is applicable only when the" +
				" global SSL flag security.ssl.enabled is set to true.");

	private E2DataServerOptions() {
	}
}
