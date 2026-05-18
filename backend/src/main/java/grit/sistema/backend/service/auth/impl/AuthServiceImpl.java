package grit.sistema.backend.service.auth.impl;

import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.auth.LoginRequestDTO;
import grit.sistema.backend.dto.auth.LoginResponseDTO;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginDto) {
        log.info(">>> Intentando autenticar usuario: {}", loginDto.email());

        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
        );

        // 2. Obtener el Principal (Adaptador)
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        // 3. Construir la data de respuesta directamente del principal
        // ¡Sin tocar la base de datos otra vez!
        LoginData data = new LoginData(
                principal.getRol().name(),
                principal.getUsername(),
                principal.getEstado().name(),
                principal.isTieneTituloNutricion(),
                principal.isTieneTituloEntrenamiento(),
                principal.getServicio() != null ? principal.getServicio().name() : null
        );

        log.info("<<< Autenticación exitosa para: {}", principal.getEmail());

        return new LoginResponseDTO(true, data);
    }
}
