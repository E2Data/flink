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

package org.apache.flink.runtime.clusterframework.types;

import org.apache.flink.api.common.operators.ResourceSpec;
import org.apache.flink.api.common.resources.Resource;
import org.apache.flink.api.common.ProcessingUnitType;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Describe the immutable resource profile of the slot, either when requiring or offering it. The profile can be
 * checked whether it can match another profile's requirement, and furthermore we may calculate a matching
 * score to decide which profile we should choose when we have lots of candidate slots.
 * It should be generated from {@link ResourceSpec} with the input and output memory calculated in JobMaster.
 *
 * <p>Resource Profiles have a total ordering, defined by comparing these fields in sequence:
 * <ol>
 *     <li>Memory Size</li>
 *     <li>Cores</li>
 *     <li>Extended resources</li>
 * </ol>
 * The extended resources are compared ordered by the resource names.
 */
public class ResourceProfile implements Serializable, Comparable<ResourceProfile> {

	private static final long serialVersionUID = 1L;

	public static final ResourceProfile UNKNOWN = new ResourceProfile(-1.0, -1);

	/** ResourceProfile which matches any other ResourceProfile. */
	public static final ResourceProfile ANY = new ResourceProfile(ProcessingUnitType.ANY, Double.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Collections.emptyMap());
	public static final ResourceProfile ANY_GPU = new ResourceProfile(ProcessingUnitType.GPU, Double.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Collections.emptyMap());
	public static final ResourceProfile ANY_CPU = new ResourceProfile(ProcessingUnitType.CPU, Double.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Collections.emptyMap());


	// ------------------------------------------------------------------------


	/** Which processing unit type is needed*/
	private final ProcessingUnitType processingUnitType;

	/** How many cores are needed, use double so we can specify cpu like 0.1. */
	private final double cores;

	/** How many heap memory in mb are needed. */
	private final int heapMemoryInMB;

	/** How many direct memory in mb are needed. */
	private final int directMemoryInMB;

	/** How many native memory in mb are needed. */
	private final int nativeMemoryInMB;

	/** Memory used for the task in the slot to communicate with its upstreams. Set by job master. */
	private final int networkMemoryInMB;

	/** A extensible field for user specified resources from {@link ResourceSpec}. */
	private final Map<String, Resource> extendedResources = new HashMap<>(1);

	// ------------------------------------------------------------------------

	/**
	 * Creates a new ResourceProfile.
	 *
	 * @param cores The number of cores (possibly fractional, i.e., 0.2 cores)
	 * @param heapMemoryInMB The size of the heap memory, in megabytes.
	 * @param directMemoryInMB The size of the direct memory, in megabytes.
	 * @param nativeMemoryInMB The size of the native memory, in megabytes.
	 * @param networkMemoryInMB The size of the memory for input and output, in megabytes.
	 * @param extendedResources The extended resources such as GPU and FPGA
	 */
	public ResourceProfile(
			ProcessingUnitType processingUnitType,
			double cores,
			int heapMemoryInMB,
			int directMemoryInMB,
			int nativeMemoryInMB,
			int networkMemoryInMB,
			Map<String, Resource> extendedResources) {
		this.processingUnitType = processingUnitType;
		this.cores = cores;
		this.heapMemoryInMB = heapMemoryInMB;
		this.directMemoryInMB = directMemoryInMB;
		this.nativeMemoryInMB = nativeMemoryInMB;
		this.networkMemoryInMB = networkMemoryInMB;
		if (extendedResources != null) {
			this.extendedResources.putAll(extendedResources);
		}
	}

	/**
	 * Creates a new simple ResourceProfile used for testing.
	 *
	 * @param cores The number of  cores (possibly fractional, i.e., 0.2 cores)
	 * @param heapMemoryInMB The size of the heap memory, in megabytes.
	 */
	public ResourceProfile(double cores, int heapMemoryInMB) {
		this(ProcessingUnitType.ANY, cores, heapMemoryInMB, 0, 0, 0, Collections.emptyMap());
	}

	/**
	 * Creates a copy of the given ResourceProfile.
	 *
	 * @param other The ResourceProfile to copy.
	 */
	public ResourceProfile(ResourceProfile other) {
		this(other.processingUnitType,
				other.cores,
				other.heapMemoryInMB,
				other.directMemoryInMB,
				other.nativeMemoryInMB,
				other.networkMemoryInMB,
				other.extendedResources);
	}

	// ------------------------------------------------------------------------

	/**
	 * Get the  processing unit type needed.
	 *
	 * @return The  processing unit type
	 */
	public ProcessingUnitType getProcessingUnitType() {
		return processingUnitType;
	}

	/**
	 * Get the cores needed.
	 *
	 * @return The cores, 1.0 means a full cpu thread
	 */
	public double getCores() {
		return cores;
	}

	/**
	 * Get the heap memory needed in MB.
	 *
	 * @return The heap memory in MB
	 */
	public int getHeapMemoryInMB() {
		return heapMemoryInMB;
	}

	/**
	 * Get the direct memory needed in MB.
	 *
	 * @return The direct memory in MB
	 */
	public int getDirectMemoryInMB() {
		return directMemoryInMB;
	}

	/**
	 * Get the native memory needed in MB.
	 *
	 * @return The native memory in MB
	 */
	public int getNativeMemoryInMB() {
		return nativeMemoryInMB;
	}

