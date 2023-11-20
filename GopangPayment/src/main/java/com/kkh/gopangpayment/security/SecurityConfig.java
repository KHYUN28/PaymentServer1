package com.kkh.gopangpayment.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            // HTTP 기본 인증 비활성화
            .httpBasic(basic -> basic.disable())
            // CORS(Cross-Origin Resource Sharing) 설정을 기본값으로 설정
            .cors(Customizer.withDefaults())
            // CSRF(Cross-Site Request Forgery) 보호 기능 비활성화
            .csrf(csrf -> csrf.disable());
            // 요청에 대한 인증 및 권한 설정)
    // 보안 필터 체인 설정 완료 후 반환
    return httpSecurity.build();
  }
}
