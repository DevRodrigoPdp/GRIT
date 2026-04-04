package grit.sistema.backend.dto;

import grit.sistema.backend.model.enums.NivelAtleta;
import grit.sistema.backend.model.enums.TipoServicio;

import java.util.UUID;

public record AtletaResponseDTO(
        boolean ok,
        String message,
        AtletaData data
) {
}
