package com.antdevrealm.housechaosmain.config;

import com.antdevrealm.housechaosmain.auth.jwt.handler.RestAuthenticationEntryPoint;
import com.antdevrealm.housechaosmain.auth.service.HOCUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSec,
                                           RestAuthenticationEntryPoint restEntryPoint,
                                           HOCUserDetailsService hocUserDetailsService
                                           ) throws Exception {
        return httpSec.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(restEntryPoint))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(a ->
                        a.requestMatchers(HttpMethod.OPTIONS, "/**")
                                .permitAll()
                                .requestMatchers(
                                        "/images/**",
                                        "/api/products/**", // Allow endpoint for development and testing. Restrict in prod
                                        "/api/users/register",
                                        "/api/auth/login",
                                        "/api/auth/logout",
                                        "/api/auth/refresh",
                                        "/error")
                                .permitAll()
                                .anyRequest().authenticated())
                .userDetailsService(hocUserDetailsService)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return urlBasedCorsConfigurationSource;
    }
}
