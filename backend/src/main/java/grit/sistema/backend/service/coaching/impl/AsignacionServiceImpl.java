package grit.sistema.backend.service.coaching.impl;

import grit.sistema.backend.dto.coaching.AsignacionRequestDTO;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.exception.business.BusinessException;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.nutrition.PlanNutricionRepository;
import grit.sistema.backend.repository.training.RutinaRepository;
import grit.sistema.backend.service.coaching.AsignacionService;
import grit.sistema.backend.validator.strategy.ValidacionServicioStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("asignacionService")
@RequiredArgsConstructor
@Slf4j
public class AsignacionServiceImpl implements AsignacionService {
    private final EntrenadorRepository entrenadorRepo;
    private final AsignacionRepository asignacionRepo;
    private final PlanNutricionRepository planNutricionRepo;
    private final RutinaRepository rutinaRepo;
    private final AtletaRepository atletaRepository;
    private final List<ValidacionServicioStrategy> estrategias;
    private final CacheManager cacheManager;

    private static final String CACHE_PLAN_ACTIVO = "planEntrenamientoActivo";
    private static final String CACHE_NUTRICION_ACTIVA = "planNutricionActivo";

    @Override
    @Transactional
    public void conectarConEntrenador(UUID atletaId, AsignacionRequestDTO request) {
        log.info("Intento de conexión: Atleta {} con código {}", atletaId, request.codigo());

        // 1. Validaciones de existencia (Fail-Fast)
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new BusinessException("ATLETA_NO_ENCONTRADO", "El atleta no existe."));

        Entrenador entrenador = entrenadorRepo.findByCodigoInvitacion(request.codigo())
                .orElseThrow(() -> new BusinessException("CODIGO_INVALIDO", "Código de invitación no válido."));

        // 2. Validaciones de estado y competencia
        if (!EstadoRevision.APROBADO.equals(entrenador.getEstadoRevision())) {
            throw new BusinessException("ENTRENADOR_NO_VERIFICADO", "El profesional no está habilitado.");
        }

        estrategias.stream()
                .filter(s -> s.aplicaA(request.rolSolicitado()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("ESTRATEGIA_NO_ENCONTRADA",
                        "No se encontró lógica de validación para: " + request.rolSolicitado()))
                .validar(entrenador);

        // 3. Validaciones de negocio (Estado actual)
        if (asignacionRepo.existsByAtletaIdAndEntrenadorIdAndActivaTrueAndTipoServicio(atletaId, entrenador.getId(), request.rolSolicitado())) {
            throw new BusinessException("ALREADY_LINKED", "Ya estás vinculado con este profesional para este servicio.");
        }

        if (asignacionRepo.existsByAtletaIdAndTipoServicioAndActivaTrue(atletaId, request.rolSolicitado())) {
            throw new BusinessException("SERVICIO_ACTIVO", "Ya tienes un profesional activo para este servicio.");
        }

        try {
            Asignacion nuevaAsignacion = new Asignacion();
            nuevaAsignacion.setAtleta(atleta);
            nuevaAsignacion.setEntrenador(entrenador);
            nuevaAsignacion.setTipoServicio(request.rolSolicitado());
            nuevaAsignacion.setActiva(true);

            asignacionRepo.save(nuevaAsignacion);
            asignacionRepo.flush();

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Error de integridad al conectar atleta {}: {}", atletaId, e.getMessage());
            throw new BusinessException("CONFLICTO_ASIGNACION",
                    "No se pudo procesar la asignación. Es posible que ya tengas un servicio activo.");
        }
    }

    @Transactional(readOnly = true)
    public boolean esEntrenadorDeAtleta(UUID entrenadorId, UUID atletaId) {
        return asignacionRepo.existsByAtletaIdAndEntrenadorIdAndActivaTrue(atletaId, entrenadorId);
    }

    @Override
    @Transactional
    public void terminarAsignacion(UUID entrenadorId, UUID atletaId) {
        Asignacion asignacion = asignacionRepo.findFirstByEntrenadorIdAndAtletaIdOrderByCreadaEnDesc(entrenadorId, atletaId)
                .orElseThrow(() -> new EntityNotFoundException("No existe un vínculo activo entre este entrenador y el atleta"));

        asignacion.setActiva(false);

        asignacionRepo.save(asignacion);

        TipoServicio servicio = asignacion.getTipoServicio();

        if (servicio == TipoServicio.ENTRENAMIENTO || servicio == TipoServicio.AMBOS) {
            rutinaRepo.desactivarRutinasActivas(entrenadorId, atletaId);
            evictCache(CACHE_PLAN_ACTIVO, atletaId);
        }

        if (servicio == TipoServicio.NUTRICION || servicio == TipoServicio.AMBOS) {
            planNutricionRepo.desactivarPlanesNutricionActivos(entrenadorId, atletaId);
            evictCache(CACHE_NUTRICION_ACTIVA, atletaId);
        }
    }

    private void evictCache(String cacheName, UUID atletaId) {
        Optional.ofNullable(cacheManager.getCache(cacheName))
                .ifPresent(cache -> {
                    cache.evict(atletaId);
                    log.debug("Caché '{}' invalidada para la clave: {}", cacheName, atletaId);
                });
    }
}
