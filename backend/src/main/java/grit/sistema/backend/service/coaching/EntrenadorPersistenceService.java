package grit.sistema.backend.service.coaching;

import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.exception.business.UsuarioExistenteException;
import grit.sistema.backend.mapper.coaching.EntrenadorMapper;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.user.UsuarioRepository;
import grit.sistema.backend.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrenadorPersistenceService {
    private final UsuarioRepository usuarioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntrenadorMapper entrenadorMapper;
    private final CodeGenerator codeGenerator;

    @Transactional
    public Entrenador guardarEntrenador(EntrenadorRequestDTO request, List<String> urls, List<MultipartFile> certificaciones, String fotoKey) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }

        Entrenador entrenador = entrenadorMapper.toEntity(request, urls, certificaciones);

        String nuevoCodigo = codeGenerator.generateGritFormat();

        entrenador.setPassword(passwordEncoder.encode(request.getPassword()));
        entrenador.setRol(Rol.ENTRENADOR);
        entrenador.setEstado(EstadoUsuario.ACTIVO);
        entrenador.setFotoUrl(fotoKey);
        entrenador.setCodigoInvitacion(nuevoCodigo);
        entrenador.setMasters(request.getMasters());

        // IMPORTANTE: Vincular los documentos al entrenador (Relación bidireccional)
        if (entrenador.getDocumentos() != null) {
            entrenador.getDocumentos().forEach(doc -> doc.setEntrenador(entrenador));
        }

        // 4. Persistencia única
        return entrenadorRepository.save(entrenador);
    }
}
