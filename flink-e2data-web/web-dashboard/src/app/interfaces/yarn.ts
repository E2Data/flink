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

export interface YarnClusterMetricsInterface {
  appsSubmitted: number;
  appsCompleted: number;
  appsPending: number;
  appsRunning: number;
  appsFailed: number;
  appsKilled: number;
  reservedMB: number;
  availableMB: number;
  allocatedMB: number;
  reservedVirtualCores: number;
  availableVirtualCores: number;
  allocatedVirtualCores: number;
  containersAllocated: number;
  containersReserved: number;
  containersPending: number;
  totalMB: number;
  totalVirtualCores: number;
  totalNodes: number;
  lostNodes: number;
  unhealthyNodes: number;
  decommissioningNodes: number;
  decommissionedNodes: number;
  rebootedNodes: number;
  activeNodes: number;
  shutdownNodes: number;
}

export interface YarnClusterInfoInterface {
  clusterMetrics: YarnClusterMetricsInterface;
}

export interface YarnNodeResourceUtilization {
  nodePhysicalMemoryMB: number;
  nodeVirtualMemoryMB: number;
  nodeCPUUsage: number;
  aggregatedContainersPhysicalMemoryMB: number;
  aggregatedContainersVirtualMemoryMB: number;
  containersCPUUsage: number;
}

export interface YarnNodeResourceInformation {
  maximumAllocation: number;
  minimumAllocation: number;
  name: string;
  resourceType: string;
  units: string;
  value: number;
}

export interface YarnNodeResource {
  memory: number;
  vCores: number;
  resourceInformation: YarnNodeResourceInformation[];
}

export interface YarnNodeInfoInterface {
  rack: string;
  state: string;
  id: string;
  nodeHostName: string;
  nodeHTTPAddress: string;
  lastHealthUpdate: number;
  version: string;
  healthReport: string;
  numContainers: number;
  usedMemoryMB: number;
  availMemoryMB: number;
  usedVirtualCores: number;
  availableVirtualCores: number;
  numRunningOpportContainers: number;
  usedMemoryOpportGB: number;
  usedVirtualCoresOpport: number;
  numQueuedContainers: number;
  resourceUtilization: YarnNodeResourceUtilization;
  usedResource: YarnNodeResource;
  availableResource: YarnNodeResource;
}

export interface YarnNodeInterface {
  node: YarnNodeInfoInterface[];
}

export interface YarnNodesInfoInterface {
  nodes: YarnNodeInterface;
}

export interface ResourceManagerNodeInterface {
  node: YarnNodeInfoInterface;
  availableModules: string;
  hasCpu: boolean;
  hasGpu: boolean;
  hasFpga: boolean;
  usingCpu: boolean;
  usingGpu: boolean;
  usingFpga: boolean;
}
