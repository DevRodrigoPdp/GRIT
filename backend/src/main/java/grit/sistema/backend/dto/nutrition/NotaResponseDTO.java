package grit.sistema.backend.dto.nutrition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotaResponseDTO(
        UUID id,
        String texto,
        LocalDateTime fecha
) {
}
