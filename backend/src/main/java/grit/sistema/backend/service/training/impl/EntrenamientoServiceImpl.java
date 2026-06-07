package grit.sistema.backend.service.training.impl;

import grit.sistema.backend.dto.coaching.AtletaBajaEventDTO;
import grit.sistema.backend.dto.coaching.EntrenadorBajaEventDTO;
import grit.sistema.backend.dto.training.EjercicioRequestDTO;
import grit.sistema.backend.dto.training.RutinaDTO;
import grit.sistema.backend.dto.training.RutinaRequestDTO;
import grit.sistema.backend.dto.training.RutinaResponseDTO;
import grit.sistema.backend.entity.training.Ejercicio;
import grit.sistema.backend.entity.training.EjercicioEnSesion;
import grit.sistema.backend.entity.training.Rutina;
import grit.sistema.backend.entity.training.SesionRutina;
import grit.sistema.backend.exception.security.AccesoDenegadoException;
import grit.sistema.backend.mapper.training.EntrenamientoMapper;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.training.EjercicioRepository;
import grit.sistema.backend.repository.training.RutinaRepository;
import grit.sistema.backend.service.training.EntrenamientoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrenamientoServiceImpl implements EntrenamientoService {
    private final RutinaRepository rutinaRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final EjercicioRepository ejercicioRepository;
    private final EntrenamientoMapper mapper;
    private final CacheManager cacheManager;

    private static final String CACHE_PLAN_ACTIVO = "planEntrenamientoActivo";

    @Override
    @Transactional
    public RutinaResponseDTO crearRutina(UUID entrenadorId, RutinaRequestDTO request) {
        var atleta = atletaRepository.findById(request.atletaId()).orElseThrow();
        var entrenador = entrenadorRepository.findById(entrenadorId).orElseThrow();

        Set<UUID> idsEjercicios = request.sesiones().stream()
                .flatMap(s -> s.ejercicios().stream())
                .map(EjercicioRequestDTO::ejercicioId)
                .collect(Collectors.toSet());
        Map<UUID, Ejercicio> catalogo = ejercicioRepository.findAllById(idsEjercicios).stream()
                .collect(Collectors.toMap(Ejercicio::getId, e -> e));

        log.info("IDs enviados desde el front: {}", idsEjercicios);
        log.info("IDs encontrados en la base de datos: {}", catalogo.keySet());

        if (catalogo.size() != idsEjercicios.size()) {
            List<UUID> faltantes = idsEjercicios.stream()
                    .filter(id -> !catalogo.containsKey(id))
                    .toList();
            throw new EntityNotFoundException("Error de integridad: Los siguientes IDs de ejercicios no existen en el catálogo: " + faltantes);
        }

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

                EjercicioEnSesion detalle = mapper.toEjercicioEnSesion(eDto, maestro);
                sesion.addEjercicio(detalle);
            }
            rutina.addSesion(sesion);
        }

        evictPlanActivo(request.atletaId());

        return mapper.toResponseDTO(rutinaRepository.save(rutina));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinaResponseDTO> listarRutinas(UUID entrenadorId, UUID atletaId) {
        List<Rutina> rutinas = (atletaId == null)
                ? rutinaRepository.findAllByEntrenadorIdOrderByCreadoEnDesc(entrenadorId)
                : rutinaRepository.findAllByEntrenadorIdAndAtletaIdOrderByCreadoEnDesc(entrenadorId, atletaId);

        return rutinas.stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarRutina(UUID entrenadorId, UUID rutinaId) {
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new EntityNotFoundException("Rutina no existe"));

        validarPropiedad(entrenadorId, rutina);
        UUID atletaId = rutina.getAtleta().getId();

        rutinaRepository.delete(rutina);

        evictPlanActivo(atletaId);
        log.info("Rutina {} eliminada y caché de atleta {} limpiada", rutinaId, atletaId);
    }

    @Override
    @Transactional
    public RutinaResponseDTO actualizarRutina(UUID entrenadorId, UUID planId, RutinaRequestDTO request) {
        Rutina rutinaExistente = rutinaRepository.findByIdAndEntrenadorId(planId, entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Rutina no encontrada"));

        rutinaExistente.setNombre(request.nombre());
        rutinaExistente.setDescripcion(request.descripcion());

        if (request.activo() && !rutinaExistente.isActivo()) {
            rutinaRepository.findByAtletaIdAndActivoTrue(rutinaExistente.getAtleta().getId())
                    .filter(p -> !p.getId().equals(planId))
                    .ifPresent(p -> p.setActivo(false));
        }
        rutinaExistente.setActivo(request.activo());

        Rutina datosNuevos = mapper.toEntity(request);

        rutinaExistente.getSesiones().clear();

        if (datosNuevos.getSesiones() != null) {
            datosNuevos.getSesiones().forEach(nuevaSesion -> {
                nuevaSesion.setRutina(rutinaExistente);
                rutinaExistente.getSesiones().add(nuevaSesion);
            });
        }

        Rutina guardada = rutinaRepository.save(rutinaExistente);

        evictPlanActivo(guardada.getAtleta().getId());

        return mapper.toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PLAN_ACTIVO, key = "#atletaId")
    public Optional<RutinaDTO> getPlanEntrenamientoActivoAtleta(UUID atletaId) {
        return rutinaRepository.findByAtletaIdAndActivoTrue(atletaId)
                .map(mapper::toDTO);
    }

    @Override
    @Transactional
    public void activarRutina(UUID entrenadorId, UUID rutinaId) {
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new EntityNotFoundException("Rutina no encontrada"));

        validarPropiedad(entrenadorId, rutina);
        UUID atletaId = rutina.getAtleta().getId();

        evictPlanActivo(atletaId);

        rutinaRepository.desactivarRutinasActivas(atletaId);
        rutina.setActivo(true);

        rutinaRepository.saveAndFlush(rutina);

        log.info("Rutina {} activada para el atleta {}", rutinaId, rutina.getAtleta().getId());
    }

    @Override
    @Transactional
    public void desactivarRutina(UUID entrenadorId, UUID rutinaId) {
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new EntityNotFoundException("Rutina no encontrada"));

        if (!rutina.getEntrenador().getId().equals(entrenadorId)) {
            throw new AccessDeniedException("No tienes permiso para modificar esta rutina");
        }

        if (!rutina.isActivo()) {
            log.info("La rutina {} ya se encuentra desactivada.", rutinaId);
            return;
        }

        rutina.setActivo(false);
        rutinaRepository.save(rutina);

        evictPlanActivo(rutina.getAtleta().getId());

        log.info("Rutina {} desactivada por el entrenador {}", rutinaId, entrenadorId);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAtletaBaja(AtletaBajaEventDTO event) {
        rutinaRepository.desactivarRutinasActivas(event.atletaId());
        evictPlanActivo(event.atletaId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEntrenadorBaja(EntrenadorBajaEventDTO event) {
        rutinaRepository.desactivarRutinasPorEntrenador(event.entrenadorId());
    }

    private void validarPropiedad(UUID entrenadorId, Rutina rutina) {
        if (!rutina.getEntrenador().getId().equals(entrenadorId)) {
            throw new AccesoDenegadoException("No tienes permiso sobre esta rutina");
        }
    }

    private void evictPlanActivo(UUID atletaId) {
        Optional.ofNullable(cacheManager.getCache(CACHE_PLAN_ACTIVO))
                .ifPresent(c -> c.evict(atletaId));
    }
}
