package grit.sistema.backend.dto.coaching;

import java.util.List;
import java.util.UUID;

public record EntrenadorPendienteDTO(
        UUID id,
        String nombre,
        String correo,
        String titulacionEntrenamiento,
        String titulacionNutricion,
        String codigoProfesional,
        java.time.Instant createdAt,
        List<DocumentoDTO> documentos
) {
}
