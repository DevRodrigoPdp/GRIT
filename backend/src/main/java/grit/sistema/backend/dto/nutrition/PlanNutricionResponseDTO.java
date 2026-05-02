package grit.sistema.backend.dto.nutrition;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PlanNutricionResponseDTO(
        UUID id,
        UUID atletaId,
        String nombre,
        String descripcion,
        boolean activo,
        OffsetDateTime creadoEn,
        List<ComidaResponseDTO> comidas
) {
}
