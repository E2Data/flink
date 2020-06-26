package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.api.common.resources.AcceleratorResource;
import org.apache.flink.api.common.resources.Resource;
import org.apache.flink.configuration.Configuration;
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

public class AcceleratorResources {

    private static final Logger LOG = LoggerFactory.getLogger(AcceleratorResources.class);

    private static final String NODE_MANAGER_HOST = "localhost";
    private static final int NODE_MANAGER_PORT = 8042;

    public static List<YarnIoResource> testResources = Collections
            .singletonList(new YarnIoResource("yarn.io/gpu-geforce1080gtx"));

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
