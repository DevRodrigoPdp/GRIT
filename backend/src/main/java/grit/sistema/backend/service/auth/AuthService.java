package grit.sistema.backend.service.auth;

import grit.sistema.backend.dto.auth.LoginRequestDTO;
import grit.sistema.backend.dto.auth.LoginResponseDTO;

public interface AuthService {
    /**
     * Gestiona el proceso de autenticación Stateless.
     * @param loginDto Datos de acceso (email/password).
     * @return DTO con la información de sesión y roles.
     */
    LoginResponseDTO login(LoginRequestDTO loginDto);
}
