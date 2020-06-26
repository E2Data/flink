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

package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.runtime.rest.HttpMethodWrapper;
import org.apache.flink.runtime.rest.messages.EmptyMessageParameters;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.MessageParameters;
import org.apache.flink.runtime.rest.versioning.RestAPIVersion;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Collection;
import java.util.Collections;

/**
 * This class specifies HTTP message headers for interfacing with YARN via a REST API. These are used, e.g., to
 * request a list of available hardware resources from YARN. Also see the Java interface for {@MessageHeaders}.
 *
 * <p>
 *     Note that this is a singleton class.
 * </p>
 */
public class YarnIoMessageHeaders implements
        MessageHeaders<EmptyRequestBody, YarnNodeStatus, MessageParameters> {

    private static final YarnIoMessageHeaders
            INSTANCE = new YarnIoMessageHeaders();

    public static final String URL = "/node";

    @Override
    public Class<EmptyRequestBody> getRequestClass() {
        return EmptyRequestBody.class;
    }

    @Override
    public Class<YarnNodeStatus> getResponseClass() {
       return YarnNodeStatus.class;
    }

    @Override
    public HttpResponseStatus getResponseStatusCode() {
        return HttpResponseStatus.OK;
    }

    @Override
    public MessageParameters getUnresolvedMessageParameters() {
        return EmptyMessageParameters.getInstance();
    }

    @Override
    public HttpMethodWrapper getHttpMethod() {
        return HttpMethodWrapper.GET;
    }

    @Override
    public String getTargetRestEndpointURL() {
        return URL;
    }
    @Override
    public String getDescription() {
        return "Queries YARN for hardware accelerators.";
    }

	/**
	 * Get a reference to the singleton instance of this class.
	 *
	 * @return A reference to the YARN IO Message Headers instance.
	 */
	public static YarnIoMessageHeaders getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<RestAPIVersion> getSupportedAPIVersions() {
        return Collections.singleton(RestAPIVersion.WS_V1);
    }
}
