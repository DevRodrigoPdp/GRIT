package grit.sistema.backend.config.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class EtagConfig {

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> shallowEtagHeaderFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> filterBean =
                new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        filterBean.addUrlPatterns(
                "/api/v1/atleta/perfil",
                "/api/v1/atleta/profesionales",
                "/api/v1/atleta/entrenamiento/plan-activo",
                "/api/v1/atleta/nutricion/plan-activo",
                "/api/v1/atleta/peso/solicitud-pendiente",
                "/api/v1/atleta/peso/historial",
                "/api/v1/entrenador/perfil",
                "/api/v1/entrenador/atletas/*/peso/pendiente",
                "/api/v1/entrenador/atletas/*/peso/historial",
                "/api/v1/entrenador/atletas",
                "/api/v1/comunicacion/atleta/hilos",
                "/api/v1/comunicacion/entrenador/atleta/*/hilos",
                "/api/v1/nutricion/planes",
                "/api/v1/entrenamiento/rutinas"
        );
        return filterBean;
    }
}
