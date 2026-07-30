package com.asrevo.cvhome.checkout.utils;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.util.UriComponentsBuilder;

public record DomainResolver(HttpServletRequest request) {

    public String domain() {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            return origin;
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            URI refererUri = URI.create(referer);
            return refererUri.getScheme() + "://" + refererUri.getAuthority();
        }

        return UriComponentsBuilder.newInstance()
                .scheme(request.getScheme())
                .host(request.getServerName())
                .port(request.getServerPort())
                .toUriString();
    }
}
