package grit.sistema.backend.dto.coaching;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentoDTO(
        UUID id,
        String nombreArchivo,
        String urlFirmada,
        OffsetDateTime uploadedAt,
        String status
) {
}
