package grit.sistema.backend.validator.strategy;

import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;

public interface ValidacionServicioStrategy {
    // Define a qué tipo de servicio aplica esta estrategia
    boolean aplicaA(TipoServicio servicio);

    // La lógica de validación específica
    void validar(Entrenador entrenador);
}
