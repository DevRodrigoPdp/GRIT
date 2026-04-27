package grit.sistema.backend.entity.coaching;

import grit.sistema.backend.entity.coaching.enums.DocStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "documentos_entrenador")
@Getter
@Setter
@NoArgsConstructor
public class DocumentoEntrenador {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", referencedColumnName = "id")
    private Entrenador entrenador;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "url_s3", nullable = false, columnDefinition = "TEXT")
    private String urlS3;

    @Column(name = "tipo_mime", length = 50) // Añadido para coincidir con SQL
    private String tipoMime;

    @Column(name = "tamanyo_bytes") // Añadido para coincidir con SQL
    private Integer tamanyoBytes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status") // Aseguramos el nombre exacto
    private DocStatus status = DocStatus.pending;

    @Column(name = "rejection_reason", columnDefinition = "TEXT") // Añadido
    private String rejectionReason;

    @Column(name = "uploaded_at")
    private java.time.OffsetDateTime uploadedAt;

    @Column(name = "reviewed_at")
    private java.time.OffsetDateTime reviewedAt;
}