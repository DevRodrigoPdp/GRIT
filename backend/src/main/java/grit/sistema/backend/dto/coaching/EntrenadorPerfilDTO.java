package grit.sistema.backend.dto.coaching;

import java.util.UUID;

public record EntrenadorPerfilDTO(
        UUID id,
        String nombre,
        String correo,
        String titulacionEntrenamiento,
        String titulacionNutricion,
        Short experienciaAnos,
        String descripcion,
        String estado
) {
}
