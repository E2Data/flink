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

import org.apache.flink.annotation.Internal;
import org.apache.flink.configuration.ConfigOption;

import static org.apache.flink.configuration.ConfigOptions.key;

/**
 * Configuration parameters for communication with HAIER scheduler.
 */
@Internal
public class HaierOptions {

	/**
	 * Pass JobGraph to HAIER scheduler to enrich vertices with execution location.
	 */
	public static final ConfigOption<Boolean> ENRICH_JOB_GRAPH =
			key("client.enrich-job-graph")
			.booleanType()
			.defaultValue(false)
			.withDescription("Pass JobGraph to HAIER scheduler to enrich vertices with execution location.");

	/**
	 * REST endpoint for HAIER scheduler.
	 */
	public static final ConfigOption<String> HAIER_REST_URL =
			key("haier.rest.url")
			.stringType()
			.noDefaultValue()
			.withDescription("REST endpoint for HAIER scheduler.");

}
