package com.data7.instdesign.config;

import com.data7.instdesign.filter.LoginCheckFilter;
import com.data7.instdesign.filter.RememberMeFilter;
import com.data7.instdesign.service.AuthService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LoginCheckFilter> loginCheckFilter() {
        FilterRegistrationBean<LoginCheckFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new LoginCheckFilter());
        registrationBean.addUrlPatterns("/*"); // 전체 경로 적용
        registrationBean.setOrder(2); // RememberMeFilter보다 나중에 실행되도록 조절

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RememberMeFilter> rememberMeFilter(AuthService authService) {
        FilterRegistrationBean<RememberMeFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RememberMeFilter(authService));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // 먼저 실행됨

        return registrationBean;
    }
}

