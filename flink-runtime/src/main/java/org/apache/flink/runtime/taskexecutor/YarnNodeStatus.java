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

import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifies the node status of a YARN node manager. The status includes information about which hardware resources
 * are provided by this node.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class YarnNodeStatus implements ResponseBody, Serializable {

    private static final Logger LOG = LoggerFactory
            .getLogger(YarnNodeStatus.class);

    private static final long serialVersionUID = 3447848215606486927L;

	/**
	 * A field name for serializing this class to JSON.
	 */
	public static final String FIELD_NAME_NODE_INFO = "nodeInfo";

	/**
	 * The node's status information.
	 */
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

	/**
	 * An internal field to match the serialized JSON description expected by YARN.
	 */
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
