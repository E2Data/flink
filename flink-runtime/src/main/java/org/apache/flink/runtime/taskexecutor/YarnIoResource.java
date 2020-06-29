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

/**
 * Wrapper for a Yarn IO resource that represents a GPU, FPGA, or other type
 * of accelerator. Parses the identifier string to determine the type of processor. This resource eventually gets
 * mapped into an {@AcceleratorResource}.
 */
class YarnIoResource {

	/**
	 * A string that identifies the resource.
	 */
    private String identifier;

    YarnIoResource(String identifier) {
        this.identifier = identifier;
    }

    String getIdentifier() {
        return identifier;
    }

    ProcessingUnitType getProcessingUnitType() {
        if (identifier.startsWith("yarn.io/gpu-")) {
            return ProcessingUnitType.GPU;
        } else if (identifier.startsWith("yarn.io/fpga-")) {
            return ProcessingUnitType.FPGA;
        } else {
            return ProcessingUnitType.ANY;
        }
    }

}
