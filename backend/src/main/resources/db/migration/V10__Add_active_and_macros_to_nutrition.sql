-- 1. ACTUALIZACIÓN DE PLANES DE NUTRICIÓN
ALTER TABLE planes_nutricion
    ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS kcal_diarias INTEGER,
    ADD COLUMN IF NOT EXISTS proteinas DECIMAL(7,2),
    ADD COLUMN IF NOT EXISTS carbos DECIMAL(7,2),
    ADD COLUMN IF NOT EXISTS grasas DECIMAL(7,2);

-- Índices para Nutrición
CREATE INDEX IF NOT EXISTS idx_atleta_plan_activo
    ON planes_nutricion(atleta_id) WHERE (activo = true);

CREATE UNIQUE INDEX IF NOT EXISTS idx_unico_plan_activo_por_atleta
    ON planes_nutricion (atleta_id) WHERE (activo = true);


-- 2. ACTUALIZACIÓN DE RUTINAS
ALTER TABLE rutinas
    ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS descripcion TEXT; -- Agregado para integridad con el modelo

-- Índices para Rutinas
CREATE INDEX IF NOT EXISTS idx_atleta_rutina_activa
    ON rutinas(atleta_id) WHERE (activo = true);

CREATE UNIQUE INDEX IF NOT EXISTS idx_unico_entrenamiento_activo_atleta
    ON rutinas (atleta_id) WHERE (activo = true);

ALTER TABLE ejercicios_en_sesion
ALTER COLUMN ejercicio_id TYPE UUID USING ejercicio_id::uuid;