package grit.sistema.backend.service.training;

import grit.sistema.backend.dto.training.*;
import grit.sistema.backend.entity.training.Ejercicio;
import grit.sistema.backend.entity.training.EjercicioEnSesion;
import grit.sistema.backend.entity.training.SesionRutina;
import grit.sistema.backend.mapper.training.EntrenamientoMapper;
import grit.sistema.backend.entity.training.Rutina;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.training.EjercicioRepository;
import grit.sistema.backend.repository.training.RutinaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntrenamientoService {

    private final RutinaRepository rutinaRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final EjercicioRepository ejercicioRepository;
    private final EntrenamientoMapper mapper;

    @Transactional
    public RutinaResponseDTO crearRutina(UUID entrenadorId, RutinaRequestDTO request) {
        // 1. Validaciones
        var atleta = atletaRepository.findById(request.atletaId()).orElseThrow();
        var entrenador = entrenadorRepository.findById(entrenadorId).orElseThrow();

        // 2. Carga masiva del catálogo (Optimización)
        Set<UUID> idsEjercicios = request.sesiones().stream()
                .flatMap(s -> s.ejercicios().stream())
                .map(EjercicioRequestDTO::ejercicioId)
                .collect(Collectors.toSet());
        Map<UUID, Ejercicio> catalogo = ejercicioRepository.findAllById(idsEjercicios).stream()
                .collect(Collectors.toMap(Ejercicio::getId, e -> e));

        // 3. Construcción Jerárquica usando el Mapper
        Rutina rutina = new Rutina();
        rutina.setNombre(request.nombre());
        rutina.setDescripcion(request.descripcion());
        rutina.setAtleta(atleta);
        rutina.setEntrenador(entrenador);
        rutina.setActivo(request.activo());

        for (var sDto : request.sesiones()) {
            SesionRutina sesion = new SesionRutina();
            sesion.setNombre(sDto.nombre());
            sesion.setOrden(sDto.orden().shortValue());

            for (var eDto : sDto.ejercicios()) {
                Ejercicio maestro = catalogo.get(eDto.ejercicioId());
                if (maestro == null) throw new EntityNotFoundException("Ejercicio no existe");

                // LLAMADA AL MAPPER: Mezcla el DTO con el Maestro
                EjercicioEnSesion detalle = mapper.toEjercicioEnSesion(eDto, maestro);
                sesion.addEjercicio(detalle);
            }
            rutina.addSesion(sesion);
        }

        return mapper.toResponseDTO(rutinaRepository.save(rutina));
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public void activarRutina(UUID entrenadorId, UUID rutinaId) {
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new EntityNotFoundException("Rutina no encontrada"));

        // Validación de propiedad
        validarPropiedad(entrenadorId, rutina);

        // Desactivar cualquier otra y activar esta
        desactivarRutinaActual(rutina.getAtleta().getId());
        rutina.setActivo(true);
        rutinaRepository.save(rutina);
    }

    private void desactivarRutinaActual(UUID atletaId) {
        rutinaRepository.findByAtletaIdAndActivoTrue(atletaId)
                .ifPresent(r -> {
                    r.setActivo(false);
                    rutinaRepository.save(r);
                    log.info("Rutina anterior {} desactivada", r.getId());
                });
    }


    private void validarPropiedad(UUID entrenadorId, Rutina rutina) {
        if (!rutina.getEntrenador().getId().equals(entrenadorId)) {
            throw new AccessDeniedException("No tienes permiso sobre esta rutina");
        }
    }

}
