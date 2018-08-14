/**
 * Copyright (C) 2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.envirocar.auth.proxy.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.envirocar.auth.proxy.common.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AuthProxyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthProxyController.class);

    public static final String PATH_PREFIX = "/api";

    private final RestTemplate restTemplate;

    private final URI endpoint;

    private final String contextPath;

    public AuthProxyController(
            @Value("${auth-proxy.target.uri}") URI endpoint,
            @Value("${server.servlet.context-path}") String contextPath) {


        restTemplate = createRestTemplate();
        this.endpoint = endpoint;
        this.contextPath = !contextPath.equals("/")
                ? contextPath
                : "";
    }

    private RestTemplate createRestTemplate() {
        CloseableHttpClient clientBuilder = HttpClientBuilder.create().build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(clientBuilder));
    }

    @ResponseBody
    @RequestMapping(PATH_PREFIX + "/**")
    public ResponseEntity<?> mirrorRest(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request) throws URISyntaxException {
        String scheme = endpoint.getScheme();
        String host = endpoint.getHost();
        int port = endpoint.getPort();
        String path = createPath(request);

        HttpHeaders httpHeader = getHttpHeaders(request);
        HttpEntity<Object> entity = new HttpEntity<>(body, httpHeader);
        URI uri = new URI(scheme, null, host, port, path, request.getQueryString(), null);

        LOGGER.debug("Forwarding to {}", uri.toString());
        return restTemplate.exchange(uri, method, entity, Object.class);
    }

    private HttpHeaders getHttpHeaders(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration<String> sentHeaders = request.getHeaderNames();
        while (sentHeaders.hasMoreElements()) {
            String sentHeader = sentHeaders.nextElement();
            httpHeaders.add(sentHeader, request.getHeader(sentHeader));
        }
        if (!httpHeaders.containsKey("Authorization")) {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            String username = authentication.getName();
            String password = (String) authentication.getCredentials();
            httpHeaders.add("Authorization", HeaderUtil.createAuthorizationValue(username, password));
        }
        return httpHeaders;
    }

    /**
     * VISIBLE FOR TESTING
     * @param request
     * @return the correct path
     */
    protected String createPath(HttpServletRequest request) {
        String endpointPath = removeTrailingSlash(endpoint.getPath());
        String targetPath = removePathPrefix(request.getRequestURI());
        return endpointPath + targetPath;
    }

    private String removeTrailingSlash(String value) {
        return value.endsWith("/")
                ? value.substring(0, value.lastIndexOf("/"))
                : value;
    }

    private String removePathPrefix(String requestURI) {
        return requestURI.substring(contextPath.length() + PATH_PREFIX.length());
    }

}
