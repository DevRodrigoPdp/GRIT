package grit.sistema.backend.security.config;

import grit.sistema.backend.security.filter.MDCFilter;
import grit.sistema.backend.security.filter.RateLimitFilter;
import grit.sistema.backend.security.handler.CustomAccessDeniedHandler;
import grit.sistema.backend.security.handler.JwtAuthenticationEntryPoint;
import grit.sistema.backend.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final AuthenticationProvider authenticationProvider;
    private final RateLimitFilter rateLimitFilter;
    private final MDCFilter mdcFilter;

    @Value("${application.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth-> auth
                        // 1. Recursos totalmente públicos (Swagger, Auth, Health)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/auth/**",
                                "/api/v1/diagnostic/**",
                                "/management/**"
                        ).permitAll()

                        // 3. Lógica de negocio específica
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/perfil").authenticated()
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/entrenador/**").hasAnyRole("ENTRENADOR", "ADMIN")
                        .requestMatchers("/api/v1/atleta/**").hasAnyRole("ATLETA", "ADMIN")
                        .requestMatchers("/api/v1/entrenamiento/**").hasAnyRole("ENTRENADOR", "ADMIN")
                        .requestMatchers("/api/v1/nutricion/**").hasAnyRole("ENTRENADOR", "ADMIN")

                        // 4. Todo lo demás requiere estar autenticado
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(mdcFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthFilter, MDCFilter.class)
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));

        // Métodos permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Cabeceras permitidas (JWT)
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Cache-Control",
                "X-Requested-With",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Retry-After",
                "X-Trace-Id",
                "Accept"
        ));

        // Permitir que el cliente acceda a ciertas cabeceras si fuera necesario
        config.setExposedHeaders(List.of("Authorization", "X-Trace-Id"));

        // Permitir envío de cookies/credenciales si fuera necesario
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
