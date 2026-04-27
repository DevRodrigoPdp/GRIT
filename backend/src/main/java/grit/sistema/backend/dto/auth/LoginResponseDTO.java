package grit.sistema.backend.dto.auth;

public record LoginResponseDTO(
        boolean ok,
        LoginData data
) {
}
