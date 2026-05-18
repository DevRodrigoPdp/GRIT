package grit.sistema.backend.dto.coaching;


public record AtletaResponseDTO(
        boolean ok,
        String message,
        AtletaData data
) {
}
