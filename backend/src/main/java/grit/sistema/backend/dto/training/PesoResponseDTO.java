package grit.sistema.backend.dto.training;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PesoResponseDTO (
        UUID id,
        LocalDate fecha,
        BigDecimal pesoKg
){
}
