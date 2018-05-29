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
package org.envirocar.auth.proxy.security;

import static org.envirocar.auth.proxy.common.HeaderUtil.basicAuth;
import static org.springframework.http.HttpMethod.GET;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Authenticates against 
 */
@Component
public class CustomAuthenticator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticator.class);
    
    private static final String USERNAME = "username";

    private final String uriTemplate;

    private final RestTemplate restTemplate;

    public CustomAuthenticator(@Value("${auth-proxy.target.uri}") String authenticationUri) {
        this.uriTemplate = createUserDetailsTemplate(authenticationUri);
        this.restTemplate = new RestTemplate();
    }

    private String createUserDetailsTemplate(String authenticationUri) {
        String base = !authenticationUri.endsWith("/")
                ? authenticationUri + "/"
                : authenticationUri;
        return base + "users/{" + USERNAME + "}";
    }

    public HttpStatus authenticate(String username, String password) {
        ResponseEntity<AuthenticatedUser> response = doRequest(username, password);
        LOGGER.debug("Authentication response: " + response.getBody());
        return response.getStatusCode();
    }

    private ResponseEntity<AuthenticatedUser> doRequest(String username, String password) {
        Map<String, String> values = Collections.singletonMap(USERNAME, username);
        HttpEntity<AuthenticatedUser> entity = new HttpEntity<>(basicAuth(username, password));
        return restTemplate.exchange(uriTemplate, GET, entity, AuthenticatedUser.class, values);
    }


}
