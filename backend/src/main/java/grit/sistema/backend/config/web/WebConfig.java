package grit.sistema.backend.config.web;

import grit.sistema.backend.security.logging.RequestLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLogInterceptor requestLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",      // Evitamos loguear intentos de login (por seguridad)
                        "/v3/api-docs/**",      // Swagger docs
                        "/swagger-ui/**",       // Interfaz de Swagger
                        "/swagger-resources/**"
                );
    }
}
