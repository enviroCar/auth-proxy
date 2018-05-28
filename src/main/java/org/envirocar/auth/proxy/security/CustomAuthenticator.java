
package org.envirocar.auth.proxy.security;

import java.util.Collections;
import java.util.Map;

import org.envirocar.auth.proxy.common.HeaderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomAuthenticator {
    
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

    public ResponseEntity<AuthenticatedUser> authenticate(String username, String pass) {
        HttpEntity<AuthenticatedUser> entity = new HttpEntity<AuthenticatedUser>(HeaderUtil.basicAuth(username, pass));
        Map<String, String> values = Collections.singletonMap(USERNAME, username);
        return restTemplate.exchange(uriTemplate, HttpMethod.GET, entity, AuthenticatedUser.class, values);
    }


}
