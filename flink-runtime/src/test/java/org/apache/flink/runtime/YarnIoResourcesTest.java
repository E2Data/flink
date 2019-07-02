package org.apache.flink.runtime;

import org.apache.flink.runtime.rest.util.RestMapperUtils;
import org.apache.flink.runtime.taskexecutor.YarnIoMessageHeaders;
import org.apache.flink.runtime.taskexecutor.YarnNodeStatus;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonParser;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JavaType;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class YarnIoResourcesTest {

    @Test
    public void shouldParseJson() throws IOException {
        // given
        final String json = "{\"nodeInfo\":{\"healthReport\":\"\",\"totalVmemAllocatedContainersMB\":8192,\"totalPmemAllocatedContainersMB\":2048,\"totalVCoresAllocatedContainers\":8,\"vmemCheckEnabled\":true,\"pmemCheckEnabled\":true,\"lastNodeUpdateTime\":1561997361757,\"resourceTypes\":\"yarn.io/gpu-geforcegtx1080, memory-mb (unit=Mi), vcores\",\"nodeHealthy\":true,\"nodeManagerVersion\":\"3.1.1\",\"nodeManagerBuildVersion\":\"3.1.1 from Unknown by viktor source checksum c62bb169a9a43e4f5ca85d5f56aff16\",\"nodeManagerVersionBuiltOn\":\"2019-06-25T14:20Z\",\"hadoopVersion\":\"3.1.1\",\"hadoopBuildVersion\":\"3.1.1 from Unknown by viktor source checksum f76ac55e5b5ff0382a9f7df36a3ca5a0\",\"hadoopVersionBuiltOn\":\"2019-06-25T14:19Z\",\"id\":\"gpu-4:36923\",\"nodeHostName\":\"gpu-4\",\"nmStartupTime\":1561805480480}}";
        // when
        final ObjectMapper objectMapper = RestMapperUtils.getStrictObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);
        final JsonParser jsonParser = objectMapper.treeAsTokens(jsonNode);
        final JavaType responseType = objectMapper.constructType(YarnNodeStatus.class);
        YarnNodeStatus status = objectMapper.readValue(jsonParser, responseType);
        // then
        assertThat(status.getYarnNodeStatusNodeInfo().getYarnIoResources(), hasItem("yarn.io/gpu-geforcegtx1080"));
    }

}
