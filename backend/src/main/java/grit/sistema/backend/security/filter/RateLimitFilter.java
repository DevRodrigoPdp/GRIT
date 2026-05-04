package grit.sistema.backend.security.filter;

import grit.sistema.backend.exception.infrastructure.RateLimitException;
import grit.sistema.backend.entity.common.enums.RateLimitPlan;
import grit.sistema.backend.service.common.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final HandlerExceptionResolver resolver;

    public RateLimitFilter(RateLimitService rateLimitService,
                           @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.rateLimitService = rateLimitService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Identificar la clave (Usuario o IP)
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String key = (auth != null && auth.isAuthenticated()) ? auth.getName() : request.getRemoteAddr();

            // 2. Determinar el plan basado en Roles
            RateLimitPlan plan = RateLimitPlan.FREE;
            if (auth != null && !auth.getAuthorities().isEmpty()) {
                String role = auth.getAuthorities().iterator().next().getAuthority();
                plan = RateLimitPlan.resolvePlanFromRole(role);
            }

            // 3. LA CLAVE: Delegar totalmente en la interfaz
            // Ya no hay Buckets ni Probes aquí
            if (rateLimitService.tryConsume(key, plan)) {
                filterChain.doFilter(request, response);
            } else {
                throw new RateLimitException("Has superado el límite de peticiones para tu plan: " + plan.name());
            }

        } catch (RateLimitException e) {
            // Enviamos el error al manejador global de excepciones
            resolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/favicon.ico");
    }
}