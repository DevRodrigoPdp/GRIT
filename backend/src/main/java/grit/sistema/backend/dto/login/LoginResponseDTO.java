package grit.sistema.backend.dto.login;

public record LoginResponseDTO(
        boolean ok,
        LoginData data
) {
}
