package com.sistema.gritfitprueba.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponseDTO(
        @Schema(description = "Token de acceso JWT para peticiones autenticadas")
        String token,
        @Schema(example = "kevin@gritfit.com")
        UsuarioDTO usuario
) {
}
