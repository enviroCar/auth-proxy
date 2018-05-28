
package org.envirocar.auth.proxy;

import org.envirocar.auth.proxy.security.CustomAuthenticationProvider;
import org.envirocar.auth.proxy.security.CustomAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Configuration
    protected static class LoginConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        private AuthenticationProvider authenticationProvider;
        

        @Bean
        @Override
        public AuthenticationManager authenticationManager() throws Exception {
            return super.authenticationManager();
        }
        
        @Bean
        public AuthenticationProvider authenticationProvider(CustomAuthenticator authenticator) {
            return new CustomAuthenticationProvider(authenticator);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider)
                .eraseCredentials(false);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/**")
            .authorizeRequests()
//            .antMatchers("/login**", "/logout", "/oauth/authorize", "/oauth/confirm_access", "/error**")
//            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic();
            
            // https://stackoverflow.com/a/49201780
//            http.headers()
//                .httpStrictTransportSecurity()
//                .disable();
////                    .includeSubDomains(true)
////                    .maxAgeInSeconds(0);
        }

    }

}
