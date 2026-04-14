package grit.sistema.backend.model.coaching;

import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.NivelAtleta;
import grit.sistema.backend.model.enums.Objetivo;
import grit.sistema.backend.model.enums.TipoServicio;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;

@Entity
@Table(name = "atletas")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Atleta extends Usuario {

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Column(name = "fecha_nac", nullable = false)
    private LocalDate fechaNac;

    @NotBlank(message = "El género es obligatorio")
    @Column(length = 20)
    private String genero;

    @DecimalMin(value = "30.0", message = "El peso debe ser mayor a 30kg")
    @DecimalMax(value = "300.0", message = "El peso no puede exceder los 300kg")
    @Column(name = "peso_kg", columnDefinition = "numeric(5,2)")
    private Double pesoKg;

    @JdbcTypeCode(Types.SMALLINT)
    @Min(value = 100, message = "La altura mínima es 100cm")
    @Max(value = 250, message = "La altura máxima es 250cm")
    @Column(name = "altura_cm")
    private Integer alturaCm;

    @NotBlank(message = "El deporte es obligatorio")
    private String deporte;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El nivel es obligatorio")
    @Column(length = 20)
    private NivelAtleta nivel;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El servicio es obligatorio")
    @Column(length = 20, nullable = false)
    private TipoServicio servicio;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Objetivo objetivo;
}
