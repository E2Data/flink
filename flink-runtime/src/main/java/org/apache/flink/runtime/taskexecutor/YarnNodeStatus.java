package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YarnNodeStatus implements ResponseBody, Serializable {

    private static final Logger LOG = LoggerFactory
            .getLogger(YarnNodeStatus.class);

    private static final long serialVersionUID = 3447848215606486927L;

    public static final String FIELD_NAME_NODE_INFO = "nodeInfo";

    @JsonProperty(FIELD_NAME_NODE_INFO)
    private final YarnNodeStatusNodeInfo nodeInfo;

    @JsonCreator
    public YarnNodeStatus(
            @JsonProperty(FIELD_NAME_NODE_INFO) YarnNodeStatusNodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public YarnNodeStatusNodeInfo getYarnNodeStatusNodeInfo() {
        return nodeInfo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YarnNodeStatusNodeInfo implements Serializable {

        private static final long serialVersionUID = 6809909673866451798L;

        public static final String FIELD_NAME_RESOURCE_TYPES = "resourceTypes";

        @JsonProperty(FIELD_NAME_RESOURCE_TYPES)
        private final String resourceTypes;

        @JsonCreator
        public YarnNodeStatusNodeInfo(
                @JsonProperty(FIELD_NAME_RESOURCE_TYPES) String resourceTypes) {
            this.resourceTypes = resourceTypes;
        }

        public List<String> getYarnIoResources() {
            List<String> result = new ArrayList<>();
            for (String resourceType : resourceTypes.split(", ")) {
                if (resourceType.startsWith("yarn.io")) {
                    LOG.info("Found yarn.io resource: {}", resourceType);
                    result.add(resourceType);
                }
            }
            return result;
        }

    }

}
