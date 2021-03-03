package org.envirocar.auth.proxy.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof UnavailableForLegalReasonsException) {
            writeException(response, (UnavailableForLegalReasonsException) authException);
        } else {
            response.addHeader("WWW-Authenticate", "x-Basic realm=\"" + this.getRealmName() + "\"");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }
    }

    private void writeException(HttpServletResponse response, UnavailableForLegalReasonsException exception)
            throws IOException {
        MediaType contentType = exception.getHeaders().getContentType();
        if (contentType != null) {
            response.setHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());
        }
        response.setStatus(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS.value());
        try (OutputStream os = response.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write(exception.getContent());
        }
    }
}
