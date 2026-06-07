package grit.sistema.backend.validator.annotation;

import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.entity.coaching.enums.TitulacionEntrenamiento;
import grit.sistema.backend.entity.coaching.enums.TitulacionNutricion;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EntrenadorProfesionalValidator implements ConstraintValidator<ValidEntrenadorProfesional, EntrenadorRequestDTO> {

    @Override
    public boolean isValid(EntrenadorRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean isValid = true;
        // Desactivamos el mensaje por defecto para poner mensajes específicos por campo
        context.disableDefaultConstraintViolation();

        // REGLA 1: Al menos una titulación es obligatoria
        boolean tieneEntrenamiento = dto.getTitulacionEntrenamiento() != null;
        boolean tieneNutricion = dto.getTitulacionNutricion() != null;

        if (!tieneEntrenamiento && !tieneNutricion) {
            context.buildConstraintViolationWithTemplate("Debe seleccionar al menos una titulación (Entrenamiento o Nutrición)")
                    .addPropertyNode("titulacionEntrenamiento") // Apuntamos a un campo para el frontend
                    .addConstraintViolation();
            isValid = false;
        }

        // REGLA 2: Si es Grado, el código profesional es obligatorio
        boolean esGradoEntrenamiento = dto.getTitulacionEntrenamiento() == TitulacionEntrenamiento.GRADO_CAFYD;
        boolean esGradoNutricion = dto.getTitulacionNutricion() == TitulacionNutricion.GRADO_NUTRICION_DIETETICA;

        boolean necesitaCodigo = esGradoEntrenamiento || esGradoNutricion;
        boolean tieneCodigo = dto.getCodigoProfesional() != null && !dto.getCodigoProfesional().isBlank();

        if (necesitaCodigo && !tieneCodigo) {
            context.buildConstraintViolationWithTemplate("El código profesional es obligatorio para titulaciones de Grado")
                    .addPropertyNode("codigoProfesional")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}