package grit.sistema.backend.mapper.nutrition;

import grit.sistema.backend.dto.nutrition.*;
import grit.sistema.backend.entity.nutrition.AlimentoEnComida;
import grit.sistema.backend.entity.nutrition.AlimentoReciente;
import grit.sistema.backend.entity.nutrition.Comida;
import grit.sistema.backend.entity.nutrition.PlanNutricion;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NutricionMapper {
    // --- MAPEO DE PLAN ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "atleta", ignore = true)
    @Mapping(target = "entrenador", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    PlanNutricion toEntity(PlanNutricionRequestDTO request);

    // --- MAPEOS SIN ID (Para Actualizaciones Seguras) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plan", ignore = true)
    Comida toComidaEntitySinId(ComidaRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comida", ignore = true)
    @Mapping(target = "codigoAlimento", source = "codigo")
    AlimentoEnComida toAlimentoEnComidaEntitySinId(AlimentoRequestDTO dto);

    // --- LÓGICA DE VINCULACIÓN ---
    @AfterMapping
    default void establecerRelaciones(@MappingTarget PlanNutricion plan) {
        if (plan.getComidas() != null) {
            plan.getComidas().forEach(comida -> {
                comida.setPlan(plan);
                if (comida.getAlimentos() != null) {
                    comida.getAlimentos().forEach(alimento -> alimento.setComida(comida));
                }
            });
        }
    }

    // --- ALIMENTOS RECIENTES ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alimentoId", source = "id")
    @Mapping(target = "nombreComida", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "usadoEn", expression = "java(java.time.OffsetDateTime.now())")
    AlimentoReciente toAlimentoRecienteEntity(AlimentoRequestDTO dto);

    @Mapping(target = "id", source = "alimentoId")
    AlimentoRecienteDTO toAlimentoRecienteDTO(AlimentoReciente entity);

    // --- RESPUESTAS ---
    PlanNutricionResponseDTO toResponseDTO(PlanNutricion plan);
}
