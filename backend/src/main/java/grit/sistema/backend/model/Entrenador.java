package grit.sistema.backend.model;

import grit.sistema.backend.model.enums.EstadoRevision;
import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entrenadores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrenador {

    @Id
    @Column(name = "usuario_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "codigo_profesional", unique = true)
    private String codigoProfesional;

    @Enumerated(EnumType.STRING)
    private TitulacionEntrenamiento titulacionEntrenamiento;

    @Enumerated(EnumType.STRING)
    private TitulacionNutricion titulacionNutricion;

    // Campos lógicos requeridos por el frontend
    @Column(nullable = false)
    private boolean tieneTituloEntrenamiento;

    @Column(nullable = false)
    private boolean tieneTituloNutricion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoRevision estado = EstadoRevision.PENDIENTE_REVISION;

    // Guardamos las URLS de los documentos subidos a S3
    @ElementCollection
    @CollectionTable(
            name = "documentos_entrenador",
            joinColumns = @JoinColumn(name = "entrenador_id", columnDefinition = "uuid"))
    @Column(name = "url_documento", length = 512)
    private List<String> documentosUrls;

    private LocalDateTime fechaSolicitud;

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDateTime.now();
        this.tieneTituloEntrenamiento = (this.titulacionEntrenamiento != null);
        this.tieneTituloNutricion = (this.titulacionNutricion != null);

        // Garantizamos que el estado sea el correcto al persistir
        if (this.estado == null) this.estado = EstadoRevision.PENDIENTE_REVISION;
    }
}
