package grit.sistema.backend.validator;

import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.entity.coaching.enums.TitulacionEntrenamiento;
import grit.sistema.backend.entity.coaching.enums.TitulacionNutricion;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EntrenadorProfesionalValidator implements ConstraintValidator<ValidEntrenadorProfesional, EntrenadorRequestDTO> {

    @Override
    public boolean isValid(EntrenadorRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        // Lógica: Si es Grado en CAFYD o Grado en Nutrición, el código profesional NO puede estar vacío
        boolean esGradoEntrenamiento = dto.getTitulacionEntrenamiento() == TitulacionEntrenamiento.GRADO_CAFYD;
        boolean esGradoNutricion = dto.getTitulacionNutricion() == TitulacionNutricion.GRADO_NUTRICION_DIETETICA;

        boolean necesitaCodigo = esGradoEntrenamiento || esGradoNutricion;
        boolean tieneCodigo = dto.getCodigoProfesional() != null && !dto.getCodigoProfesional().isBlank();

        if (necesitaCodigo && !tieneCodigo) {
            // Personalizamos el error para que apunte al campo específico en Swagger/Frontend
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("codigoProfesional")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}