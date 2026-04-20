package grit.sistema.backend.dto.nutrition;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlanNutricionResponseDTO(
        UUID id,
        OffsetDateTime creadoEn
) {
}
