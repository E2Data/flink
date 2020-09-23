/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.client.haier;

public class HaierHardwareResource {

	public HaierHardwareResource() {}

	public long maximumAllocation;
	public int minimumAllocation;
	public String name;
	public String units; // has meaning only in memory and indicates whether the reported amount is counted in MB/GB etc.
	public int value;
	public String host; // indicates the machine where this resource exists.

	public long getMaximumAllocation() {
		return maximumAllocation;
	}

	public void setMaximumAllocation(long maximumAllocation) {
		this.maximumAllocation = maximumAllocation;
	}

	public int getMinimumAllocation() {
		return minimumAllocation;
	}

	public void setMinimumAllocation(int minimumAllocation) {
		this.minimumAllocation = minimumAllocation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
