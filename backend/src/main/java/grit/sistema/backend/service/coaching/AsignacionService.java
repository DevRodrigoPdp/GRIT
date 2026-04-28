package grit.sistema.backend.service.coaching;

import grit.sistema.backend.exception.business.BusinessException;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor // Inyección por constructor automática [cite: 25, 26]
public class AsignacionService {

    private final EntrenadorRepository entrenadorRepo;
    private final AsignacionRepository asignacionRepo;
    private final AtletaRepository atletaRepository;

    @Transactional
    public void conectarConEntrenador(UUID atletaId, String codigo) {
        // 1. Obtener al Atleta y su servicio contratado
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new BusinessException("ATLETA_NO_ENCONTRADO", "El atleta no existe."));

        TipoServicio servicioRequerido = atleta.getServicio();

        // 2. Buscar entrenador activo por código de invitación
        // IMPORTANTE: Un entrenador debe estar en estado 'ACTIVO' (o REVISADO) para vincularse
        Entrenador entrenador = entrenadorRepo.findByCodigoInvitacion(codigo)
                .orElseThrow(() -> new BusinessException("CODIGO_INVALIDO", "El código de invitación no existe."));

        // 3. VALIDACIÓN DE TITULACIÓN (Regla de negocio core)
//        validarCompetenciaProfesional(entrenador, servicioRequerido);

        // 4. Verificar si ya existe una vinculación activa para ese servicio
        if (asignacionRepo.existsByAtletaIdAndTipoServicioAndActivaTrue(atletaId, servicioRequerido)) {
            throw new BusinessException("YA_VINCULADO", "Ya tienes un profesional para este servicio.");
        }

        // 5. Crear la asignación
        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setAtleta(atleta);
        nuevaAsignacion.setEntrenador(entrenador);
        nuevaAsignacion.setTipoServicio(servicioRequerido);
        nuevaAsignacion.setActiva(true);

        asignacionRepo.save(nuevaAsignacion);
    }

//    private void validarCompetenciaProfesional(Entrenador entrenador, TipoServicio servicio) {
//        boolean esApto = switch (servicio) {
//            case AMBOS -> entrenador.isTieneAccesoEntrenamiento() && entrenador.isTieneAccesoNutricion();
//            case ENTRENAMIENTO -> entrenador.isTieneAccesoEntrenamiento();
//            case NUTRICION -> entrenador.isTieneAccesoNutricion();
//            default -> false;
//        };
//
//        if (!esApto) {
//            throw new BusinessException("SIN_TITULACION",
//                    "El entrenador no cuenta con la titulación verificada para el servicio: " + servicio);
//        }
//    }
}
