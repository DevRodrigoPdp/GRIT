package grit.sistema.backend.dto.atleta;


public record AtletaResponseDTO(
        boolean ok,
        String message,
        AtletaData data
) {
}
