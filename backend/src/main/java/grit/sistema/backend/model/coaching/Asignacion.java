package grit.sistema.backend.model.coaching;

import grit.sistema.backend.model.enums.TipoServicio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "asignaciones")
@Getter
@Setter
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    private Entrenador entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    @Enumerated(EnumType.STRING)
    @Column(name = "servicio", nullable = false, length = 20)
    private TipoServicio tipoServicio;

    private Boolean activa = true;

    @Column(name = "creada_en", updatable = false)
    private OffsetDateTime creadaEn = OffsetDateTime.now();
}
