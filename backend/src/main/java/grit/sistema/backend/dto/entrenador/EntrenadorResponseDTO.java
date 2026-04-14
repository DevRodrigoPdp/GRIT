package grit.sistema.backend.dto.entrenador;

import grit.sistema.backend.model.enums.EstadoRevision;
import grit.sistema.backend.model.enums.Rol;

import java.util.UUID;

public record EntrenadorResponseDTO(
        UUID id,
        String nombre,
        Rol rol,
        EstadoRevision estado,
        Boolean tituloEntrenamiento,
        Boolean tituloNutricion
) {
}
