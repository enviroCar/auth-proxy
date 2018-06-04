
package org.envirocar.auth.proxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer createCORSFilter() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] allowedMethods = {"*"};
                String[] exposedHeaders = {"*"};
                String[] allowedHeaders = {"*"};
                registry.addMapping("/*")
                        .allowedOrigins("*")
                        .allowedMethods(allowedMethods)
                        .exposedHeaders(exposedHeaders)
                        .allowedHeaders(allowedHeaders);
            };
        };
    }

}
