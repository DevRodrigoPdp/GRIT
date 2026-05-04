package grit.sistema.backend.entity.communication;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mensajes_hilo")
@Getter
@Setter
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hilo_id")
    private Hilo hilo;

    @Column(columnDefinition = "TEXT")
    private String texto;

    @Column(name = "enviado_por")
    private String enviadoPor; // "ENTRENADOR" o "ATLETA"

    @OneToMany(mappedBy = "mensaje", cascade = CascadeType.ALL)
    private List<Adjunto> adjuntos = new ArrayList<>();

    private LocalDateTime enviadoEn = LocalDateTime.now();
}
