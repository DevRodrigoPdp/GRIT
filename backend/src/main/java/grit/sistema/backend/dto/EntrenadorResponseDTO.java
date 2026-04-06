package grit.sistema.backend.dto;

import grit.sistema.backend.model.enums.EstadoRevision;
import grit.sistema.backend.model.enums.Rol;

import java.util.UUID;

public record EntrenadorResponseDTO(
        UUID id,
        String nombre,
        Rol rol,
        EstadoRevision estado,
        boolean tituloEntrenamiento,
        boolean tituloNutricion

) {
}
