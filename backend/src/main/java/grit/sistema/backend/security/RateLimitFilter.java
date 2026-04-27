package grit.sistema.backend.security;

import grit.sistema.backend.exception.RateLimitException;
import grit.sistema.backend.entity.common.enums.RateLimitPlan;
import grit.sistema.backend.service.RateLimitService;
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
    private final HandlerExceptionResolver resolver; // Añade esto

    // Inyectamos el resolver de Spring MVC
    public RateLimitFilter(RateLimitService rateLimitService,
                           @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.rateLimitService = rateLimitService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String key = (auth != null && auth.isAuthenticated()) ? auth.getName() : request.getRemoteAddr();

            RateLimitPlan plan = RateLimitPlan.FREE;
            if (auth != null && !auth.getAuthorities().isEmpty()) {
                plan = RateLimitPlan.resolvePlanFromRole(auth.getAuthorities().iterator().next().getAuthority());
            }

            Bucket bucket = rateLimitService.resolveBucket(key, plan);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                // Headers informativos (Importante para CORS más abajo)
                response.addHeader("X-RateLimit-Limit", String.valueOf(plan.getLimit().getCapacity()));
                response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                // Lanzamos la excepción personalizada
                throw new RateLimitException("Demasiadas peticiones. Límite: " + plan.name());
            }
        } catch (RateLimitException e) {
            // ESTA ES LA MAGIA: Enviamos el error al HandlerExceptionResolver
            resolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Excluimos explícitamente Swagger y recursos estáticos
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/favicon.ico");
    }
}