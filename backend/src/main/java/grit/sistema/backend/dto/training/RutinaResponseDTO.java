package grit.sistema.backend.dto.training;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RutinaResponseDTO(
        UUID id,
        OffsetDateTime creadoEn,
        UUID atletaId
) {
}
