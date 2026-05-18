package grit.sistema.backend.dto.communication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MensajeDTO(
        UUID id,
        String texto,
        String de, // "ENTRENADOR" | "ATLETA"
        LocalDateTime fecha,
        List<AdjuntoDTO> adjuntos
) {
}
