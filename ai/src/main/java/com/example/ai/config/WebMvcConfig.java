package com.example.ai.config;

import com.todaylunch.common.security.auth.config.CurrentMemberWebConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CurrentMemberWebConfig.class)
public class WebMvcConfig {
}

