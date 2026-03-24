package grit.sistema.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EntrenamientoResponseDTO(
        String uuid,
        String nombre,
        String descripcion,
        LocalDateTime fechaEntrenamiento,
        Integer duracionMinutos,
        List<EjercicioReponseDTO> ejercicios
) {
}
