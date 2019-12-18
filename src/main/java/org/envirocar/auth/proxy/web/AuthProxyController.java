/*
 * Copyright (C) 2019 52°North Initiative for Geospatial Open Source
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

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

@RestController
public class AuthProxyController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthProxyController.class);
    private static final String PATH_PREFIX = "/api";
    private static final String FORWARDED_HOST_HEADER = "X-Forwarded-Host";
    private static final String FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
    private static final String FORWARDED_PORT_HEADER = "X-Forwarded-Port";

    private final RestTemplate restTemplate;
    private final URI endpoint;
    private final String contextPath;
    private final Set<String> headersToIgnore;

    public AuthProxyController(
            @Value("${auth-proxy.target.uri}") URI endpoint,
            @Value("${server.servlet.context-path}") String contextPath,
            @Value("#{'${auth-proxy.headersToIgnore}'.split(',')}") Set<String> headersToIgnore) {
        restTemplate = createRestTemplate();
        this.endpoint = endpoint;
        this.contextPath = !contextPath.equals("/") ? contextPath : "";
        this.headersToIgnore = new TreeSet<>(String::compareToIgnoreCase);
        if (headersToIgnore != null) {
            this.headersToIgnore.addAll(headersToIgnore);
        }

    }

    private RestTemplate createRestTemplate() {
        CloseableHttpClient clientBuilder = HttpClientBuilder.create().build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(clientBuilder));
    }

    @ResponseBody
    @RequestMapping(PATH_PREFIX + "/**")
    public ResponseEntity<?> mirrorRest(@RequestBody(required = false) String body, HttpMethod method,
                                        HttpServletRequest request) throws URISyntaxException {
        String scheme = endpoint.getScheme();
        String host = endpoint.getHost();
        int port = endpoint.getPort();
        String path = createPath(request);

        HttpHeaders httpHeader = getHttpHeaders(request);

        HttpEntity<Object> entity = new HttpEntity<>(body, httpHeader);
        URI uri = new URI(scheme, null, host, port, path, request.getQueryString(), null);

        LOG.debug("Forwarding to: {}", uri.toString());
        LOG.debug("R E Q U E S T info:");
        LOG.debug("headers    : {}", request.getHeaderNames());
        LOG.debug("host       : {}", host);
        LOG.debug("port       : {}", port);
        LOG.debug("path       : {}", path);
        LOG.debug("scheme     : {}", scheme);
        LOG.debug("httpHeader : {}", httpHeader);
        LOG.debug("body       : {}", body);
        LOG.debug(" ");
        ResponseEntity<Object> re = restTemplate.exchange(uri, method, entity, Object.class);
        LOG.debug("R E S P O N S E info:");
        LOG.debug("headers         : {}", re.getHeaders());
        LOG.debug("StatusCode      : {}", re.getStatusCode());
        LOG.debug("StatusCodeValue : {}", re.getStatusCodeValue());
        if (re.getBody() != null) {
            LOG.debug("body            : {}", re.getBody().toString());
        }
        return re;
    }

    private HttpHeaders getHttpHeaders(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration<String> sentHeaders = request.getHeaderNames();
        while (sentHeaders.hasMoreElements()) {
            String sentHeader = sentHeaders.nextElement();
            if ((sentHeader != null) && !headersToIgnore.contains(sentHeader)) {
                httpHeaders.add(sentHeader, request.getHeader(sentHeader));
            }
        }
        if (!httpHeaders.containsKey(HttpHeaders.AUTHORIZATION)) {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            String username = authentication.getName();
            String password = (String) authentication.getCredentials();
            httpHeaders.set(HttpHeaders.AUTHORIZATION, HeaderUtil.createAuthorizationValue(username, password));
        }
        final URI requestURI = URI.create(request.getRequestURI());
        if (!httpHeaders.containsKey(FORWARDED_HOST_HEADER)) {
            String host = request.getHeader(HttpHeaders.HOST);
            if (host == null) {
                host = requestURI.getHost();
            }
            httpHeaders.set(FORWARDED_HOST_HEADER, host);
        }
        if (!httpHeaders.containsKey(FORWARDED_PROTO_HEADER)) {
            httpHeaders.set(FORWARDED_PROTO_HEADER, requestURI.getScheme());
        }
        if (!httpHeaders.containsKey(FORWARDED_PORT_HEADER)) {
            if (requestURI.getPort() > 0) {
                httpHeaders.set(FORWARDED_PORT_HEADER, String.valueOf(requestURI.getPort()));
            }
        }
        return httpHeaders;
    }

    protected String createPath(HttpServletRequest request) {
        String endpointPath = removeTrailingSlash(endpoint.getPath());
        String targetPath = removePathPrefix(request.getRequestURI());
        return endpointPath + targetPath;
    }

    private String removeTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String removePathPrefix(String requestURI) {
        return requestURI.substring(contextPath.length() + PATH_PREFIX.length());
    }

}
