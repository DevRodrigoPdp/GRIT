package grit.sistema.backend.entity.nutrition;

import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notas_nutricionista")
@Getter
@Setter
@NoArgsConstructor
public class NotaNutricionista {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false, columnDefinition = "DATE", updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id")
    private Entrenador entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id")
    private Atleta atleta;
}
