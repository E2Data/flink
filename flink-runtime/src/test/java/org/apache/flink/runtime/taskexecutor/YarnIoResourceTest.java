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
