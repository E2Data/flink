package org.apache.flink.api.common.resources;

import java.math.BigDecimal;

/**
 * Defines a hardware accelerator resource (e.g., GPU, FPGA), with it's name and further details.
 */
public class AcceleratorResource extends Resource {

	private static final long serialVersionUID = -5669448809999384676L;

	public AcceleratorResource(String name) {
		super(name, 1);
	}

	@Override
	public Resource merge(Resource other) {
		throw new RuntimeException("Cannot merge AcceleratorResource types.");
	}

	@Override
	protected Resource create(BigDecimal value) {
		return null;
	}
}
