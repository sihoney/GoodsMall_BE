package com.example.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Security filter chain is intentionally omitted in member-service.
    /* public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.httpBasic(HttpBasicConfigurer::disable)
            // cors
            .cors(configurer -> {
                CorsConfiguration corsConfigurer = new CorsConfiguration();
                corsConfigurer.setAllowedOriginPatterns(List.of("*"));
                corsConfigurer.setAllowedMethods(
                    Arrays.asList(HttpMethod.POST.name(), HttpMethod.GET.name(), HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(), HttpMethod.PATCH.name()));
                corsConfigurer.addAllowedHeader("*");
                corsConfigurer.setAllowCredentials(true);
                corsConfigurer.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", corsConfigurer);
                configurer.configurationSource(source);
            })
            // csrf
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(FormLoginConfigurer::disable)
            // 인가
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/v1/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/v3/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().permitAll();
            });
        return httpSecurity.build();
    } */
}
