package grit.sistema.backend.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ProblemDetail pb = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "No tiene los privilegios necesarios para acceder a este recurso."
        );
        pb.setType(URI.create("https://api.GRIT.com/errors/forbidden"));
        pb.setTitle("Acceso Denegado");
        pb.setInstance(URI.create(request.getRequestURI()));
        pb.setProperty("timestamp", LocalDateTime.now());

        response.getWriter().write(objectMapper.writeValueAsString(pb));
    }
}
