package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.api.common.resources.AcceleratorResource;
import org.apache.flink.api.common.resources.Resource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.clusterframework.types.ResourceProfile;
import org.apache.flink.runtime.concurrent.Executors;
import org.apache.flink.runtime.rest.RestClient;
import org.apache.flink.runtime.rest.RestClientConfiguration;
import org.apache.flink.runtime.rest.messages.*;
import org.apache.flink.runtime.rest.versioning.RestAPIVersion;
import org.apache.flink.util.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class YarnIoResources {

    private static final Logger LOG = LoggerFactory.getLogger(YarnIoResources.class);

    private static final String NODE_MANAGER_HOST = "localhost";
    private static final int NODE_MANAGER_PORT = 8042;

    public static List<YarnIoResource> testResources = Collections
            .singletonList(new YarnIoResource("yarn.io/gpu-geforce1080gtx"));

    static void addYarnIoResources(
            final List<ResourceProfile> resourceProfiles) {
        List<YarnIoResource> yarnIoResources = getYarnIoResources();
        if (testResources != null) {
            LOG.warn("Overriding yarn.io resources.");
            yarnIoResources = testResources;
        }
        for (YarnIoResource yarnIoResource : yarnIoResources) {
            Map<String, Resource> extendedResources = new HashMap<>();
            extendedResources.put("accelerator.name", new AcceleratorResource(yarnIoResource.getIdentifier()));
            resourceProfiles.add(new ResourceProfile(yarnIoResource.getProcessingUnitType(), 2048, Integer.MAX_VALUE, Integer.MAX_VALUE, 8192, Integer.MAX_VALUE, extendedResources));
            LOG.info("Added resource profile: ProcessingUnitType = {}, accelerator.name = {}", yarnIoResource.getProcessingUnitType().name(), yarnIoResource.getIdentifier());
        }

    }

    private static List<YarnIoResource> getYarnIoResources() {
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
