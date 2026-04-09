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
public class Entrenador implements org.springframework.data.domain.Persistable<UUID>{

    @Id
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

    // Solo lectura: PostgreSQL los gestiona
    @Column(name = "tiene_titulo_entrenamiento", insertable = false, updatable = false)
    @org.hibernate.annotations.Generated
    private Boolean tieneTituloEntrenamiento;

    @Column(name = "tiene_titulo_nutricion", insertable = false, updatable = false)
    @org.hibernate.annotations.Generated
    private Boolean tieneTituloNutricion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoRevision estado = EstadoRevision.PENDIENTE_REVISION;

    @ElementCollection
    @CollectionTable(
            name = "documentos_entrenador",
            joinColumns = @JoinColumn(name = "entrenador_id"))
    @Column(name = "url_documento", length = 512)
    private List<String> documentosUrls;

    private LocalDateTime fechaSolicitud;

    @PrePersist
    protected void onCreate() {
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = LocalDateTime.now();
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return fechaSolicitud == null;
    }
}
