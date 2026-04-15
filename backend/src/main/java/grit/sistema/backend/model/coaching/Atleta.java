package grit.sistema.backend.model.coaching;

import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.GeneroTipo;
import grit.sistema.backend.model.enums.NivelAtleta;
import grit.sistema.backend.model.enums.Objetivo;
import grit.sistema.backend.model.enums.TipoServicio;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
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

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @NotNull(message = "El género es obligatorio")
    @Column(name = "genero", columnDefinition = "genero_tipo")
    private GeneroTipo genero;

    @DecimalMin(value = "30.0", message = "El peso debe ser mayor a 30kg")
    @DecimalMax(value = "300.0", message = "El peso no puede exceder los 300kg")
    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @JdbcTypeCode(Types.SMALLINT)
    @Min(value = 100, message = "La altura mínima es 100cm")
    @Max(value = 250, message = "La altura máxima es 250cm")
    @Column(name = "altura_cm")
    private Integer alturaCm;

    @NotBlank(message = "El deporte es obligatorio")
    private String deporte;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @NotNull(message = "El nivel es obligatorio")
    @Column(name = "nivel", columnDefinition = "nivel_atleta", length = 20)
    private NivelAtleta nivel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @NotNull(message = "El servicio es obligatorio")
    @Column(name = "servicio", columnDefinition = "servicio_tipo", length = 20, nullable = false)
    private TipoServicio servicio;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "objetivo", columnDefinition = "objetivo_tipo", length = 20)
    private Objetivo objetivo;
}
