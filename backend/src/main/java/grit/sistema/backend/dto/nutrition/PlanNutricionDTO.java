package grit.sistema.backend.dto.nutrition;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PlanNutricionDTO(
        UUID id,
        UUID atletaId,
        String nombre,
        String descripcion,
        OffsetDateTime creadoEn,
        List<ComidaDTO> comidas
) {
}
