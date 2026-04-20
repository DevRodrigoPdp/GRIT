package grit.sistema.backend.dto.nutrition;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RecetaResponseDTO(
        UUID id,
        String nombre,
        BigDecimal gramosTotal,
        OffsetDateTime creadoEn,
        List<IngredienteRecetaResponseDTO> ingredientes
) {
}
