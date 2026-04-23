package grit.sistema.backend.model.coaching;

import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoRevision;
import grit.sistema.backend.model.enums.SolicitudAmpliacionTipo;
import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import grit.sistema.backend.util.CodeGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entrenadores")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Entrenador extends Usuario {

    @Column(name = "codigo_profesional", unique = true, length = 20)
    private String codigoProfesional;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "titulacion_entrenamiento")
    private TitulacionEntrenamiento titulacionEntrenamiento;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "titulacion_nutricion")
    private TitulacionNutricion titulacionNutricion;

    @Column(name = "titulo_entrenamiento", nullable = false)
    private boolean tieneAccesoEntrenamiento = false;

    @Column(name = "titulo_nutricion", nullable = false)
    private boolean tieneAccesoNutricion = false;

    @Column(name = "experiencia_anos")
    private Short experienciaAnos;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado_revision")
    private EstadoRevision estadoRevision = EstadoRevision.PENDIENTE_REVISION;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "masters", columnDefinition = "text[]")
    private List<String> masters = new ArrayList<>();

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "codigo_invitacion", unique = true, nullable = false, length = 20)
    private String codigoInvitacion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "solicitud_ampliacion_pendiente")
    private SolicitudAmpliacionTipo solicitudAmpliacionPendiente;

    // --- RELACIONES ---
    @OneToMany(mappedBy = "entrenador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentoEntrenador> documentos = new ArrayList<>();

    @Override
    protected void onPrePersist() {
        super.onPrePersist();
        if (this.getEstadoRevision() == null) {
            this.setEstadoRevision(EstadoRevision.PENDIENTE_REVISION);
        }
        if (this.getCodigoInvitacion() == null) {
            this.setCodigoInvitacion(CodeGenerator.generateGritFormat());
        }
    }
    public void addDocumento(DocumentoEntrenador documento) {
        documentos.add(documento);
        documento.setEntrenador(this);
    }
}
