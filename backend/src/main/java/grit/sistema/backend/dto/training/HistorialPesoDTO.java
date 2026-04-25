package grit.sistema.backend.dto.training;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record HistorialPesoDTO(
        UUID id,
        LocalDate fecha,
        BigDecimal pesoKg,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String solicitadoPor
) {
    public HistorialPesoDTO {
        if (pesoKg != null) {
            // BigDecimal requiere compareTo:
            // result < 0 significa menor que, > 0 significa mayor que
            if (pesoKg.compareTo(new BigDecimal("30")) < 0 ||
                    pesoKg.compareTo(new BigDecimal("300")) > 0) {
                throw new IllegalArgumentException("Peso fuera de rango (30-300kg)");
            }
        }
    }
}