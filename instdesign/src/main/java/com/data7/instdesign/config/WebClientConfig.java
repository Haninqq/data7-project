package com.data7.instdesign.config;

import com.data7.instdesign.filter.LoginCheckFilter;
import com.data7.instdesign.filter.RememberMeFilter;
import com.data7.instdesign.service.AuthService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

