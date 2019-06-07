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

package org.apache.flink.client.deployment;

/**
 * Description of the cluster to start by the {@link ClusterDescriptor}.
 */
public final class ClusterSpecification {
	private final int masterMemoryMB;
	private final int taskManagerMemoryMB;
	private final int slotsPerTaskManager;
	private final boolean useAccelerators;

	private ClusterSpecification(int masterMemoryMB, int taskManagerMemoryMB, int slotsPerTaskManager, boolean useAccelerators) {
		this.masterMemoryMB = masterMemoryMB;
		this.taskManagerMemoryMB = taskManagerMemoryMB;
		this.slotsPerTaskManager = slotsPerTaskManager;
		this.useAccelerators = useAccelerators;
	}

	public int getMasterMemoryMB() {
		return masterMemoryMB;
	}

	public int getTaskManagerMemoryMB() {
		return taskManagerMemoryMB;
	}

	public int getSlotsPerTaskManager() {
		return slotsPerTaskManager;
	}

	public boolean getUseAccelerators() {
		return useAccelerators;
	}

	@Override
	public String toString() {
		return "ClusterSpecification{" +
			"masterMemoryMB=" + masterMemoryMB +
			", taskManagerMemoryMB=" + taskManagerMemoryMB +
			", slotsPerTaskManager=" + slotsPerTaskManager +
			'}';
	}

	/**
	 * Builder for the {@link ClusterSpecification} instance.
	 */
	public static class ClusterSpecificationBuilder {
		private int masterMemoryMB = 768;
		private int taskManagerMemoryMB = 1024;
		private int slotsPerTaskManager = 1;
		private boolean useAccelerators = false;

		public ClusterSpecificationBuilder setMasterMemoryMB(int masterMemoryMB) {
			this.masterMemoryMB = masterMemoryMB;
			return this;
		}

		public ClusterSpecificationBuilder setTaskManagerMemoryMB(int taskManagerMemoryMB) {
			this.taskManagerMemoryMB = taskManagerMemoryMB;
			return this;
		}

		public ClusterSpecificationBuilder setSlotsPerTaskManager(int slotsPerTaskManager) {
			this.slotsPerTaskManager = slotsPerTaskManager;
			return this;
		}

		public ClusterSpecificationBuilder setUseAccelerators(boolean useAccelerators) {
			this.useAccelerators = useAccelerators;
			return this;
		}

		public ClusterSpecification createClusterSpecification() {
			return new ClusterSpecification(
				masterMemoryMB,
				taskManagerMemoryMB,
				slotsPerTaskManager,
				useAccelerators);
		}
	}
}
