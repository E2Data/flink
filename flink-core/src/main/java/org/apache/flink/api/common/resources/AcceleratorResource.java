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

package org.apache.flink.api.common.resources;

import java.math.BigDecimal;

/**
 * Specifies a hardware accelerator resource (e.g., CPU, GPU, FPGA). The resource can be matched from a user-defined
 * {@ResourceSpec} to an available resource provided by YARN.
 *
 * <p>
 *     The accelerator resource can be decorated with a name that specifies the exact device
 *     (e.g,, "gpu-teslav100sxm232gb"), as well as further details as given by YARN.
 * </p>
 */
public class AcceleratorResource extends Resource {

	private static final long serialVersionUID = -5669448809999384676L;

	/**
	 * Creates a new Accelerator Resource with the specified name.
	 *
	 * @param name A name describing the Accelerator Resource.
	 */
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
