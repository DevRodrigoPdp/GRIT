-- V6: Crear tabla central de alimentos

-- Habilitar extensión pg_trgm para búsqueda full-text eficiente
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Tabla central de alimentos reutilizables
CREATE TABLE alimentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(255) NOT NULL,
    marca VARCHAR(255),
    kcal_por_100g DECIMAL(7,2) NOT NULL,
    proteinas_por_100g DECIMAL(7,2) NOT NULL,
    carbs_por_100g DECIMAL(7,2) NOT NULL,
    grasas_por_100g DECIMAL(7,2) NOT NULL,
    creado_por UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índice GIN usando pg_trgm para búsqueda eficiente por nombre
CREATE INDEX idx_alimentos_nombre_trgm ON alimentos USING GIN (nombre gin_trgm_ops);

-- Índice simple para búsquedas por ILIKE básicas
CREATE INDEX idx_alimentos_nombre ON alimentos (nombre);

-- Índice para búsquedas por marca
CREATE INDEX idx_alimentos_marca ON alimentos (marca);
