package grit.sistema.backend.validator.strategy;

import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.exception.business.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class ValidacionAmbosStrategy implements ValidacionServicioStrategy {
    @Override
    public boolean aplicaA(TipoServicio servicio) { return servicio == TipoServicio.AMBOS; }

    @Override
    public void validar(Entrenador entrenador) {
        if (!entrenador.isTieneAccesoEntrenamiento() || !entrenador.isTieneAccesoNutricion()) {
            throw new BusinessException("COMPETENCIA_INSUFICIENTE", "El profesional requiere ambos títulos.");
        }
    }
}
