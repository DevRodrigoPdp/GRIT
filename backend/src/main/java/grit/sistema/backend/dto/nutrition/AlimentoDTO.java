package grit.sistema.backend.dto.nutrition;

import java.math.BigDecimal;
import java.util.UUID;

public record AlimentoDTO(
        UUID id,
        String codigo,
        String nombre,
        String marca,
        BigDecimal cantidadG,
        Integer kcal,
        Double proteinas,
        Double carbos,
        Double grasas
) {
}
