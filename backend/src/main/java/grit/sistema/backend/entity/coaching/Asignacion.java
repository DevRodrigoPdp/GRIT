package grit.sistema.backend.entity.coaching;

import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "asignaciones", indexes = {
        @Index(name = "idx_asignaciones_atleta", columnList = "atleta_id")
})
@Getter
@Setter
@NoArgsConstructor
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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "servicio", nullable = false, length = 20)
    private TipoServicio tipoServicio;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "creada_en", updatable = false, nullable = false)
    private OffsetDateTime creadaEn;

    @PrePersist
    protected void onCreate() {
        this.creadaEn = OffsetDateTime.now();
    }
}
