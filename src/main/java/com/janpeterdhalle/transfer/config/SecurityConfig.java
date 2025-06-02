package com.janpeterdhalle.transfer.config;

import com.janpeterdhalle.transfer.services.UserDetailsService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.ResourceUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    @Value("${cors_urls}")
    private List<String> corsUrls;

    @Value("${jwt.public_file}")
    private String publicKeyPath;

    @Value("${jwt.private_file}")
    private String privateKeyPath;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
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
                   .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                   .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                   .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                   .logout(logout -> logout.invalidateHttpSession(true)
                                           .deleteCookies("JSESSIONID")
                                           .permitAll())
                   .userDetailsService(userDetailsService)
                   .build();
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

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);

        return provider;
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        String cleanedKey = null;
        cleanedKey = Files.readString(ResourceUtils.getFile(publicKeyPath).toPath(), StandardCharsets.UTF_8)
                          .replace("-----BEGIN PUBLIC KEY-----", "")
                          .replace("-----END PUBLIC KEY-----", "")
                          .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(cleanedKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        String privateKeyPEM = null;
        privateKeyPEM = Files.readString(ResourceUtils.getFile(privateKeyPath).toPath(), StandardCharsets.UTF_8)
                             .replace("-----BEGIN PRIVATE KEY-----", "")
                             .replace("-----END PRIVATE KEY-----", "")
                             .replaceAll("\\s+", "");

        byte[] encoded = Base64.getMimeDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        RSAKey key = new RSAKey.Builder(rsaPublicKey()).privateKey(rsaPrivateKey()).build();
        ImmutableJWKSet<SecurityContext> keys = new ImmutableJWKSet<>(new JWKSet(key));
        return new NimbusJwtEncoder(keys);
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
