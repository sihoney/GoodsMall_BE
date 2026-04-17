package com.example.payment.config;

import com.todaylunch.common.security.auth.config.CurrentMemberWebConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * payment 모듈에서 {@code @CurrentMember} 인자 해석기를 사용하도록 공통 Web MVC 설정을 가져온다
 */
@Configuration
@Import(CurrentMemberWebConfig.class)
public class WebMvcConfig {
}
