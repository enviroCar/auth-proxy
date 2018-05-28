
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

    private static String createAuthorizationValue(String username, String password) {
        String authorization = username + ":" + password;
        byte[] bytes = authorization.getBytes(Charset.forName("US-ASCII"));
        return "Basic " + new String(encode(bytes));
    }

    private static byte[] encode(byte[] bytes) {
        Encoder encoder = Base64.getEncoder();
        return encoder.encode(bytes);
    }
}
