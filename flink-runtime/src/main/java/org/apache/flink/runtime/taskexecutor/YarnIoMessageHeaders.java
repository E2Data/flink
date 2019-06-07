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

    public static YarnIoMessageHeaders getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<RestAPIVersion> getSupportedAPIVersions() {
        return Collections.singleton(RestAPIVersion.WS_V1);
    }
}
