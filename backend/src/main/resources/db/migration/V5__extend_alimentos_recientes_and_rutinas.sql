-- V5: Ajustes de esquema para compatibilidad con el modelo de nutrición y rutinas

ALTER TABLE alimentos_recientes
    ADD COLUMN marca VARCHAR(255),
    ADD COLUMN kcal_por_100g DECIMAL(7,2),
    ADD COLUMN proteinas_por_100g DECIMAL(7,2),
    ADD COLUMN carbs_por_100g DECIMAL(7,2),
    ADD COLUMN grasas_por_100g DECIMAL(7,2);

ALTER TABLE rutinas
    ADD COLUMN descripcion TEXT;
