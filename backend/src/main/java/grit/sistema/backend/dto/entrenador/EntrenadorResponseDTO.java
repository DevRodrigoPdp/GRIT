package grit.sistema.backend.dto.entrenador;

import grit.sistema.backend.entity.coaching.enums.EstadoRevision;
import grit.sistema.backend.entity.common.enums.Rol;

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
