package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.api.common.ProcessingUnitType;

/**
 * Wrapper for a Yarn IO resource that represents a GPU, FPGA, or other type
 * of accelerator. See E2Data deliverable D3.2 for details. Parses the
 * identifier string to determine the type of processor.
 */
class YarnIoResource {

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