	/**
	 * Get the memory needed for task to communicate with its upstreams and downstreams in MB.
	 * @return The network memory in MB
	 */
	public int getNetworkMemoryInMB() {
		return networkMemoryInMB;
	}

	/**
	 * Get the total memory needed in MB.
	 *
	 * @return The total memory in MB
	 */
	public int getMemoryInMB() {
		return heapMemoryInMB + directMemoryInMB + nativeMemoryInMB + networkMemoryInMB;
	}

	/**
	 * Get the memory the operators needed in MB.
	 *
	 * @return The operator memory in MB
	 */
	public int getOperatorsMemoryInMB() {
		return heapMemoryInMB + directMemoryInMB + nativeMemoryInMB;
	}

	/**
	 * Get the extended resources.
	 *
	 * @return The extended resources
	 */
	public Map<String, Resource> getExtendedResources() {
		return Collections.unmodifiableMap(extendedResources);
	}

	/**
	 * Check whether required resource profile can be matched.
	 *
	 * @param required the required resource profile
	 * @return true if the requirement is matched, otherwise false
	 */
	public boolean isMatching(ResourceProfile required) {
		if (cores >= required.getCores() &&
				heapMemoryInMB >= required.getHeapMemoryInMB() &&
				directMemoryInMB >= required.getDirectMemoryInMB() &&
				nativeMemoryInMB >= required.getNativeMemoryInMB() &&
				networkMemoryInMB >= required.getNetworkMemoryInMB() && (
				processingUnitType.equals(required.getProcessingUnitType())
						|| required.getProcessingUnitType().equals(ProcessingUnitType.ANY)
						|| this.processingUnitType.equals(ProcessingUnitType.ANY)))
		{
			for (Map.Entry<String, Resource> resource : required.extendedResources.entrySet()) {
				if (!extendedResources.containsKey(resource.getKey()) ||
						!extendedResources.get(resource.getKey()).getResourceAggregateType().equals(resource.getValue().getResourceAggregateType()) ||
						extendedResources.get(resource.getKey()).getValue() < resource.getValue().getValue()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(@Nonnull ResourceProfile other) {
		int cmp = Integer.compare(this.getMemoryInMB(), other.getMemoryInMB());
		if (cmp == 0) {
			cmp = Double.compare(this.cores, other.cores);
		}
		if (cmp == 0) {
			Iterator<Map.Entry<String, Resource>> thisIterator = extendedResources.entrySet().iterator();
			Iterator<Map.Entry<String, Resource>> otherIterator = other.extendedResources.entrySet().iterator();
			while (thisIterator.hasNext() && otherIterator.hasNext()) {
				Map.Entry<String, Resource> thisResource = thisIterator.next();
				Map.Entry<String, Resource> otherResource = otherIterator.next();
				if ((cmp = otherResource.getKey().compareTo(thisResource.getKey())) != 0) {
					return cmp;
				}
				if (!otherResource.getValue().getResourceAggregateType().equals(thisResource.getValue().getResourceAggregateType())) {
					return 1;
				}
				if ((cmp = Double.compare(thisResource.getValue().getValue(), otherResource.getValue().getValue())) != 0) {
					return cmp;
				}
			}
			if (thisIterator.hasNext()) {
				return 1;
			}
			if (otherIterator.hasNext()) {
				return -1;
			}
		}
		return cmp;
	}

	// ------------------------------------------------------------------------

	@Override
	public int hashCode() {
		final long coresBits =  Double.doubleToLongBits(cores);
		int result = (int) (coresBits ^ (coresBits >>> 32));
		result = 31 * result + heapMemoryInMB;
		result = 31 * result + directMemoryInMB;
		result = 31 * result + nativeMemoryInMB;
		result = 31 * result + networkMemoryInMB;
		result = 31 * result + extendedResources.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (obj != null && obj.getClass() == ResourceProfile.class) {
			ResourceProfile that = (ResourceProfile) obj;
			return this.cores == that.cores &&
					this.heapMemoryInMB == that.heapMemoryInMB &&
					this.directMemoryInMB == that.directMemoryInMB &&
					this.networkMemoryInMB == that.networkMemoryInMB &&
					this.processingUnitType == that.processingUnitType &&
					Objects.equals(extendedResources, that.extendedResources);
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder resources = new StringBuilder(extendedResources.size() * 10);
		for (Map.Entry<String, Resource> resource : extendedResources.entrySet()) {
			resources.append(", ").append(resource.getKey()).append('=').append(resource.getValue());
		}
		return "ResourceProfile{" +
			"processingUnitType="+processingUnitType+
			"cores=" + cores +
			", heapMemoryInMB=" + heapMemoryInMB +
			", directMemoryInMB=" + directMemoryInMB +
			", nativeMemoryInMB=" + nativeMemoryInMB +
			", networkMemoryInMB=" + networkMemoryInMB + resources +
			'}';
	}

	public static ResourceProfile fromResourceSpec(ResourceSpec resourceSpec, int networkMemory) {
		Map<String, Resource> copiedExtendedResources = new HashMap<>(resourceSpec.getExtendedResources());

		return new ResourceProfile(
				resourceSpec.getProcessingUnitType(),
				resourceSpec.getCores(),
				resourceSpec.getHeapMemory(),
				resourceSpec.getDirectMemory(),
				resourceSpec.getNativeMemory(),
				networkMemory,
				copiedExtendedResources);
	}
}
