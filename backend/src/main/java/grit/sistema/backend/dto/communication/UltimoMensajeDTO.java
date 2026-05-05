package grit.sistema.backend.dto.communication;

import java.time.LocalDateTime;

public record UltimoMensajeDTO(
        String texto,
        LocalDateTime fecha,
        String de
) {
}
