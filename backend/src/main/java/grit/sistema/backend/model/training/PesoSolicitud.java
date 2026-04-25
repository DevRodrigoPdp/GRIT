package grit.sistema.backend.model.training;

import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.enums.EstadoCheckin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "checkins_peso_solicitudes")
@Getter
@Setter
@NoArgsConstructor
public class PesoSolicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id")
    private Atleta atleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id")
    private Entrenador entrenador;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private EstadoCheckin estado = EstadoCheckin.PENDIENTE;

    private LocalDateTime creadaEn = LocalDateTime.now();
    private LocalDateTime completadaEn;
}