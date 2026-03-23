package com.sistema.gritfitprueba.service;

import com.sistema.gritfitprueba.dto.AuthResponseDTO;
import com.sistema.gritfitprueba.dto.LoginRequestDTO;
import com.sistema.gritfitprueba.dto.RegistroRequestDTO;
import com.sistema.gritfitprueba.dto.UsuarioDTO;
import com.sistema.gritfitprueba.mapper.UsuarioMapper;
import com.sistema.gritfitprueba.model.Usuario;
import com.sistema.gritfitprueba.model.enums.Rol;
import com.sistema.gritfitprueba.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toDTO).toList();
    }

    public UsuarioDTO guardar(UsuarioDTO usuarioDTO) {
        log.info("Iniciando creación de usuario para: {}", usuarioDTO.email());

        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        usuario.setPassword(passwordEncoder.encode(usuarioDTO.password()));

        Usuario guardado = usuarioRepository.save(usuario);

        return usuarioMapper.toDTO(guardado);
    }

    public UsuarioDTO findByUuid(UUID uuid) {
        Usuario usuario = usuarioRepository.findByUuid(uuid).orElseThrow(() -> new RuntimeException("Usuario no encontrado con el UUID: " + uuid));

        return usuarioMapper.toDTO(usuario);
    }

    public AuthResponseDTO login(LoginRequestDTO loginDto) {
        log.info("Iniciando cuenta para login: {}", loginDto.email());
        /*Usamos el mismo mensaje ("Credenciales inválidas") tanto si el email no existe como si la contraseña es errónea. Así no le damos pistas a un atacante sobre qué parte falló.*/
        Usuario usuario = usuarioRepository.findByEmail(loginDto.email()).orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        log.info("Usuario encontrado, verificando password...");

        if (!passwordEncoder.matches(loginDto.password(), usuario.getPassword())) {
            log.warn("Password incorrecta para el usuario: {}", loginDto.email());
            throw new RuntimeException("Credenciales inválidas");
        }

        String miTokenGenerado = jwtService.generarToken(usuario.getEmail(),usuario.getRol().name());

        UsuarioDTO usuarioDTO = usuarioMapper.toDTO(usuario);

        log.info("Token generado con éxito: {}", miTokenGenerado);
        return new AuthResponseDTO(miTokenGenerado, usuarioDTO);
    }

    public void registrar(RegistroRequestDTO registroDto) {
        if (usuarioRepository.existsByEmail(registroDto.email())) {
            throw new RuntimeException("Usuario existente");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(registroDto.username());
        usuario.setEmail(registroDto.email());
        usuario.setPassword(passwordEncoder.encode(registroDto.password()));
        usuario.setRol(Rol.USER);
        usuarioRepository.save(usuario);
    }

    public UsuarioDTO obtenerUsuarioActual() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return usuarioMapper.toDTO(usuario);
    }
}
