package grit.sistema.backend.service.training;

import grit.sistema.backend.dto.training.RutinaDTO;
import grit.sistema.backend.dto.training.RutinaRequestDTO;
import grit.sistema.backend.dto.training.RutinaResponseDTO;
import grit.sistema.backend.mapper.training.EntrenamientoMapper;
import grit.sistema.backend.entity.training.Rutina;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.training.RutinaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntrenamientoService {

    private final RutinaRepository rutinaRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final EntrenamientoMapper mapper;

    @Transactional
    public RutinaResponseDTO crearRutina(UUID entrenadorId, RutinaRequestDTO request) {
        log.info("Iniciando creación de rutina para entrenador: {}", entrenadorId);

        // 1. Validaciones (Fail Fast)
        var atleta = atletaRepository.findById(request.atletaId())
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));
        var entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        // 2. Mapeo: El Mapper ya se encarga de la vinculación interna mediante @AfterMapping
        Rutina rutina = mapper.toEntity(request);

        // 3. Vincular raíces externas
        rutina.setAtleta(atleta);
        rutina.setEntrenador(entrenador);

        // 4. PERSISTENCIA
        // Al ser una entidad nueva (ID null), save() disparará el persist en cascada correctamente.
        Rutina guardada = rutinaRepository.save(rutina);

        return mapper.toResponseDTO(guardada);
    }

    @Transactional(readOnly = true) // Optimización para consultas
    public List<RutinaDTO> listarRutinas(UUID entrenadorId, UUID atletaId) {
        List<Rutina> rutinas = (atletaId == null)
                ? rutinaRepository.findAllByEntrenadorId(entrenadorId)
                : rutinaRepository.findAllByEntrenadorIdAndAtletaId(entrenadorId, atletaId);

        return rutinas.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public void eliminarRutina(UUID entrenadorId, UUID rutinaId) {
        // 1. Buscar la entidad o lanzar 404
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new EntityNotFoundException("La rutina no existe"));

        // 2. Validación de propiedad (Seguridad a nivel de datos)
        if (!rutina.getEntrenador().getId().equals(entrenadorId)) {
            log.warn("Intento de borrado no autorizado: Entrenador {} sobre rutina {}", entrenadorId, rutinaId);
            throw new AccessDeniedException("No tienes permisos para eliminar esta rutina");
        }

        // 3. Borrado en cascada (gestionado por CascadeType.ALL en la entidad)
        rutinaRepository.delete(rutina);
        log.info("Rutina {} eliminada correctamente", rutinaId);
    }

    @Transactional(readOnly = true)
    public Optional<RutinaDTO> getPlanEntrenamientoActivoAtleta(UUID atletaId) {
        return rutinaRepository.findByAtletaIdAndActivoTrue(atletaId)
                .map(mapper::toDTO);
    }

}
