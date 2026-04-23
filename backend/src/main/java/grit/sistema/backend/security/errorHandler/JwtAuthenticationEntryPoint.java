package grit.sistema.backend.security.errorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ProblemDetail pb = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Token inválido, expirado o inexistente. Debe autenticarse para acceder."
        );
        pb.setType(URI.create("https://api.GRIT.com/errors/unauthorized"));
        pb.setTitle("No Autenticado");
        pb.setInstance(URI.create(request.getRequestURI()));
        pb.setProperty("timestamp", LocalDateTime.now());

        response.getWriter().write(objectMapper.writeValueAsString(pb));
    }
}
