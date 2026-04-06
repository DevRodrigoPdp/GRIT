package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.AtletaData;
import grit.sistema.backend.dto.AtletaRequestDTO;
import grit.sistema.backend.dto.AtletaResponseDTO;
import grit.sistema.backend.model.Atleta;
import grit.sistema.backend.model.enums.TipoServicio;
import org.springframework.stereotype.Component;

@Component
public class AtletaMapper {

    public Atleta toEntity(AtletaRequestDTO dto) {
        if (dto == null) return null;

        Atleta atleta = new Atleta();
        atleta.setNombre(dto.nombre());
        atleta.setEmail(dto.email());
        atleta.setFechaNac(dto.fechaNac());
        atleta.setGenero(dto.genero());
        atleta.setPesoKg(dto.pesoKg());
        atleta.setAlturaCm(dto.alturaCm());
        atleta.setDeporte(dto.deporte());
        atleta.setNivel(dto.nivel());
        atleta.setServicio(dto.servicio());

        if (dto.servicio() == TipoServicio.NUTRICION) {
            atleta.setObjetivo(null);
        } else {
            atleta.setObjetivo(dto.objetivo());
        }

        return atleta;
    }

    public AtletaResponseDTO toResponseDTO(Atleta guardado) {
        if (guardado == null) return null;

        AtletaData data = new AtletaData(
                guardado.getId(),
                guardado.getEstado().name(),
                guardado.getRol().name()
        );

        return new AtletaResponseDTO(
                true,
                "Perfil creado correctamente.",
                data
        );
    }
}
