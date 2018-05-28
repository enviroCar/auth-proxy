
package org.envirocar.auth.proxy.security;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.http.ResponseEntity;

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

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * An {@link AuthenticationProvider} implementation that retrieves user details from a
 * {@link UserDetailsService}.
 *
 * @author Ben Alex
 * @author Rob Winch
 */
public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    // ~ Static fields/initializers
    // =====================================================================================

    /**
     * The plaintext password used to perform PasswordEncoder#matches(CharSequence, String)} on when the user
     * is not found to avoid SEC-2056.
     */
    private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";

    // ~ Instance fields
    // ================================================================================================

    private CustomAuthenticator authenticator;

    private PasswordEncoder passwordEncoder;

    /**
     * The password used to perform {@link PasswordEncoder#matches(CharSequence, String)} on when the user is
     * not found to avoid SEC-2056. This is necessary, because some {@link PasswordEncoder} implementations
     * will short circuit if the password is not in a valid format.
     */
    private volatile String userNotFoundEncodedPassword;

    public CustomAuthenticationProvider(CustomAuthenticator authenticator) {
        setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
        setAuthenticator(authenticator);
    }

    // ~ Methods
    // ========================================================================================================

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
                                                                  "Bad credentials"));
        }

        String presentedPassword = authentication.getCredentials().toString();

        if ( !passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            logger.debug("Authentication failed: password does not match stored value");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
                                                                  "Bad credentials"));
        }
    }

    @Override
    protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        prepareTimingAttackProtection();
        try {
            String password = authentication.getCredentials().toString();
            ResponseEntity<AuthenticatedUser> authenticationResponse = authenticator.authenticate(username, password);
            logger.debug("Authentication response: " + authenticationResponse.getBody());
            if (authenticationResponse.getStatusCode().value() == 401) {
                return new User("wrongUsername", "wrongPass", new ArrayList<GrantedAuthority>());
            }
            return new User(username, "{noop}" + password, Collections.emptyList());
        }
        catch (Exception ex) {
            throw new AuthenticationServiceException("Unable to authenticate user " + username, ex);
        }
    }

    private void prepareTimingAttackProtection() {
        if (this.userNotFoundEncodedPassword == null) {
            this.userNotFoundEncodedPassword = this.passwordEncoder.encode(USER_NOT_FOUND_PASSWORD);
        }
    }

    public void setAuthenticator(CustomAuthenticator authenticator) {
        Assert.notNull(authenticator, "authenticator cannot be null");
        this.authenticator = authenticator;
    }

    /**
     * Sets the PasswordEncoder instance to be used to encode and validate passwords. If not set, the password
     * will be compared using {@link PasswordEncoderFactories#createDelegatingPasswordEncoder()}
     *
     * @param passwordEncoder
     *        must be an instance of one of the {@code PasswordEncoder} types.
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
        this.passwordEncoder = passwordEncoder;
        this.userNotFoundEncodedPassword = null;
    }

    protected PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

}
