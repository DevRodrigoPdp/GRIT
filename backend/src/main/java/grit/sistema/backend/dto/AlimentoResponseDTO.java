package grit.sistema.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para un alimento.
 * El campo 'id' se serializa como 'codigo' en el JSON para cumplir con la especificación.
 */

public record AlimentoResponseDTO(
        @JsonProperty("codigo") UUID id,
        String nombre,
        String marca,
        String categoria,
        BigDecimal kcalPor100g,
        BigDecimal proteinasPor100g,
        BigDecimal carbsPor100g,
        BigDecimal grasasPor100g
) {}
