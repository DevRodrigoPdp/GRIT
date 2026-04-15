package grit.sistema.backend.dto.entrenador;

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
