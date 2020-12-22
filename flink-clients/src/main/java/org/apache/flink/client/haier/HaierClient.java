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

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.ProcessingUnitType;
import org.apache.flink.api.common.operators.ResourceSpec;
import org.apache.flink.api.common.resources.AcceleratorResource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.jobmanager.scheduler.SlotSharingGroup;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.FlinkException;
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

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Communicate with HAIER scheduler over REST API to enrich JobGraph.
 */
public class HaierClient {

	private static final Logger LOG = LoggerFactory
		.getLogger(HaierClient.class);

	private final String haierUrl;

	// The amount of GPUs already assigned by the scheduler (used for testing only)
	@VisibleForTesting
	private static int gpus_assigned = 0;

	@VisibleForTesting
	private final int max_gpus_assigned = 1;

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
	 * @param jobGraph The JobGraph.
	 * @return Enriched JobGraph, or original JobGraph if HAIER URL is not configured.
	 */
	public JobGraph enrichJobGraph(@Nonnull JobGraph jobGraph) throws FlinkException {
		LOG.info("vvvvvvvvvvvvvvvvvvv   HAIER   vvvvvvvvvvvvvvvvvvvvvvvv");

		try {
			Preconditions.checkNotNull(this.haierUrl);
		} catch (NullPointerException e) {
			LOG.warn("HAIER URL is not configured. Returning unprocessed jobGraph.");
			return jobGraph;
		}

		java.nio.file.Path jobGraphFile;
		try {
			jobGraphFile = createTemporaryFile(jobGraph);
		} catch (FlinkException e) {
			throw new FlinkException("Failed to serialize the JobGraph for HAIER.");
		}

		final List<HaierSerializableScheduledJobVertex> haierJobVertices = receiveHaierSchedule(jobGraphFile);
		cleanupTemporaryFile(jobGraphFile);

		return mergeHaierSchedule(jobGraph, haierJobVertices);
//		return mergeGpuOnlySchedule(jobGraph, haierJobVertices);
//		return mergeCpuOnlySchedule(jobGraph, haierJobVertices);
	}

	private java.nio.file.Path createTemporaryFile(@Nonnull JobGraph jobGraph) throws FlinkException {
		try {
			final java.nio.file.Path jobGraphFile = Files.createTempFile("flink-jobgraph", ".bin");
			try (ObjectOutputStream objectOut = new ObjectOutputStream(Files.newOutputStream(jobGraphFile))) {
				objectOut.writeObject(jobGraph);
			}
			return jobGraphFile;
		} catch (IOException e) {
			throw new FlinkException("Failed to serialize JobGraph.", e);
		}
	}

	private void cleanupTemporaryFile(@Nonnull java.nio.file.Path filePath) {
		File file = filePath.toFile();
		if (file.exists() && !file.isDirectory()) {
			if (!file.delete()) {
				LOG.warn("Failed to cleanup temporary JobGraph file '" + filePath + "'.");
			} else {
				LOG.info("JobGraph file passed to HAIER was cleaned up by HAIER, no further cleanup performed.");
			}
		} else {
			LOG.warn("Failed to cleanup temporary JobGraph file '" + filePath + "'.");
		}
	}

	private List<HaierSerializableScheduledJobVertex> receiveHaierSchedule(@Nonnull java.nio.file.Path jobGraphFile) {
		final CloseableHttpClient haierClient = HttpClients.createDefault();
		final HttpEntity haierReqEntity = MultipartEntityBuilder.create().addBinaryBody("file", jobGraphFile.toFile(), ContentType.APPLICATION_OCTET_STREAM, jobGraphFile.getFileName().toString()).build();
		final HttpPost haierReq = new HttpPost(haierUrl);
		haierReq.setEntity(haierReqEntity);

		List<HaierSerializableScheduledJobVertex> haierJobVertices = new ArrayList<>();

		try {
			LOG.info("Executing request:  " + haierReq.getRequestLine());
			final CloseableHttpResponse haierRes = haierClient.execute(haierReq);
			try {
				LOG.info("Response:  " + haierRes.getStatusLine());
				final HttpEntity haierResEntity = haierRes.getEntity();
				if (haierResEntity != null) {
					LOG.info("Response's Content-Length:  " + haierResEntity.getContentLength());
					OutputStream haierStream = new ByteArrayOutputStream();
					haierResEntity.writeTo(haierStream);
					String haierRawJSON = haierStream.toString();
					ObjectMapper haierVertexMapper = new ObjectMapper().addMixIn(JobVertexID.class, HaierJobVertexMixin.class);
					haierJobVertices = haierVertexMapper.readValue(haierRawJSON, new TypeReference<List<HaierSerializableScheduledJobVertex>>() {
					});
				}
				EntityUtils.consume(haierResEntity);
			} finally {
				haierRes.close();
			}
		} catch (Exception e) {
			throw new HaierException(e, jobGraphFile);
		}

		return haierJobVertices;
	}

