package grit.sistema.backend.mapper.nutrition;

import grit.sistema.backend.dto.nutrition.*;
import grit.sistema.backend.entity.nutrition.AlimentoEnComida;
import grit.sistema.backend.entity.nutrition.Comida;
import grit.sistema.backend.entity.nutrition.PlanNutricion;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NutricionMapper {
    // --- 1. PLANES ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "atleta", ignore = true)
    @Mapping(target = "entrenador", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    PlanNutricion toEntity(PlanNutricionRequestDTO request);

    @Mapping(target = "atletaId", source = "atleta.id")
    PlanNutricionResponseDTO toResponseDTO(PlanNutricion plan);

    // Este es para el dashboard del atleta
    PlanData toDataDTO(PlanNutricion plan);

    ComidaResponseDTO toComidaResponseDTO(Comida entity);

    // --- 2. COMIDAS ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plan", ignore = true)
    Comida toComidaEntity(ComidaRequestDTO dto);

    ComidaDTO toComidaDTO(Comida entity);

    // --- 3. ALIMENTOS (La parte técnica resuelta) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comida", ignore = true)
    @Mapping(target = "codigoAlimento", source = "codigo") // Sincroniza nombres si DTO dice 'codigo' y Entity 'codigoAlimento'
    @Mapping(target = "cantidadG", source = "cantidadG")
    @Mapping(target = "kcalPor100g", ignore = true)
    @Mapping(target = "proteinasPor100g", ignore = true)
    @Mapping(target = "carbsPor100g", ignore = true)
    @Mapping(target = "grasasPor100g", ignore = true)
    AlimentoEnComida toAlimentoEntity(AlimentoDTO dto);

    @Mapping(target = "kcal", source = "entity", qualifiedByName = "calcularKcal")
    @Mapping(target = "proteinas", source = "entity", qualifiedByName = "calcularProteinas")
    @Mapping(target = "carbos", source = "entity", qualifiedByName = "calcularCarbos")
    @Mapping(target = "grasas", source = "entity", qualifiedByName = "calcularGrasas")
    @Mapping(target = "codigo", source = "codigoAlimento")
    AlimentoDTO toAlimentoDTO(AlimentoEnComida entity);

    // --- 5. LÓGICA DE CÁLCULO (DELEGADA A ENTIDAD) ---
    @Named("calcularKcal")
    default Integer calcularKcal(AlimentoEnComida a) {
        return (a == null || a.getKcalTotales() == null) ? 0 : a.getKcalTotales().intValue();
    }

    @Named("calcularProteinas")
    default Double calcularProteinas(AlimentoEnComida a) {
        return (a == null || a.getProteinasTotales() == null) ? 0.0 : a.getProteinasTotales().doubleValue();
    }

    @Named("calcularCarbos")
    default Double calcularCarbos(AlimentoEnComida a) {
        return (a == null || a.getCarbsTotales() == null) ? 0.0 : a.getCarbsTotales().doubleValue();
    }

    @Named("calcularGrasas")
    default Double calcularGrasas(AlimentoEnComida a) {
        return (a == null || a.getGrasasTotales() == null) ? 0.0 : a.getGrasasTotales().doubleValue();
    }

    // --- 6. VINCULACIÓN (LÓGICA DE NEGOCIO JPA) ---
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
}
