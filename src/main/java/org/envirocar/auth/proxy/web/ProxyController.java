package org.envirocar.auth.proxy.web;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.envirocar.auth.proxy.common.HeaderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProxyController {

    private final RestTemplate restTemplate;
    
    private final URI endpoint;
    
    public ProxyController(@Value("${auth-proxy.target.uri}") URI endpoint) {
        this.restTemplate = new RestTemplate();
        this.endpoint = endpoint;
    }

    @ResponseBody
    @RequestMapping("/**")
    public ResponseEntity<?> mirrorRest(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request) throws URISyntaxException {
        String scheme = endpoint.getScheme();
        String host = endpoint.getHost();
        int port = endpoint.getPort();
        String path = createPath(request);
        
        HttpHeaders basicAuthHeader = getAuthenticationHeader();
        HttpEntity<Object> entity = new HttpEntity<>(body, basicAuthHeader);
        URI uri = new URI(scheme, null, host, port, path, request.getQueryString(), null);
        return restTemplate.exchange(uri, method, entity, Object.class);
    }

    private HttpHeaders getAuthenticationHeader() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = (String) authentication.getName();
        String password = (String) authentication.getCredentials();
        return HeaderUtil.basicAuth(username, password);
    }

    private String createPath(HttpServletRequest request) {
        String endpointPath = removeTrailingSlash(endpoint.getPath());
        String targetPath = request.getRequestURI();
        return endpointPath + targetPath;
    }

    private String removeTrailingSlash(String value) {
        return value.endsWith("/")
                ? value.substring(0, value.lastIndexOf("/"))
                : value;
    }
    
}
