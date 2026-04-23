package grit.sistema.backend.dto.nutrition;

import java.math.BigDecimal;
import java.util.UUID;

public record AlimentoRecienteDTO(
        UUID id,
        String codigo,
        String nombre,
        String marca,
        BigDecimal kcalPor100g,
        BigDecimal proteinasPor100g,
        BigDecimal carbsPor100g,
        BigDecimal grasasPor100g
) {
}
