package grit.sistema.backend.entity.communication;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "adjuntos_mensaje")
@Getter
@Setter
@NoArgsConstructor
public class Adjunto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensaje_id", nullable = false)
    private Mensaje mensaje;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;
    private String nombreOriginal;
    private String tipo; // "IMAGEN", "VIDEO"
}