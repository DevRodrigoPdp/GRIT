package grit.sistema.backend.dto.nutrition;

import java.util.List;
import java.util.UUID;

public record ComidaDTO(
        UUID id,
        String nombre,
        Short orden,
        List<AlimentoDTO> alimentos
) {
}
