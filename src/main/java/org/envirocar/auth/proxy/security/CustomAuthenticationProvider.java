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
package org.envirocar.auth.proxy.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.envirocar.auth.proxy.common.HeaderUtil.basicAuth;
import static org.springframework.http.HttpMethod.GET;

/*
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
    private static final String USERNAME = "username";
    private final String uriTemplate;
    private final RestTemplate restTemplate;

    @Autowired
    public CustomAuthenticationProvider(@Value("${auth-proxy.target.uri}") String authenticationUri) {
        this.uriTemplate = createUserDetailsTemplate(authenticationUri);
        this.restTemplate = new RestTemplate();
    }

    private String createUserDetailsTemplate(String authenticationUri) {
        String base = !authenticationUri.endsWith("/") ? authenticationUri + "/" : authenticationUri;
        return base + "users/{" + USERNAME + "}";
    }

    private HttpStatus authenticate(String username, String password) {
        ResponseEntity<AuthenticatedUser> response = doRequest(username, password);
        LOGGER.debug("Authentication response: " + response.getBody());
        return response.getStatusCode();
    }

    private ResponseEntity<AuthenticatedUser> doRequest(String username, String password) {
        Map<String, String> values = Collections.singletonMap(USERNAME, username);
        HttpEntity<AuthenticatedUser> entity = new HttpEntity<>(basicAuth(username, password));
        return restTemplate.exchange(uriTemplate, GET, entity, AuthenticatedUser.class, values);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        List<GrantedAuthority> authorities = Collections.emptyList();
        try {
            if (authenticate(username, password) != HttpStatus.OK) {
                throw new AuthenticationServiceException("Unable to authenticate user " + username);
            }
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS) {
                throw new UnavailableForLegalReasonsException(ex);
            } else {
                throw new AuthenticationServiceException("Unable to authenticate user " + username, ex);
            }
        } catch (Exception ex) {
            throw new AuthenticationServiceException("Unable to authenticate user " + username, ex);
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private static class AuthenticatedUser {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
