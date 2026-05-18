package grit.sistema.backend.dto.nutrition;

import java.util.List;
import java.util.UUID;

public record PlanData(
        UUID id,
        String nombre,
        String descripcion,
        Integer kcalDiarias,
        Double proteinas,
        Double carbos,
        Double grasas,
        List<ComidaDTO> comidas
) {
}
