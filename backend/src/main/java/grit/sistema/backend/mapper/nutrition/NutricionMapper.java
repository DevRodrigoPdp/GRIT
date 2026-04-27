package grit.sistema.backend.mapper.nutrition;

import grit.sistema.backend.dto.nutrition.*;
import grit.sistema.backend.entity.nutrition.AlimentoEnComida;
import grit.sistema.backend.entity.nutrition.AlimentoReciente;
import grit.sistema.backend.entity.nutrition.Comida;
import grit.sistema.backend.entity.nutrition.PlanNutricion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NutricionMapper {
    // --- PLAN DE NUTRICIÓN ---
    @Mapping(target = "atleta", ignore = true)
    @Mapping(target = "entrenador", ignore = true)
    @Mapping(target = "comidas", source = "comidas")
    PlanNutricion toEntity(PlanNutricionRequestDTO request);

    @Mapping(target = "atletaId", source = "atleta.id")
    PlanNutricionDTO toDTO(PlanNutricion entity);

    // --- COMIDAS ---
    @Mapping(target = "plan", ignore = true)
    Comida toEntity(ComidaRequestDTO dto);

    ComidaDTO toDTO(Comida entity);

    // --- ALIMENTOS EN COMIDA ---
    // Si AlimentoEnComida no tiene 'codigoAlimento', quita el primer @Mapping
    @Mapping(target = "comida", ignore = true)
    AlimentoEnComida toAlimentoEnComidaEntity(AlimentoRequestDTO dto);

    // --- ALIMENTOS RECIENTES (Aquí estaban los errores) ---

    // Entidad -> AlimentoDTO
    // Quitamos 'id' y 'codigoAlimento' porque MapStruct dice que no existen
    @Mapping(target = "codigo", ignore = true) // O mapealo a 'alimentoId' si quieres ver el ID como código
    AlimentoDTO toAlimentoDTO(AlimentoReciente entity);

    // Entidad -> AlimentoRecienteDTO
    @Mapping(target = "id", source = "alimentoId")
    @Mapping(target = "codigo", ignore = true)
    AlimentoRecienteDTO toAlimentoRecienteDTO(AlimentoReciente entity);

    // DTO -> Entidad (Para guardar)
    @Mapping(target = "alimentoId", source = "id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usadoEn", ignore = true)
    AlimentoReciente toAlimentoRecienteEntity(AlimentoRequestDTO dto);

    // --- RESPUESTAS ---
    PlanNutricionResponseDTO toResponseDTO(PlanNutricion plan);
}
