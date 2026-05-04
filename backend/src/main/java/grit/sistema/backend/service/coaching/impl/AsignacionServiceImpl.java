package grit.sistema.backend.service.coaching.impl;

import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.exception.business.BusinessException;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.service.coaching.AsignacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsignacionServiceImpl implements AsignacionService {
    private final EntrenadorRepository entrenadorRepo;
    private final AsignacionRepository asignacionRepo;
    private final AtletaRepository atletaRepository;

    @Override
    @Transactional
    public void conectarConEntrenador(UUID atletaId, String codigo) {
        log.info("Intento de conexión: Atleta {} con código {}", atletaId, codigo);

        // 1. Validaciones de existencia (Fail-Fast)
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new BusinessException("ATLETA_NO_ENCONTRADO", "El atleta no existe."));

        Entrenador entrenador = entrenadorRepo.findByCodigoInvitacion(codigo)
                .orElseThrow(() -> new BusinessException("CODIGO_INVALIDO", "Código de invitación no válido."));

        // 2. Validaciones de estado y competencia
        if (!EstadoRevision.APROBADO.equals(entrenador.getEstadoRevision())) {
            throw new BusinessException("ENTRENADOR_NO_VERIFICADO", "El profesional no está habilitado.");
        }

        validarCompetenciaProfesional(entrenador, atleta.getServicio());

        // 3. Validaciones de negocio (Estado actual)
        if (asignacionRepo.existsByAtletaIdAndEntrenadorIdAndActivaTrue(atletaId, entrenador.getId())) {
            throw new BusinessException("ALREADY_LINKED", "Ya estás vinculado con este profesional.");
        }

        if (asignacionRepo.existsByAtletaIdAndTipoServicioAndActivaTrue(atletaId, atleta.getServicio())) {
            throw new BusinessException("SERVICIO_ACTIVO", "Ya tienes un profesional activo para este servicio.");
        }

        try {
            Asignacion nuevaAsignacion = new Asignacion();
            nuevaAsignacion.setAtleta(atleta);
            nuevaAsignacion.setEntrenador(entrenador);
            nuevaAsignacion.setTipoServicio(atleta.getServicio());
            nuevaAsignacion.setActiva(true);

            asignacionRepo.save(nuevaAsignacion);
            asignacionRepo.flush();

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Error de integridad al conectar atleta {}: {}", atletaId, e.getMessage());
            throw new BusinessException("CONFLICTO_ASIGNACION",
                    "No se pudo procesar la asignación. Es posible que ya tengas un servicio activo.");
        }
    }

    private void validarCompetenciaProfesional(Entrenador entrenador, TipoServicio servicio) {
        boolean esApto = switch (servicio) {
            case AMBOS -> entrenador.isTieneAccesoEntrenamiento() && entrenador.isTieneAccesoNutricion();
            case ENTRENAMIENTO -> entrenador.isTieneAccesoEntrenamiento();
            case NUTRICION -> entrenador.isTieneAccesoNutricion();
        };

        if (!esApto) {
            throw new BusinessException("COMPETENCIA_INSUFICIENTE",
                    "El entrenador no está facultado para el servicio: " + servicio);
        }
    }
}
