package grit.sistema.backend.validator.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EntrenadorProfesionalValidator.class)
@Documented
public @interface ValidEntrenadorProfesional {
    String message() default "El código profesional es obligatorio para titulaciones de grado (CAFYD o Nutrición)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}