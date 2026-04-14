package grit.sistema.backend.security.errorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import grit.sistema.backend.dto.error.ErrorRespuestaDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException
    {
        // Configuramos el JSON de error profesional
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                "Token invalido o expirado",
                request.getRequestURI(),
                401
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
