package grit.sistema.backend.validator.strategy;

import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.exception.business.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class ValidacionEntrenamientoStrategy implements ValidacionServicioStrategy {
    @Override
    public boolean aplicaA(TipoServicio servicio) { return servicio == TipoServicio.ENTRENAMIENTO; }

    @Override
    public void validar(Entrenador entrenador) {
        if (!entrenador.isTieneAccesoEntrenamiento()) {
            throw new BusinessException("COMPETENCIA_INSUFICIENTE", "El profesional no tiene título de entrenamiento.");
        }
    }
}
