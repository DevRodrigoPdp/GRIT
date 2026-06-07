package grit.sistema.backend.security.config;

import grit.sistema.backend.security.filter.CsrfCookieFilter;
import grit.sistema.backend.security.filter.JwtAuthenticationFilter;
import grit.sistema.backend.security.filter.MDCFilter;
import grit.sistema.backend.security.filter.RateLimitFilter;
import grit.sistema.backend.security.handler.CustomAccessDeniedHandler;
import grit.sistema.backend.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final AuthenticationProvider authenticationProvider;
    private final RateLimitFilter rateLimitFilter;
    private final MDCFilter mdcFilter;
    private final CsrfCookieFilter csrfCookieFilter;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final Environment env;

    @Value("${application.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> {
                    if (isDev) {
                        csrf.disable();
                    } else {
                        csrf
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                .csrfTokenRequestHandler(requestHandler)
                                .ignoringRequestMatchers("/api/v1/diagnostic/**", "/management/**")
                                .ignoringRequestMatchers(SWAGGER_WHITELIST);
                    }
                })
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/api/v1/auth/**",
                            "/api/v1/diagnostic/**",
                            "/management/**",
                            "/error"
                    ).permitAll();

                    if (isDev) {
                        auth.requestMatchers(SWAGGER_WHITELIST).permitAll();
                        log.info("Swagger UI habilitado en SecurityFilterChain (Perfil DEV)");
                    }

                    auth.requestMatchers(HttpMethod.GET, "/api/v1/usuarios/perfil").authenticated()
                            .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/entrenador/**").hasAnyRole("ENTRENADOR")
                            .requestMatchers("/api/v1/atleta/**").hasAnyRole("ATLETA")
                            .requestMatchers("/api/v1/entrenamiento/**").hasAnyRole("ENTRENADOR")
                            .requestMatchers("/api/v1/nutricion/**").hasAnyRole("ENTRENADOR")
                            .requestMatchers("/api/v1/comunicacion/**").hasAnyRole("ENTRENADOR", "ATLETA", "ADMIN")
                            .anyRequest().authenticated();
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(mdcFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(csrfCookieFilter, MDCFilter.class)
                .addFilterAfter(jwtAuthFilter, CsrfCookieFilter.class)
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("https://frontend-rho-two-49.vercel.app/", "http://localhost:4200"));

        // Métodos permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Cabeceras permitidas (JWT)
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-XSRF-TOKEN",
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
