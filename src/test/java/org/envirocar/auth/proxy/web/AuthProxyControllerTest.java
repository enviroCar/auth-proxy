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
