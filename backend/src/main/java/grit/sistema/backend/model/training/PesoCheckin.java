package grit.sistema.backend.model.training;

import grit.sistema.backend.model.coaching.Atleta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "checkins_peso")
@Getter
@Setter
@NoArgsConstructor
public class PesoCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relación con la solicitud que originó este registro
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private PesoSolicitud solicitud;

    // Relación con el atleta (propietario del dato)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    @Column(name = "peso_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();

}
