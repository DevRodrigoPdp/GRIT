package grit.sistema.backend.security.filter;

import grit.sistema.backend.security.jwt.JwtUtils;
import grit.sistema.backend.security.jwt.TokenStoreService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final TokenStoreService tokenStoreService;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/registro/entrenador") ||
                path.equals("/api/v1/auth/registro/atleta")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.contains("/management") ||
                        path.contains("/swagger-ui") ||
                        path.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Extraer JWT de la cookie de forma limpia
        String jwt = null;
        if (request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "access_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // 2. Si no hay token, delegar a Spring Security (él decidirá si permite el paso o no)
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String ati = jwtUtils.extraerAti(jwt);

            if (ati != null && tokenStoreService.isReuseDetected(ati)) {
                log.warn("BLOQUEO: Intento de acceso con Access Token cuyo padre (ATI: {}) fue rotado/comprometido.", ati);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            String userEmail = jwtUtils.extraerEmail(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtils.esTokenValido(jwt, userDetails.getUsername())) {

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Usuario {} autenticado exitosamente", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("No se pudo establecer la autenticación de usuario: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
