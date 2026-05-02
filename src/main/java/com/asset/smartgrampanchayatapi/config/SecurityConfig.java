package com.asset.smartgrampanchayatapi.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.asset.smartgrampanchayatapi.web.filter.TenantCodeHeaderFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    TenantCodeHeaderFilter tenantCodeHeaderFilter() {
        return new TenantCodeHeaderFilter();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, TenantCodeHeaderFilter tenantCodeHeaderFilter) throws Exception {
        http.addFilterBefore(tenantCodeHeaderFilter, SecurityContextHolderFilter.class)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/test/**",
                                "/api/tenants/**",
                                "/api/citizens/**",
                                "/api/certificate-types/**"
                        ).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

    /** Allows the Angular dev server to call the API when not using a proxy. */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