	private JobGraph mergeHaierSchedule(JobGraph jobGraph, List<HaierSerializableScheduledJobVertex> haierJobVertices) {
		for (JobVertex vertex : jobGraph.getVertices()) {
			for (HaierSerializableScheduledJobVertex haierVertex : haierJobVertices) {
				if (vertex.getID().equals(haierVertex.id)) {
					final String resourceName = haierVertex.assignedResource.getName();

					if (resourceName.contains("vcores")) {
						AcceleratorResource resource = new AcceleratorResource(resourceName);
						int cores = haierVertex.assignedResource.value;
						ResourceSpec spec = ResourceSpec.newBuilder(cores, 0)
							.setProcessingUnitType(ProcessingUnitType.CPU)
							.build();
						vertex.setResources(spec, spec);

						// Make sure that ResourceSpec of SlotSharingGroup is updated. The JobMaster requests YARN
						// containers based on the the SlotSharingGroup, not the vertex.
						SlotSharingGroup slotSharingGroup = vertex.getSlotSharingGroup();
						slotSharingGroup.addVertexToGroup(vertex.getID(), spec);

						LOG.info("HAIER is requesting the resource: " + spec + " for the vertex " + vertex);
					}
					else if (resourceName.contains("gpu")) {
						// TODO: Assign host name to AcceleratorResource
						AcceleratorResource resource = new AcceleratorResource(resourceName);
						ResourceSpec spec = ResourceSpec.newBuilder(0, 0)
							.addExtendedResource(resourceName, resource)
							.setProcessingUnitType(ProcessingUnitType.GPU)
							.build();
						vertex.setResources(spec, spec);

						// Make sure that ResourceSpec of SlotSharingGroup is updated. The JobMaster requests YARN
						// containers based on the the SlotSharingGroup, not the vertex.
						SlotSharingGroup slotSharingGroup = vertex.getSlotSharingGroup();
						slotSharingGroup.addVertexToGroup(vertex.getID(), spec);

						LOG.info("HAIER is requesting the resource: " + spec + " for the vertex " + vertex);
					}
					else {
						LOG.info("HAIER is requesting an unknown resource: " + resourceName + " for the vertex " + vertex);
					}
				}
			}
		}

		return jobGraph;
	}

	// Test CPU scheduling
	@VisibleForTesting
	private JobGraph mergeCpuOnlySchedule(JobGraph jobGraph, List<HaierSerializableScheduledJobVertex> haierJobVertices) {
		int cores = 2; // a non-default value; default == 1

		for (JobVertex vertex : jobGraph.getVertices()) {
			ResourceSpec spec = ResourceSpec.newBuilder(cores, 0)
				.setProcessingUnitType(ProcessingUnitType.CPU)
				.build();
			vertex.setResources(spec, spec);

			// Make sure that ResourceSpec of SlotSharingGroup is updated. The JobMaster requests YARN
			// containers based on the the SlotSharingGroup, not the vertex.
			SlotSharingGroup slotSharingGroup = vertex.getSlotSharingGroup();
			slotSharingGroup.addVertexToGroup(vertex.getID(), spec);

			LOG.info("HAIER is requesting the resource: " + spec + " for the vertex " + vertex);
		}

		return jobGraph;
	}

	// Test GPU scheduling
	@VisibleForTesting
	private JobGraph mergeGpuOnlySchedule(JobGraph jobGraph, List<HaierSerializableScheduledJobVertex> haierJobVertices) {
		String resourceName = "yarn.io/gpu-geforcegtx1080";

		for (JobVertex vertex : jobGraph.getVertices()) {
			if (this.gpus_assigned < this.max_gpus_assigned) {
				this.gpus_assigned += 1;

				// TODO: Assign host name to AcceleratorResource
				AcceleratorResource resource = new AcceleratorResource(resourceName);
				ResourceSpec spec = ResourceSpec.newBuilder(0, 0)
					.addExtendedResource(resourceName, resource)
					.setProcessingUnitType(ProcessingUnitType.GPU)
					.build();
				vertex.setResources(spec, spec);

				// Make sure that ResourceSpec of SlotSharingGroup is updated. The JobMaster requests YARN
				// containers based on the the SlotSharingGroup, not the vertex.
				SlotSharingGroup slotSharingGroup = vertex.getSlotSharingGroup();
				slotSharingGroup.addVertexToGroup(vertex.getID(), spec);

				LOG.info("HAIER is requesting the resource: " + spec + " for the vertex " + vertex);
			}
		}

		return jobGraph;
	}
}
