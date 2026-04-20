package grit.sistema.backend.dto.training;

import java.util.List;
import java.util.UUID;

public record SesionRutinaDTO(
        UUID id,
        String nombre,
        Short orden,
        List<EjercicioResponseDTO> ejercicios
) {
}
