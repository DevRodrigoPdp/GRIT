package grit.sistema.backend.service;

import grit.sistema.backend.dto.entrenador.EntrenadorRequestDTO;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.EntrenadorMapper;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoRevision;
import grit.sistema.backend.model.enums.EstadoUsuario;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.repository.EntrenadorRepository;
import grit.sistema.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrenadorPersistenceService {
    private final UsuarioRepository usuarioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntrenadorMapper entrenadorMapper;

    @Transactional
    public Entrenador guardarEntrenador(EntrenadorRequestDTO request, List<String> urls) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }

        Entrenador entrenador = entrenadorMapper.toEntity(request, urls);

        entrenador.setPassword(passwordEncoder.encode(request.getPassword()));
        entrenador.setRol(Rol.ENTRENADOR);
        entrenador.setEstado(EstadoUsuario.ACTIVO);

        // IMPORTANTE: Vincular los documentos al entrenador (Relación bidireccional)
        if (entrenador.getDocumentos() != null) {
            entrenador.getDocumentos().forEach(doc -> doc.setEntrenador(entrenador));
        }

        // 4. Persistencia única
        return entrenadorRepository.save(entrenador);
    }
}
