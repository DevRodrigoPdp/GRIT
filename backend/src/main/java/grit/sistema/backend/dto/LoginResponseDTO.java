package grit.sistema.backend.dto;

public record LoginResponseDTO(
        boolean ok,
        LoginData data
) {
}
