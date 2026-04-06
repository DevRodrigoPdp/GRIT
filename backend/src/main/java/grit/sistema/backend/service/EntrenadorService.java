package grit.sistema.backend.service;

import grit.sistema.backend.dto.EntrenadorRequestDTO;
import grit.sistema.backend.dto.EntrenadorResponseDTO;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.EntrenadorMapper;
import grit.sistema.backend.model.Entrenador;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoUsuario;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import grit.sistema.backend.repositories.EntrenadorRepository;
import grit.sistema.backend.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrenadorService {
    private final UsuarioRepository usuarioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final EntrenadorMapper entrenadorMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EntrenadorResponseDTO registrarEntrenador(EntrenadorRequestDTO request) {
        validarRequisitosProfesionales(request);

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }

        // 1. Crear y PERSISTIR el Usuario primero
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.ENTRENADOR);
        usuario.setEstado(EstadoUsuario.PENDIENTE_REVISION);

        usuario = usuarioRepository.saveAndFlush(usuario);

        // 2. Procesar documentos
        List<String> urls = (request.getDocumentos() == null) ? List.of() : request.getDocumentos().stream()
                .map(file -> "MOCK_S3_PATH/" + System.currentTimeMillis() + "_" + file.getOriginalFilename())
                .toList();

        Entrenador entrenador = entrenadorMapper.toEntity(request, usuario, urls);

        // VINCULACIÓN MANUAL CRÍTICA:
        entrenador.setUsuario(usuario);
        entrenador.setId(usuario.getId()); // Aseguramos que el ID coincida antes de entrar al repo


        try {
            entrenador = entrenadorRepository.save(entrenador);
        } catch (Exception e) {
            log.error("Fallo al guardar entrenador: {}", e.getMessage());
            throw e;
        }

        log.info("Registro exitoso: Entrenador con ID {} guardado", entrenador.getId());
        return entrenadorMapper.toResponse(entrenador);
    }

    private void validarRequisitosProfesionales(EntrenadorRequestDTO request) {
        if (request.getTitulacionEntrenamiento() == null && request.getTitulacionNutricion() == null) {
            throw new IllegalArgumentException("Debe tener al menos una titulación.");
        }

        boolean requiereColegiado = (request.getTitulacionEntrenamiento() == TitulacionEntrenamiento.GRADO_CAFYD) ||
                (request.getTitulacionNutricion() == TitulacionNutricion.GRADO_NUTRICION_DIETETICA);

        if (requiereColegiado && (request.getCodigoProfesional() == null || request.getCodigoProfesional().isBlank())) {
            throw new IllegalArgumentException("El código profesional es obligatorio para titulaciones de grado");
        }
    }
}