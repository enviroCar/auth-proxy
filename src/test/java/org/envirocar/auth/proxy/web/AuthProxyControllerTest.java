/**
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

import static java.net.URI.create;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class AuthProxyControllerTest {

    @Test
    public void testCreatePath() throws Exception {
        URI endpoint = create("https://envirocar.org/api/stable/");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/auth-proxy/api/users");
        String createPath = new AuthProxyController(endpoint, "/auth-proxy").createPath(request);
        assertThat(createPath, is("/api/stable/users"));
    }
    
    @Test
    public void given_contextPathIsSlash_when_reqeustUsers_then_forwardUrlIsCorrect() throws Exception {
        URI endpoint = create("https://envirocar.org/api/stable/");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users");
        String createPath = new AuthProxyController(endpoint, "/").createPath(request);
        assertThat(createPath, is("/api/stable/users"));
    }

}
