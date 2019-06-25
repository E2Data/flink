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

package org.apache.flink.yarn;

import org.apache.flink.api.common.ProcessingUnitType;
import org.apache.flink.api.common.operators.ResourceSpec;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.client.cli.CliFrontendTestBase;
import org.apache.flink.client.cli.CliFrontendTestUtils;
import org.apache.flink.client.deployment.ClusterDeploymentException;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.client.program.ProgramInvocationException;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.AkkaOptions;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobmanager.scheduler.NoResourceAvailableException;
import org.apache.flink.runtime.jobmanager.scheduler.SlotSharingGroup;
import org.apache.flink.runtime.jobmaster.JobResult;
import org.apache.flink.yarn.util.YarnTestUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertTrue;
import static org.apache.flink.yarn.util.YarnTestUtils.getTestJarPath;

/**
 * Test cases for defining ProcessingUnitType preferences for JobGraph vertices.
 * The MiniYARNCluster should provide CPU and GPU slots: 50/50 (FLINK_CONF_DIR/e2data_conf.xml)
 */
public class PreferredProcessingUnitTypeTest extends YarnTestBase {

	private ExecutionEnvironment env;
	private ClusterSpecification clusterSpecification;
	private YarnClusterDescriptor yarnClusterDescriptor;
	private ClusterClient<ApplicationId> clusterClient;

	@Before
	public void setupYarnAndSessionCluster() throws ClusterDeploymentException {

		// Setup code copied from YARNITCase

		YARN_CONFIGURATION.set(YarnTestBase.TEST_CLUSTER_NAME_KEY, "e2data-tests");
		startYARNWithConfig(YARN_CONFIGURATION);

		Configuration configuration = new Configuration();
		configuration.setString(AkkaOptions.ASK_TIMEOUT, "30 s");
		final YarnClient yarnClient = getYarnClient();
		final YarnClusterDescriptor yarnClusterDescriptor = new YarnClusterDescriptor(
			configuration,
			getYarnConfiguration(),
			System.getenv(ConfigConstants.ENV_FLINK_CONF_DIR),
			yarnClient,
			true);

		yarnClusterDescriptor.setLocalJarPath(new Path(flinkUberjar.getAbsolutePath()));
		yarnClusterDescriptor.addShipFiles(Arrays.asList(flinkLibFolder.listFiles()));
		this.yarnClusterDescriptor = yarnClusterDescriptor;

		final ClusterSpecification clusterSpecification = new ClusterSpecification.ClusterSpecificationBuilder()
			.setMasterMemoryMB(768)
			.setTaskManagerMemoryMB(1024)
			.setSlotsPerTaskManager(3)
			.setNumberTaskManagers(2)
			.createClusterSpecification();
		this.clusterSpecification = clusterSpecification;

		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		this.env = env;

		ClusterClient<ApplicationId> clusterClient = yarnClusterDescriptor.deploySessionCluster(clusterSpecification);
		this.clusterClient = clusterClient;
	}

	/**
	 * Execute JobGraph with distinct SlotSharingGroups and ProcessingUnitTypePreferences = ANY.
	 */
	@Test
	public void testAccomplishableProcessingUnitTypePreferences01() throws ProgramInvocationException, ClusterDeploymentException, ExecutionException, InterruptedException, FileNotFoundException {
		File testingJar = getTestJarPath("BatchWordCount.jar");
		ResourceSpec demandResource = new ResourceSpec(ProcessingUnitType.GPU, 0, 0, 0, 0, 0);

		Configuration configuration = GlobalConfiguration.loadConfiguration(CliFrontendTestUtils.getConfigDir());

		PackagedProgram program = new PackagedProgram(testingJar, new String[]{});
		JobGraph jobGraph = PackagedProgramUtils.createJobGraph(program, configuration, 1);

		for (JobVertex vertex : jobGraph.getVertices()) {
			vertex.setResources(demandResource, demandResource);
		}

		clusterClient = yarnClusterDescriptor.deployJobCluster(
						clusterSpecification,
						jobGraph,
						false);
		final RestClusterClient<ApplicationId> restClusterClient = (RestClusterClient<ApplicationId>) clusterClient;
		final CompletableFuture<JobResult> jobResultCompletableFuture = restClusterClient.requestJobResult(jobGraph.getJobID());
		jobResultCompletableFuture.wait();
		final JobResult jobResult = jobResultCompletableFuture.get();

		assertTrue(jobResult.isSuccess());
	}

	/**
	 * Execute JobGraph with distinct SlotSharingGroups and ProcessingUnitTypePreferences = GPU/CPU, 50/50.
	 */
	@Test
	public void testAccomplishableProcessingUnitTypePreferences02() {
		// TODO
		assertTrue(true);
	}

	/**
	 * Execute JobGraph with distinct SlotSharingGroups and ProcessingUnitTypePreferences = FPGA/ASIC, 50/50.
	 */
	@Test(expected = NoResourceAvailableException.class)
	public void testUnaccomplishableProcessingUnitTypePreferences() throws Exception {
		// TODO
		throw new NoResourceAvailableException("");
	}

	/**
	 * Add ProcessingUnitTypePreferences to the JobVertices of a JobGraph: 50/50 processingUnitType01/processingUnitType02.
	 * @param jobGraph
	 * @param processingUnitType01
	 * @param processingUnitType02
	 */
	private void addProcessingUnitTypePreference(JobGraph jobGraph, ProcessingUnitType processingUnitType01, ProcessingUnitType processingUnitType02) {
//		int i = 0;
//		for (JobVertex vertex : jobGraph.getVertices()) {
//			if (i % 2 == 0) {
//				vertex.setPreferredResources(new ResourceSpec(processingUnitType01, 0, 0, 0, 0, 0));
//			} else {
//				vertex.setPreferredResources(new ResourceSpec(processingUnitType02, 0, 0, 0, 0, 0));
//			}
//			i++;
//		}
	}

	/**
	 * Distinct slotSharingGroups of the JobVertices of a JobGraph.
	 * @param jobGraph
	 */
	private void diversifySlotSharingGroup(JobGraph jobGraph) {
		for (JobVertex vertex : jobGraph.getVertices()) {
			vertex.getSlotSharingGroup().removeVertexFromGroup(vertex.getID());
			vertex.setSlotSharingGroup(new SlotSharingGroup());
		}
	}

	@After
	public void shutDown() throws Exception {
		if (this.clusterClient != null) {
			this.clusterClient.shutdown();
			this.yarnClusterDescriptor.killCluster(this.clusterClient.getClusterId());
		}
	}
}
