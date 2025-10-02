package com.antdevrealm.housechaosmain.infrastructure.security.config;

import com.antdevrealm.housechaosmain.infrastructure.security.handler.RestAuthenticationEntryPoint;
import com.antdevrealm.housechaosmain.infrastructure.security.jwt.filter.JwtAuthFilter;
import com.antdevrealm.housechaosmain.infrastructure.security.service.HOCUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSec,
                                           RestAuthenticationEntryPoint restEntryPoint,
                                           HOCUserDetailsService hocUserDetailsService,
                                           JwtAuthFilter jwtAuthFilter) throws Exception {
        return httpSec.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(restEntryPoint))
                .authorizeHttpRequests(a -> a.requestMatchers("/users/register", "/users/login", "/users/free", "/error").permitAll()
                        .anyRequest().authenticated())
                .userDetailsService(hocUserDetailsService)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
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
}
