package grit.sistema.backend.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
public class CsrfCookieFilter extends OncePerRequestFilter {

    private static final String[] EXCLUDED_PATHS = {
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return Arrays.stream(EXCLUDED_PATHS).anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            // Al acceder a .getToken(), disparamos la generación del valor si no existe
            String token = csrfToken.getToken();
            // Lo enviamos como cabecera para asegurar que el interceptor de Angular lo vea
            response.setHeader("X-XSRF-TOKEN", token);
        }
        filterChain.doFilter(request, response);
    }
}