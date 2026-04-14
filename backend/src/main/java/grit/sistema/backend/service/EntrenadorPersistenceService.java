package grit.sistema.backend.service;

import grit.sistema.backend.dto.entrenador.EntrenadorRequestDTO;
import grit.sistema.backend.mapper.EntrenadorMapper;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoUsuario;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.repositories.EntrenadorRepository;
import grit.sistema.backend.repositories.UsuarioRepository;
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
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;
    private final EntrenadorMapper entrenadorMapper;

    @Value("${application.security.pepper}")
    private String pepper;

    @Transactional
    public Entrenador guardarEntrenador(EntrenadorRequestDTO request, List<String> urls) {
        // Creación del Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword() + pepper));
        usuario.setRol(Rol.ENTRENADOR);
        usuario.setEstado(EstadoUsuario.PENDIENTE_REVISION);

        // Usamos save() normal; @Transactional se encarga del flush al final [cite: 17]
        usuario = usuarioRepository.save(usuario);

        // Mapeo y vinculación
        Entrenador entrenador = entrenadorMapper.toEntity(request, usuario, urls);
        entrenador.setUsuario(usuario);
        entrenador.setId(usuario.getId()); // Coherencia con @MapsId

        try {
            entrenador = entrenadorRepository.save(entrenador);
            entrenadorRepository.flush(); // Forzamos para que los triggers de DB actúen

            entityManager.refresh(entrenador); // Cargamos campos @Generated de Hibernate [cite: 14]
        } catch (Exception e) {
            log.error("Fallo crítico en DB para entrenador: {}", e.getMessage());
            throw e;
        }

        return entrenador;
    }
}
