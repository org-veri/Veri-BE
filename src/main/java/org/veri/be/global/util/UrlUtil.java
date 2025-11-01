package org.veri.be.global.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class UrlUtil {

    public static String getRequestingUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // 1. Origin 헤더
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            return origin;
        }

        // 2. Referer 헤더
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return null;
        }

        // Referer 헤더의 baseUri
        try {
            URL refererUrl = (new URI(referer)).toURL();
            String protocol = refererUrl.getProtocol();
            String host = refererUrl.getHost();
            int port = refererUrl.getPort();

            String baseUri;
            if (port != -1 && port != refererUrl.getDefaultPort()) {
                baseUri = String.format("%s://%s:%d", protocol, host, port);
            } else {
                baseUri = String.format("%s://%s", protocol, host);
            }

            return baseUri;

        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }
}
