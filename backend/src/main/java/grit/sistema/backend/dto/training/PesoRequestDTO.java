package grit.sistema.backend.dto.training;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PesoRequestDTO (
        @NotNull(message = "El ID de la solicitud es obligatorio")
        UUID solicitudId,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "30.0", message = "El peso mínimo permitido es 30kg")
        @DecimalMax(value = "300.0", message = "El peso máximo permitido es 300kg")
        BigDecimal pesoKg
){
}
