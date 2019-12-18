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
package org.envirocar.auth.proxy.common;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HeaderUtil {

    public static HttpHeaders basicAuth(String username, String password) {
        HttpHeaders acceptHeaders = new HttpHeaders();
        acceptHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        String authorizationValue = createAuthorizationValue(username, password);
        acceptHeaders.set("Authorization", authorizationValue);

        // http://envirocar.github.io/enviroCar-server/api/authentification
//        acceptHeaders.set("X-User", username);
//        acceptHeaders.set("X-Token", password);
        return acceptHeaders;
    }

    public static String createAuthorizationValue(String username, String password) {
        String authorization = username + ":" + password;
        byte[] bytes = authorization.getBytes(Charset.forName("US-ASCII"));
        return "Basic " + new String(encode(bytes));
    }

    private static byte[] encode(byte[] bytes) {
        Encoder encoder = Base64.getEncoder();
        return encoder.encode(bytes);
    }
}
