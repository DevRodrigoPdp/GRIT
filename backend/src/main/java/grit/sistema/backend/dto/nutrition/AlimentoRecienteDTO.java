package grit.sistema.backend.dto.nutrition;

import java.math.BigDecimal;

public record AlimentoRecienteDTO(
        String codigo,
        String nombre,
        String marca,
        BigDecimal kcalPor100g,
        BigDecimal proteinasPor100g,
        BigDecimal carbsPor100g,
        BigDecimal grasasPor100g
) {
}
