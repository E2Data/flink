package org.apache.flink.runtime.taskexecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flink.api.common.resources.AcceleratorResource;
import org.apache.flink.api.common.resources.Resource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.queryablestate.network.messages.MessageBody;
import org.apache.flink.runtime.clusterframework.types.ResourceProfile;
import org.apache.flink.runtime.concurrent.Executors;
import org.apache.flink.runtime.rest.RestClient;
import org.apache.flink.runtime.rest.RestClientConfiguration;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.MessageParameters;
import org.apache.flink.runtime.rest.messages.RequestBody;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.util.ConfigurationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.Arrays.asList;

class YarnIoResources {

    private static final Log LOG = LogFactory.getLog(YarnIoResources.class);

    private static final String NODE_MANAGER_HOST = "localhost";
    private static final int NODE_MANAGER_PORT = 8042;

    public static List<YarnIoResource> testResources = asList(new YarnIoResource("yarn.io/gpu-geforce1080gtx"));

    static void addYarnIoResources(
            final List<ResourceProfile> resourceProfiles) {
        List<YarnIoResource> yarnIoResources = testResources != null ? testResources : getYarnIoResources();
        for (YarnIoResource yarnIoResource : yarnIoResources) {
            Map<String, Resource> extendedResources = new HashMap<>();
            extendedResources.put("accelerator.name", new AcceleratorResource(yarnIoResource.getIdentifier()));
            resourceProfiles.add(new ResourceProfile(yarnIoResource.getProcessingUnitType(), 2048, Integer.MAX_VALUE, Integer.MAX_VALUE, 8192, Integer.MAX_VALUE, extendedResources));
        }

    }

    private static List<YarnIoResource> getYarnIoResources() {
        ArrayList<YarnIoResource> results = new ArrayList<>();

        try {
            final RestClient restClient = new RestClient(RestClientConfiguration.fromConfiguration(new Configuration()), Executors.directExecutor());
            MessageHeaders<RequestBody, ResponseBody, MessageParameters> messageHeaders = null;
            MessageParameters messageParameters = null;
            RequestBody messageBody = null;
            CompletableFuture<ResponseBody> future = restClient.sendRequest(NODE_MANAGER_HOST, NODE_MANAGER_PORT, messageHeaders, messageParameters, messageBody);
        } catch (ConfigurationException e) {
            LOG.error("Could not create configuration for REST client to retrieve YARN IO resources.", e);
        } catch (IOException e) {
            LOG.error("Could not send REST request to retrieve YARN IO resources.", e);
        }

        return results;
    }

}
