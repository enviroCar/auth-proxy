package org.envirocar.auth.proxy.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.web.client.HttpStatusCodeException;

public class UnavailableForLegalReasonsException extends AccountStatusException {
    private final HttpHeaders headers;
    private final String content;

    public UnavailableForLegalReasonsException(HttpStatusCodeException cause) {
        super(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS.getReasonPhrase(), cause);
        this.headers = cause.getResponseHeaders();
        this.content = cause.getResponseBodyAsString();
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }
}
