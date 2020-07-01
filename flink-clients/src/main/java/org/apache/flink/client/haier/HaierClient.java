/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.client.haier;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Preconditions;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.util.concurrent.CompletableFuture;

/**
 * Communicate with HAIER scheduler over REST API to enrich JobGraph.
 */
public class HaierClient {

	private static final Logger LOG = LoggerFactory
			.getLogger(HaierClient.class);

	private final String haierUrl;

	/**
	 * Create a HAIER client using settings from the Flink configuration.
     *
	 * @param config Flink configuration object.
	 */
	public static HaierClient fromConfiguration(Configuration config) {
		final String haierUrl = config.getString(HaierOptions.HAIER_REST_URL);
		return new HaierClient(haierUrl);
	}

	public HaierClient(String haierUrl) {
		this.haierUrl = haierUrl;
	}

	/**
	 * Try passing the JobGraph to HAIER to enrich it with scheduling information.
	 *
	 * @param jobGraphFileFuture Serialized JobGraph.
	 * @return Enriched JobGraph, or original JobGraph if HAIER URL is not configured.
	 */
	public CompletableFuture<java.nio.file.Path> enrichJob(@Nonnull CompletableFuture<java.nio.file.Path> jobGraphFileFuture) {
		try {
			Preconditions.checkNotNull(this.haierUrl);
			return enrichJobInternal(jobGraphFileFuture);
		} catch (NullPointerException e) {
			LOG.warn("HAIER URL is not configured. Returning unprocessed jobGraph.");
		}
		return jobGraphFileFuture;
	}

	private CompletableFuture<java.nio.file.Path> enrichJobInternal(@Nonnull CompletableFuture<java.nio.file.Path> jobGraphFileFuture) {
		final CompletableFuture<java.nio.file.Path> haierFuture = jobGraphFileFuture.thenApply(jobGraphFile -> {
			LOG.info("vvvvvvvvvvvvvvvvvvv   HAIER   vvvvvvvvvvvvvvvvvvvvvvvv\n\n\n\n\n");

			final CloseableHttpClient haierClient = HttpClients.createDefault();
			final HttpEntity haierReqEntity = MultipartEntityBuilder.create().addBinaryBody("file", jobGraphFile.toFile(), ContentType.APPLICATION_OCTET_STREAM, jobGraphFile.getFileName().toString()).build();
			final HttpPost haierReq = new HttpPost(haierUrl);
			haierReq.setEntity(haierReqEntity);

			try {
				LOG.info("Executing request:  " + haierReq.getRequestLine());
				final CloseableHttpResponse haierRes = haierClient.execute(haierReq);
				try {
					LOG.info("Response:  " + haierRes.getStatusLine());
					final HttpEntity haierResEntity = haierRes.getEntity();
					if (haierResEntity != null) {
						LOG.info("Response's Content-Length:  " + haierResEntity.getContentLength());
					}
					EntityUtils.consume(haierResEntity);
				} finally {
					haierRes.close();
				}
			} catch (Exception e) {
				throw new HaierException(e, jobGraphFile);
			}

			LOG.info("^^^^^^^^^^^^^^^^^^^   HAIER   ^^^^^^^^^^^^^^^^^^^^^^^^\n");
			return jobGraphFile;
		}).exceptionally(e -> {
			LOG.warn("[HAIER] OOPS...: " + e.getMessage());
			LOG.warn("^^^^^^^^^^^^^^^^^^^   HAIER   ^^^^^^^^^^^^^^^^^^^^^^^^\n");
			return ((HaierException) e).getJobGraphFile();
		});
		/*.handle((ret, e) -> {
			if (ret != null) {
				return ret;
			}
			log.warn("[HAIER] OOPS...: " + e.getMessage());
			log.warn("^^^^^^^^^^^^^^^^^^^   JOBGRAPH   ^^^^^^^^^^^^^^^^^^^^^^^^\n");
			return ((HaierException) e).getJobGraphFile();
		});*/
		return haierFuture;
	}

}
