package org.apache.flink.api.common;

/**
 * Specifies the type of processing unit requested, e.g., a CPU or a GPU.
 *
 * ProcessingUnitType is used in {@link org.apache.flink.api.common.operators.ResourceSpec} to request resources of the
 * type specified by the user.
 */
public enum ProcessingUnitType {
    ANY, CPU, GPU, FPGA, ASIC
}
