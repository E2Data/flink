package org.apache.flink.api.common.resources;

import org.apache.flink.util.Preconditions;

import java.math.BigDecimal;

/**
 * Defines a hardware accelerator resource (e.g., GPU, FPGA), with it's name and further details.
 */
public class AcceleratorResource extends Resource {

	private static final long serialVersionUID = -5669448809999384676L;

	public AcceleratorResource(String name) {
		this(name, new BigDecimal(1.0));
	}

	public AcceleratorResource(String name, BigDecimal value) {
		super(name, value);
	}

	@Override
	public Resource merge(Resource other) {
		Preconditions.checkArgument(this.getName().equals(other.getName()), "Can only merge accelerator resources with same name");
		return new AcceleratorResource(this.getName(), this.getValue().add(other.getValue()));
	}

	@Override
	protected Resource create(BigDecimal value) {
		return new AcceleratorResource(this.getName(), value);
	}
}
