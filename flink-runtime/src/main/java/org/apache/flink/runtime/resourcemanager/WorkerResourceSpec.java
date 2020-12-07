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

package org.apache.flink.runtime.resourcemanager;

import org.apache.flink.api.common.resources.CPUResource;
import org.apache.flink.api.common.resources.Resource;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.runtime.clusterframework.TaskExecutorProcessSpec;
import org.apache.flink.runtime.clusterframework.types.ResourceProfile;
import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resource specification of a worker, mainly used by SlotManager requesting from ResourceManager.
 */
public final class WorkerResourceSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final WorkerResourceSpec ZERO = new Builder().build();

	private final CPUResource cpuCores;

	private final MemorySize taskHeapSize;

	private final MemorySize taskOffHeapSize;

	private final MemorySize networkMemSize;

	private final MemorySize managedMemSize;

	private final Map<String, Resource> extendedResources;

	private WorkerResourceSpec(
		CPUResource cpuCores,
		MemorySize taskHeapSize,
		MemorySize taskOffHeapSize,
		MemorySize networkMemSize,
		MemorySize managedMemSize) {

		this.cpuCores = Preconditions.checkNotNull(cpuCores);
		this.taskHeapSize = Preconditions.checkNotNull(taskHeapSize);
		this.taskOffHeapSize = Preconditions.checkNotNull(taskOffHeapSize);
		this.networkMemSize = Preconditions.checkNotNull(networkMemSize);
		this.managedMemSize = Preconditions.checkNotNull(managedMemSize);
		this.extendedResources = new HashMap<>(1);
	}

	private WorkerResourceSpec(
		CPUResource cpuCores,
		MemorySize taskHeapSize,
		MemorySize taskOffHeapSize,
		MemorySize networkMemSize,
		MemorySize managedMemSize,
		Map<String, Resource> extendedResources
		) {

		this.cpuCores = Preconditions.checkNotNull(cpuCores);
		this.taskHeapSize = Preconditions.checkNotNull(taskHeapSize);
		this.taskOffHeapSize = Preconditions.checkNotNull(taskOffHeapSize);
		this.networkMemSize = Preconditions.checkNotNull(networkMemSize);
		this.managedMemSize = Preconditions.checkNotNull(managedMemSize);
		this.extendedResources = new HashMap<>(Preconditions.checkNotNull(extendedResources));
	}

	public static WorkerResourceSpec fromTaskExecutorProcessSpec(final TaskExecutorProcessSpec taskExecutorProcessSpec) {
		Preconditions.checkNotNull(taskExecutorProcessSpec);
		return new WorkerResourceSpec(
			taskExecutorProcessSpec.getCpuCores(),
			taskExecutorProcessSpec.getTaskHeapSize(),
			taskExecutorProcessSpec.getTaskOffHeapSize(),
			taskExecutorProcessSpec.getNetworkMemSize(),
			taskExecutorProcessSpec.getManagedMemorySize());
	}

	public static WorkerResourceSpec fromResourceProfile(final ResourceProfile profile) {
		Preconditions.checkNotNull(profile);
		return new WorkerResourceSpec(
			(CPUResource) profile.getCpuCores(),
			profile.getTaskHeapMemory(),
			profile.getTaskOffHeapMemory(),
			profile.getNetworkMemory(),
			profile.getManagedMemory(),
			profile.getExtendedResources()
		);
	}

	public CPUResource getCpuCores() {
		return cpuCores;
	}

	public MemorySize getTaskHeapSize() {
		return taskHeapSize;
	}

	public MemorySize getTaskOffHeapSize() {
		return taskOffHeapSize;
	}

	public MemorySize getNetworkMemSize() {
		return networkMemSize;
	}

	public MemorySize getManagedMemSize() {
		return managedMemSize;
	}

	public Map<String, Resource> getExtendedResources() {
		return Collections.unmodifiableMap(extendedResources);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cpuCores, taskHeapSize, taskOffHeapSize, networkMemSize, managedMemSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj != null && obj.getClass() == WorkerResourceSpec.class) {
			WorkerResourceSpec that = (WorkerResourceSpec) obj;
			return Objects.equals(this.cpuCores, that.cpuCores) &&
				Objects.equals(this.taskHeapSize, that.taskHeapSize) &&
				Objects.equals(this.taskOffHeapSize, that.taskOffHeapSize) &&
				Objects.equals(this.networkMemSize, that.networkMemSize) &&
				Objects.equals(this.managedMemSize, that.managedMemSize) &&
				Objects.equals(this.extendedResources, that.extendedResources);
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder extendedResourceStr = new StringBuilder(extendedResources.size() * 10);
		for (Map.Entry<String, Resource> resource : extendedResources.entrySet()) {
			String key = resource.getKey();
			Resource value = resource.getValue();
			if (value == null) {
				extendedResourceStr.append(", ").append(key).append("= null");
			}
			else {
				extendedResourceStr.append(", ").append(key).append('=').append(value.getName());
			}
		}

		return "WorkerResourceSpec {"
			+ "cpuCores=" + cpuCores.getValue().doubleValue()
			+ ", taskHeapSize=" + taskHeapSize.toHumanReadableString()
			+ ", taskOffHeapSize=" + taskOffHeapSize.toHumanReadableString()
			+ ", networkMemSize=" + networkMemSize.toHumanReadableString()
			+ ", managedMemSize=" + managedMemSize.toHumanReadableString()
			+ extendedResourceStr
			+ "}";
	}

	/**
	 * Builder for {@link WorkerResourceSpec}.
	 */
	public static class Builder {
		private CPUResource cpuCores = new CPUResource(0.0);
		private MemorySize taskHeapSize = MemorySize.ZERO;
		private MemorySize taskOffHeapSize = MemorySize.ZERO;
		private MemorySize networkMemSize = MemorySize.ZERO;
		private MemorySize managedMemSize = MemorySize.ZERO;

		public Builder() {}

		public Builder setCpuCores(double cpuCores) {
			this.cpuCores = new CPUResource(cpuCores);
			return this;
		}

		public Builder setTaskHeapMemoryMB(int taskHeapMemoryMB) {
			this.taskHeapSize = MemorySize.ofMebiBytes(taskHeapMemoryMB);
			return this;
		}

		public Builder setTaskOffHeapMemoryMB(int taskOffHeapMemoryMB) {
			this.taskOffHeapSize = MemorySize.ofMebiBytes(taskOffHeapMemoryMB);
			return this;
		}

		public Builder setNetworkMemoryMB(int networkMemoryMB) {
			this.networkMemSize = MemorySize.ofMebiBytes(networkMemoryMB);
			return this;
		}

		public Builder setManagedMemoryMB(int managedMemoryMB) {
			this.managedMemSize = MemorySize.ofMebiBytes(managedMemoryMB);
			return this;
		}

		public WorkerResourceSpec build() {
			return new WorkerResourceSpec(
				cpuCores,
				taskHeapSize,
				taskOffHeapSize,
				networkMemSize,
				managedMemSize);
		}
	}
}
