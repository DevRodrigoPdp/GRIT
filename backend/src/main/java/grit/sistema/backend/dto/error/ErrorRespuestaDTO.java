package grit.sistema.backend.dto.error;


import java.time.LocalDateTime;

public record ErrorRespuestaDTO(
        LocalDateTime fecha,
        String mensaje,
        String ruta,
        int codigo
) {
}
