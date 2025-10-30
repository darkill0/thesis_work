package org.example.restapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.stream.Stream;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource())) // Use custom CORS configuration
                .authorizeHttpRequests(configurer ->
                        configurer.requestMatchers("/notify").permitAll()
                        .requestMatchers("/keycloak").permitAll()//
                                .requestMatchers("/uploads/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                        .anyRequest().authenticated() // Остальные запросы требуют аутентификации

                )
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(configurer -> configurer.jwt(jwt -> {
                    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                    jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);

                    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                    JwtGrantedAuthoritiesConverter customJwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                    customJwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("groups");
                    customJwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(token ->
                            Stream.concat(jwtGrantedAuthoritiesConverter.convert(token).stream(),
                                            customJwtGrantedAuthoritiesConverter.convert(token).stream())
                                    .toList());
                }))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Use specific origins in production
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        System.out.println("CORS Filter applied with settings: " + config); // Логирование
        return source;
    }
}
