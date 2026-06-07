package grit.sistema.backend.dto.training;

import java.util.UUID;

public record EjercicioResponseDTO(
        UUID id,
        String ejercicioId,
        String ejercicioNombre,
        Short series,
        String reps,
        String notas,
        Short orden
) {
}
