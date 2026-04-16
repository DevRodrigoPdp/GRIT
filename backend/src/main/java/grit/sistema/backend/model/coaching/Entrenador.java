package grit.sistema.backend.model.coaching;

import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoRevision;
import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entrenadores")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Entrenador extends Usuario {

    @Column(name = "codigo_profesional", unique = true, length = 20)
    private String codigoProfesional;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "titulacion_entrenamiento")
    private TitulacionEntrenamiento titulacionEntrenamiento;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "titulacion_nutricion")
    private TitulacionNutricion titulacionNutricion;

    @Column(name = "experiencia_anos")
    private Short experienciaAnos;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado_revision")
    private EstadoRevision estadoRevision = EstadoRevision.PENDIENTE_REVISION;


    @OneToMany(mappedBy = "entrenador", cascade = CascadeType.ALL)
    private List<DocumentoEntrenador> documentos;
}
