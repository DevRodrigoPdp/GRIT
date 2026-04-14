package grit.sistema.backend.dto.entrenador;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EntrenadorPendienteDTO(
        UUID id,
        String nombre,
        String correo,
        String titulacionEntrenamiento,
        String titulacionNutricion,
        String codigoProfesional,
        LocalDateTime uploaded_at,
        List<DocumentoDTO> documentos
) {
}
