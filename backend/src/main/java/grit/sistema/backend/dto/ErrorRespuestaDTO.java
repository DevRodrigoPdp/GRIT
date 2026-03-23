package com.sistema.gritfitprueba.dto;


import java.time.LocalDateTime;

public record ErrorRespuestaDTO(
        LocalDateTime fecha,
        String mensaje,
        String ruta,
        int codigo
) {
}
