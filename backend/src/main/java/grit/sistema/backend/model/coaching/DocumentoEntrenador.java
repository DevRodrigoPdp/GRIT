package grit.sistema.backend.model.coaching;

import grit.sistema.backend.model.enums.DocStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documentos_entrenador")
@Getter
@Setter
@NoArgsConstructor
public class DocumentoEntrenador {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id")
    private Entrenador entrenador;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "url_s3", nullable = false)
    private String urlS3;

    @Enumerated(EnumType.STRING)
    private DocStatus status;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}