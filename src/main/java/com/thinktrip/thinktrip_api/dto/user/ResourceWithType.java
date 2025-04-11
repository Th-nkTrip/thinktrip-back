package com.thinktrip.thinktrip_api.dto.user;

import org.springframework.core.io.Resource;

public class ResourceWithType {
    private final Resource resource;
    private final String contentType;

    public ResourceWithType(Resource resource, String contentType) {
        this.resource = resource;
        this.contentType = contentType;
    }

    public Resource getResource() {
        return resource;
    }

    public String getContentType() {
        return contentType;
    }
}
