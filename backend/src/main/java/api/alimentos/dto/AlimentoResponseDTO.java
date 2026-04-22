package api.alimentos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para un alimento.
 * El campo 'id' se serializa como 'codigo' en el JSON para cumplir con la especificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlimentoResponseDTO {

    @JsonProperty("codigo")
    private UUID id;

    private String nombre;

    private String marca;

    @JsonProperty("kcalPor100g")
    private BigDecimal kcalPor100g;

    @JsonProperty("proteinasPor100g")
    private BigDecimal proteinasPor100g;

    @JsonProperty("carbsPor100g")
    private BigDecimal carbsPor100g;

    @JsonProperty("grasasPor100g")
    private BigDecimal grasasPor100g;
}
