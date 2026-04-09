package grit.sistema.backend.dto;


public record AtletaResponseDTO(
        boolean ok,
        String message,
        AtletaData data
) {
}
