-- V7: Crear tabla central de alimentos

-- 1. Extensiones (Solo una vez)
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Tabla de Alimentos (Nutrición)
CREATE TABLE IF NOT EXISTS alimentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(255) NOT NULL,
    marca VARCHAR(255),
    categoria VARCHAR(100), -- Ej: Lácteos, Carnes, Frutas
    kcal_por_100g NUMERIC(7,2) NOT NULL CHECK (kcal_por_100g >= 0),
    proteinas_por_100g NUMERIC(7,2) NOT NULL CHECK (proteinas_por_100g >= 0),
    carbs_por_100g NUMERIC(7,2) NOT NULL CHECK (carbs_por_100g >= 0),
    grasas_por_100g NUMERIC(7,2) NOT NULL CHECK (grasas_por_100g >= 0),
    creado_por UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                                                );

-- 3. Tabla de Ejercicios (Entrenamiento)
CREATE TABLE IF NOT EXISTS ejercicios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(255) NOT NULL UNIQUE,
    grupo_muscular VARCHAR(100) NOT NULL, -- Ej: Pecho, Espalda, Pierna [cite: 16]
    equipo_necesario VARCHAR(100), -- Ej: Mancuernas, Barra, Máquina
    descripcion TEXT,
    dificultad VARCHAR(50) CHECK (dificultad IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                                 );

-- 4. Índices de Alto Rendimiento (GIN para búsquedas parciales)
CREATE INDEX idx_alimentos_nombre_tgrm ON alimentos USING GIN (nombre gin_trgm_ops);
CREATE INDEX idx_ejercicios_nombre_tgrm ON ejercicios USING GIN (nombre gin_trgm_ops);

-- 5. Índices de Filtrado (B-Tree para filtros exactos)
CREATE INDEX idx_alimentos_categoria ON alimentos (categoria);
CREATE INDEX idx_ejercicios_grupo_muscular ON ejercicios (grupo_muscular);