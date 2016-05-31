package org.sav;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public class RequestContext {
    private HttpServletRequest frontendRequest;
    private HttpServletResponse frontendResponse;
    private HttpUriRequest backendRequest;
    private HttpResponse backendResponse;

    public RequestContext(HttpServletRequest frontendRequest, HttpServletResponse frontendResponse) {
        super();
        this.frontendRequest = frontendRequest;
        this.frontendResponse = frontendResponse;
    }

    public HttpServletRequest getFrontendRequest() {
        return frontendRequest;
    }

    public void setFrontendRequest(HttpServletRequest frontendRequest) {
        this.frontendRequest = frontendRequest;
    }

    public HttpServletResponse getFrontendResponse() {
        return frontendResponse;
    }

    public void setFrontendResponse(HttpServletResponse frontendResponse) {
        this.frontendResponse = frontendResponse;
    }

    public HttpUriRequest getBackendRequest() {
        return backendRequest;
    }

    public void setBackendRequest(HttpUriRequest backendRequest) {
        this.backendRequest = backendRequest;
    }

    public HttpResponse getBackendResponse() {
        return backendResponse;
    }

    public void setBackendResponse(HttpResponse backendResponse) {
        this.backendResponse = backendResponse;
    }

    @Override
    public String toString() {
        return "RequestContext [\nfrontendRequest=" + frontendRequest + "\nfrontendResponse=" + frontendResponse + "\nbackendRequest=" + backendRequest + "\nbackendResponse=" + backendResponse + "]";
    }


}
