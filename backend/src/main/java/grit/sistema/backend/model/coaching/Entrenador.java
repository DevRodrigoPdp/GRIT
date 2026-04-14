package grit.sistema.backend.model.coaching;

import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoRevision;
import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "codigo_profesional", unique = true)
    private String codigoProfesional;

    @Enumerated(EnumType.STRING)
    @Column(name = "titulacion_entrenamiento")
    private TitulacionEntrenamiento titulacionEntrenamiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "titulacion_nutricion")
    private TitulacionNutricion titulacionNutricion;

    @Enumerated(EnumType.STRING)
    private EstadoRevision estado = EstadoRevision.PENDIENTE_REVISION;


    @OneToMany(mappedBy = "entrenador", cascade = CascadeType.ALL)
    private List<DocumentoEntrenador> documentos;
}
