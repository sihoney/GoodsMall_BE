package com.todaylunch.common.security.auth.config;

import com.todaylunch.common.security.auth.resolver.CurrentMemberArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CurrentMemberWebConfig implements WebMvcConfigurer {

    @Bean
    public CurrentMemberArgumentResolver currentMemberArgumentResolver() {
        return new CurrentMemberArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMemberArgumentResolver());
    }
}
