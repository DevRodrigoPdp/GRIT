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
import org.springframework.web.multipart.MultipartFile;

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

        if(usuarioRepository.existsByEmail(request.email())){
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }
        if(request.codigoProfesional() != null && !request.codigoProfesional().isBlank()){
            if (entrenadorRepository.existsByCodigoProfesional(request.codigoProfesional())) {
                throw new UsuarioExistenteException("CODIGO_COLEGIADO_DUPLICADO");
            }
        }

        // 3. Crear el Usuario base (Inactivo hasta revisión)
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.ENTRENADOR);
        usuario.setEstado(EstadoUsuario.PENDIENTE_REVISION);
        usuario = usuarioRepository.save(usuario);
//
///       // 4. Subir documentos a S3
//        // folder: "documentos/entrenadores/{email}"
//        List<String> urls = request.documentos().stream()
//                .map(file -> s3Service.uploadFile(file, "verificaciones/" + request.correo()))
//                .toList();
        List<String> urls = request.documentos().stream()
                .map(file -> "MOCK_S3_PATH/" + System.currentTimeMillis() + "_" + file.getOriginalFilename())
                .toList();

        Entrenador entrenador = entrenadorMapper.toEntity(request, usuario, urls);
        entrenador = entrenadorRepository.save(entrenador);

        log.info("Registro exitoso: Entrenador {} pendiente de validación", usuario.getEmail());

        return entrenadorMapper.toResponse(entrenador);
    }

    private void validarRequisitosProfesionales(EntrenadorRequestDTO request) {
        if (request.titulacionEntrenamiento() == null && request.titulacionNutricion() == null ){
            throw new IllegalArgumentException("Debe tener al menos una titulación en entrenamiento o nutrición.");
        }

        boolean esGradoEntrenamiento = request.titulacionEntrenamiento() == TitulacionEntrenamiento.GRADO_CAFYD;
        boolean esGradoNutricion = request.titulacionNutricion() == TitulacionNutricion.GRADO_NUTRICION_DIETETICA;

        if ((esGradoEntrenamiento || esGradoNutricion) &&
        (request.codigoProfesional() == null || request.codigoProfesional().isBlank())){
            throw new IllegalArgumentException("El código profesional es obligatorio para titulaciones de grado");
        }

    }
}
