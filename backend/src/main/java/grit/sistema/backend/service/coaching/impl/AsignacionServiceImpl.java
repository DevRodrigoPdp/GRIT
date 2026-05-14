package grit.sistema.backend.service.coaching.impl;

import grit.sistema.backend.dto.coaching.AsignacionRequestDTO;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;
import grit.sistema.backend.exception.business.BusinessException;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.service.coaching.AsignacionService;
import grit.sistema.backend.validator.strategy.ValidacionServicioStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service("asignacionService")
@RequiredArgsConstructor
@Slf4j
public class AsignacionServiceImpl implements AsignacionService {
    private final EntrenadorRepository entrenadorRepo;
    private final AsignacionRepository asignacionRepo;
    private final AtletaRepository atletaRepository;
    private final List<ValidacionServicioStrategy> estrategias;

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
        if (asignacionRepo.existsByAtletaIdAndEntrenadorIdAndActivaTrue(atletaId, entrenador.getId())) {
            throw new BusinessException("ALREADY_LINKED", "Ya estás vinculado con este profesional.");
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
        Asignacion asignacion = asignacionRepo.findByEntrenadorIdAndAtletaId(entrenadorId, atletaId)
                .orElseThrow(() -> new EntityNotFoundException("No existe un vínculo activo entre este entrenador y el atleta"));

        asignacion.setActiva(false);

        asignacionRepo.save(asignacion);
    }
}
