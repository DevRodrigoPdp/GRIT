package grit.sistema.backend.entity.nutrition;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alimentos_recientes", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_usuario_comida_alimento",
                columnNames = {"usuario_id", "nombre_comida", "alimento_id"}
        )
})
@Getter
@Setter
@NoArgsConstructor
public class AlimentoReciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "nombre_comida", nullable = false, length = 100)
    private String nombreComida;

    @Column(name = "alimento_id", nullable = false)
    private UUID alimentoId;

    @Column(name = "usado_en", nullable = false)
    private OffsetDateTime usadoEn;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.usadoEn = OffsetDateTime.now();
    }
}
