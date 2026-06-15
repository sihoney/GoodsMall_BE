package com.example.payment.common.config;

import com.todaylunch.common.security.auth.config.CurrentMemberWebConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * payment 紐⑤뱢?먯꽌 {@code @CurrentMember} ?몄옄 ?댁꽍湲곕? ?ъ슜?섎룄濡?怨듯넻 Web MVC ?ㅼ젙??媛?몄삩?? */
@Configuration
@Import(CurrentMemberWebConfig.class)
public class WebMvcConfig {
}
