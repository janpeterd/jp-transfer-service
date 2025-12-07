package com.janpeterdhalle.transfer.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${cors_urls}")
    private List<String> corsUrls;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(
                        (authorize) -> authorize
                                .requestMatchers(HttpMethod.GET, "/").permitAll()
                                .requestMatchers(HttpMethod.GET, "/explorer/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/download").permitAll()
                                .requestMatchers(HttpMethod.GET, "/download/**").permitAll()
                                .requestMatchers("/api/transfer/uuid/*").permitAll()
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers(
                                        "/auth/refresh-token",
                                        "/auth/register",
                                        "/auth/login",
                                        "/auth/login",
                                        "/logout")
                                .permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/v3/**").permitAll()
                                .requestMatchers("/auth/set-password").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/users").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/users").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/users").hasAuthority("ADMIN")
                                .anyRequest().authenticated()) // All other requests require authentication
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .logout(logout -> logout.invalidateHttpSession(true).deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsUrls);

        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
