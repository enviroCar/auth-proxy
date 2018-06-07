
package org.envirocar.auth.proxy;

import org.envirocar.auth.proxy.web.AuthProxyController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/logout-success").setViewName("logout-success");
        registry.addViewController("/hello").setViewName("hello");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/").setViewName("index");
        WebMvcConfigurer.super.addViewControllers(registry);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping(AuthProxyController.PATH_PREFIX + "/**")
                        .allowCredentials(true)
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowedOrigins("*");
            }

        };
    }

}
