package grit.sistema.backend.dto.nutrition;

import java.math.BigDecimal;

public record IngredienteRecetaResponseDTO(
        String codigo,
        String nombre,
        BigDecimal kcalPor100g,
        BigDecimal cantidadG,
        Short orden
) {
}
