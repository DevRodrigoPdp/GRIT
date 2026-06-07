-- V5: Ajustes de esquema para compatibilidad con el modelo de nutrición y rutinas

ALTER TABLE alimentos_recientes
    ADD COLUMN IF NOT EXISTS marca VARCHAR(255),
    ADD COLUMN IF NOT EXISTS kcal_por_100g DECIMAL(7,2),
    ADD COLUMN IF NOT EXISTS proteinas_por_100g DECIMAL(7,2),
    ADD COLUMN IF NOT EXISTS carbs_por_100g DECIMAL(7,2),
    ADD COLUMN IF NOT EXISTS grasas_por_100g DECIMAL(7,2);

ALTER TABLE rutinas
    ADD COLUMN IF NOT EXISTS descripcion TEXT;
