-- V2: Estructura avanzada de Nutrición, Recetas y Rutinas

-- 1. SISTEMA DE NUTRICIÓN
CREATE TABLE planes_nutricion (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  entrenador_id UUID NOT NULL REFERENCES entrenadores(usuario_id),
                                  atleta_id UUID NOT NULL REFERENCES atletas(id),
                                  nombre VARCHAR(255) NOT NULL,
                                  descripcion TEXT,
                                  creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE comidas (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         plan_id UUID NOT NULL REFERENCES planes_nutricion(id) ON DELETE CASCADE,
                         nombre VARCHAR(100) NOT NULL, -- "Desayuno", "Almuerzo"
                         orden SMALLINT NOT NULL
);

CREATE TABLE alimentos_en_comida (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     comida_id UUID NOT NULL REFERENCES comidas(id) ON DELETE CASCADE,
                                     codigo_alimento VARCHAR(50), -- Barcode de Open Food Facts
                                     nombre VARCHAR(255) NOT NULL,
                                     marca VARCHAR(255),
                                     kcal_por_100g DECIMAL(7,2) NOT NULL,
                                     proteinas_por_100g DECIMAL(7,2) NOT NULL,
                                     carbs_por_100g DECIMAL(7,2) NOT NULL,
                                     grasas_por_100g DECIMAL(7,2) NOT NULL,
                                     cantidad_g DECIMAL(7,2) NOT NULL
);

-- 2. RECETAS PROPIAS DEL ENTRENADOR
CREATE TABLE recetas (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         entrenador_id UUID NOT NULL REFERENCES entrenadores(usuario_id),
                         nombre VARCHAR(255) NOT NULL,
                         gramos_total DECIMAL(7,2) NOT NULL,
                         creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ingredientes_receta (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     receta_id UUID NOT NULL REFERENCES recetas(id) ON DELETE CASCADE,
                                     codigo_alimento VARCHAR(50),
                                     nombre VARCHAR(255) NOT NULL,
                                     kcal_por_100g DECIMAL(7,2) NOT NULL,
                                     cantidad_g DECIMAL(7,2) NOT NULL,
                                     orden SMALLINT
);

-- 3. HISTORIAL DE ALIMENTOS RECIENTES (Optimización UX)
CREATE TABLE alimentos_recientes (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     usuario_id UUID NOT NULL REFERENCES usuarios(id),
                                     nombre_comida VARCHAR(100) NOT NULL,
                                     codigo_alimento VARCHAR(50) NOT NULL,
                                     nombre VARCHAR(255) NOT NULL,
                                     marca VARCHAR(255),
                                     kcal_por_100g DECIMAL(7,2),
                                     proteinas_por_100g DECIMAL(7,2),
                                     carbs_por_100g DECIMAL(7,2),
                                     grasas_por_100g DECIMAL(7,2),
                                     usado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                     UNIQUE(usuario_id, nombre_comida, codigo_alimento)
);

-- 4. SISTEMA DE RUTINAS AVANZADAS
CREATE TABLE rutinas (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         entrenador_id UUID NOT NULL REFERENCES entrenadores(usuario_id),
                         atleta_id UUID NOT NULL REFERENCES atletas(id),
                         nombre VARCHAR(255) NOT NULL,
                         descripcion TEXT,
                         creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sesiones_rutina (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 rutina_id UUID NOT NULL REFERENCES rutinas(id) ON DELETE CASCADE,
                                 nombre VARCHAR(100) NOT NULL, -- "Día de Empuje", "Pierna"
                                 orden SMALLINT NOT NULL
);

CREATE TABLE ejercicios_en_sesion (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      sesion_id UUID NOT NULL REFERENCES sesiones_rutina(id) ON DELETE CASCADE,
                                      ejercicio_id VARCHAR(100) NOT NULL, -- ID de dataset Yuhonas
                                      ejercicio_nombre VARCHAR(255) NOT NULL,
                                      series SMALLINT NOT NULL,
                                      reps VARCHAR(50) NOT NULL, -- Permite "10-12" o "Al fallo"
                                      notas TEXT,
                                      orden SMALLINT NOT NULL
);


-- 4. RELACIÓN ENTRENADOR/ATLETA
CREATE TABLE asignaciones (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              entrenador_id UUID NOT NULL REFERENCES entrenadores(usuario_id) ON DELETE CASCADE,
                              atleta_id UUID NOT NULL REFERENCES atletas(id) ON DELETE CASCADE,
                              servicio VARCHAR(20) NOT NULL CHECK (servicio IN ('ENTRENAMIENTO', 'NUTRICION')),
                              activa BOOLEAN DEFAULT TRUE,
                              creada_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Crear los índices de rendimiento
CREATE INDEX idx_asignaciones_entrenador ON asignaciones(entrenador_id);
CREATE INDEX idx_asignaciones_atleta ON asignaciones(atleta_id);

-- 3. Crear el índice único parcial (Aquí es donde aplicamos la lógica de negocio)
-- Esto evita que un atleta tenga dos entrenadores de 'NUTRICION' con activa = true
CREATE UNIQUE INDEX unique_asignacion_activa
    ON asignaciones (atleta_id, servicio)
    WHERE (activa = TRUE);