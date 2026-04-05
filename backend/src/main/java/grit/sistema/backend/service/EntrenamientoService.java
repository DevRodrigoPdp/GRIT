package grit.sistema.backend.service;

import grit.sistema.backend.dto.EntrenamientoRequestDTO;
import grit.sistema.backend.dto.EntrenamientoResponseDTO;
import grit.sistema.backend.exception.AccesoDenegadoException;
import grit.sistema.backend.mapper.EntrenamientoMapper;
import grit.sistema.backend.model.Entrenamiento;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.repositories.EntrenamientoRepository;
import grit.sistema.backend.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrenamientoService {
    @PersistenceContext
    private EntityManager entityManager;

    private final EntrenamientoRepository entrenamientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntrenamientoMapper entrenamientoMapper;

    public EntrenamientoResponseDTO guardarEntrenamiento(EntrenamientoRequestDTO request, String email){
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Entrenamiento entrenamiento = entrenamientoMapper.toEntity(request, usuario);
        Entrenamiento guardado = entrenamientoRepository.save(entrenamiento);

        return entrenamientoMapper.toResponse(guardado);
    }

    public List<EntrenamientoResponseDTO> listarEntrenamientosPorUsuario(String email){
        return entrenamientoRepository.findAllByUsuarioEmail(email)
                .stream()
                .map(entrenamientoMapper::toResponse)
                .toList();
    }

    @Transactional
    public void eliminarEntrenamientoPorUuid(String uuid, String emailUsuarioAutenticado){
        entityManager.clear();

        Entrenamiento entrenamiento = entrenamientoRepository.findByUuidConUsuario(UUID.fromString(uuid))
                .orElseThrow(() -> new RuntimeException("Entrenamiento no encontrado"));

        String emailPropietario = entrenamiento.getUsuario().getEmail();

        log.info("Intento de borrado: Dueño[{}] - Solicitante[{}]", emailPropietario, emailUsuarioAutenticado);

        if (!emailPropietario.equalsIgnoreCase(emailUsuarioAutenticado)) {
            throw new AccesoDenegadoException("No tienes permiso para borrar este entrenamiento. No te pertenece.");
        }

        entrenamientoRepository.delete(entrenamiento);
    }
}
