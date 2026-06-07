package grit.sistema.backend.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class MDCFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String USER_KEY = "user";
    private static final String RESPONSE_HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Generar el UUID único para esta petición
        String traceId = UUID.randomUUID().toString().substring(0, 8); // 8 caracteres suelen bastar para buscar

        try {
            // 2. Poner el traceId en el MDC
            MDC.put(TRACE_ID_KEY, traceId);

            // 3. Obtener el usuario (si existe)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
            MDC.put(USER_KEY, username);

            // 4. Añadir el traceId a la respuesta HTTP para que el Frontend lo vea
            response.addHeader(RESPONSE_HEADER_TRACE_ID, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // 5. Limpieza absoluta
            MDC.clear();
        }
    }
}