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

package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.resources.AcceleratorResource;
import org.apache.flink.api.common.resources.Resource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.concurrent.Executors;
import org.apache.flink.runtime.rest.RestClient;
import org.apache.flink.runtime.rest.RestClientConfiguration;
import org.apache.flink.runtime.rest.messages.EmptyMessageParameters;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.versioning.RestAPIVersion;
import org.apache.flink.util.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Retrieve accelerator resources from YARN.
 *
 * Specifically, YARN is queries for yarn.io resources.
 * This involves sending REST API requests to YARN to detect which resources are available.
 * These resources can then be added to an existing Resource Profile.
 */
public class AcceleratorResources {

    private static final Logger LOG = LoggerFactory.getLogger(AcceleratorResources.class);

    private static final String NODE_MANAGER_HOST = "localhost";
    private static final int NODE_MANAGER_PORT = 8042;

	/**
	 * Mock a dummy accelerator resource for testing.
	 */
	@VisibleForTesting
    public static List<YarnIoResource> testResources = Collections
            .singletonList(new YarnIoResource("yarn.io/gpu-geforce1080gtx"));

    /**
     * Transform yarn.io accelerator resources into extended resources for ResourceProfiles.
     */
    public static Map<String, Resource> getAcceleratorResources() {
        List<YarnIoResource> yarnIoResources = getYarnIoResources();
        Map<String, Resource> acceleratorResources = new HashMap<>(yarnIoResources.size());
        for (YarnIoResource yarnIoResource : yarnIoResources) {
            AcceleratorResource resource = new AcceleratorResource(yarnIoResource.getIdentifier());
            acceleratorResources.put(resource.getName(), resource);
            LOG.info("Added resource profile: accelerator.name = {}", yarnIoResource.getIdentifier());
        }
        return acceleratorResources;
    }


    /**
     * Query YARN for yarn.io accelerator resources.
     *
     * @return A list of accelerator resources.
     */
    private static List<YarnIoResource> getYarnIoResources() {
        if (testResources != null) {
            LOG.warn("Overriding yarn.io resources.");
            return testResources;
        }
        ArrayList<YarnIoResource> results = new ArrayList<>();

        try {
            final RestClient restClient = new RestClient(RestClientConfiguration.fromConfiguration(new Configuration()), Executors.directExecutor());
            CompletableFuture<YarnNodeStatus> future = restClient
                    .sendRequest(NODE_MANAGER_HOST, NODE_MANAGER_PORT,
                                 YarnIoMessageHeaders.getInstance(),
                                 EmptyMessageParameters.getInstance(),
                                 EmptyRequestBody.getInstance(),
                                 Collections.emptyList(),
                                 RestAPIVersion.WS_V1);
            YarnNodeStatus nodeStatus = future.get();
            List<YarnIoResource> result = new ArrayList<>();
            for (String identifier : nodeStatus.getYarnNodeStatusNodeInfo().getYarnIoResources()) {
                result.add(new YarnIoResource(identifier));
            }
            return result;
        } catch (ConfigurationException e) {
            LOG.error("Could not create configuration for REST client to retrieve YARN IO resources.", e);
        } catch (IOException e) {
            LOG.error("Could not send REST request to retrieve YARN IO resources.", e);
        } catch (InterruptedException e) {
            LOG.error("REST request to retrieve YARN IO resources was interrupted.", e);
        } catch (ExecutionException e) {
            LOG.error("Execution of REST request to retrieve YARN IO resources failed.", e);
        }

        return results;
    }

}
