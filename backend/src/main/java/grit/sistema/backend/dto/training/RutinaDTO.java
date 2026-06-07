package grit.sistema.backend.dto.training;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RutinaDTO(
        UUID id,
        UUID atletaId,
        String nombre,
        String descripcion,
        OffsetDateTime creadoEn,
        List<SesionRutinaDTO> sesiones
) {
}
