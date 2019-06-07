package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.api.common.ProcessingUnitType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class YarnIoResourceTest {

    @Test
    public void shouldReturnIdentifier() {
        // given
        String identifier = "yarn.io/gpu-geforce1080gtx";
        // when
        YarnIoResource resource = new YarnIoResource(identifier);
        // then
        assertThat(resource.getIdentifier(), is(identifier));
    }

    @Test
    public void shouldReturnGpuType() {
        // given
        String identifier = "yarn.io/gpu-geforce1080gtx";
        // when
        YarnIoResource resource = new YarnIoResource(identifier);
        // then
        assertThat(resource.getProcessingUnitType(), is(ProcessingUnitType.GPU));
    }

    @Test
    public void shouldReturnFpgaType() {
        // given
        String identifier = "yarn.io/fpga-somefpga";
        // when
        YarnIoResource resource = new YarnIoResource(identifier);
        // then
        assertThat(resource.getProcessingUnitType(), is(ProcessingUnitType.FPGA));
    }

}
