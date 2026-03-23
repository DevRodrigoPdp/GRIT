package com.sistema.gritfitprueba.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @Schema(example = "kevin@gritfit.com")
        @Email(message = "Email inválido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @Schema(example = "Password123!")
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
