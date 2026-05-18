/* MIGRACIÓN V6: Actualización Integral - Nutrición, Entrenamiento y Check-ins
   Arquitecto: SPRINGBOOT
*/
DROP TABLE IF EXISTS recetas CASCADE;
DROP TABLE IF EXISTS ingredientes_receta CASCADE;

-- 1. Extensiones de tipos ENUM existentes y nuevos
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkin_estado') THEN
CREATE TYPE checkin_estado AS ENUM ('PENDIENTE', 'COMPLETADA');
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'solicitud_ampliacion_tipo') THEN
CREATE TYPE solicitud_ampliacion_tipo AS ENUM ('ENTRENAMIENTO', 'NUTRICION');
END IF;
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 2. ACTUALIZACIÓN TABLA ENTRENADORES
-- Añadimos campos de control de acceso, códigos de invitación y metadatos profesionales
ALTER TABLE entrenadores
    ADD COLUMN IF NOT EXISTS titulo_entrenamiento BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS titulo_nutricion BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS masters TEXT[] DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS codigo_invitacion VARCHAR(20) UNIQUE,
    ADD COLUMN IF NOT EXISTS foto_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS solicitud_ampliacion_pendiente solicitud_ampliacion_tipo;

-- 3. ACTUALIZACIÓN TABLA ATLETAS
ALTER TABLE atletas
    ADD COLUMN IF NOT EXISTS alergias TEXT[] DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS intolerancias TEXT[] DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS foto_url VARCHAR(500);

-- 4. NUEVAS TABLAS DE SEGUIMIENTO (PESO)
CREATE TABLE checkins_peso_solicitudes (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           entrenador_id UUID NOT NULL REFERENCES entrenadores(id),
                                           atleta_id UUID NOT NULL REFERENCES atletas(id),
                                           estado checkin_estado DEFAULT 'PENDIENTE',
                                           creada_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                           completada_en TIMESTAMP WITH TIME ZONE
);

-- Constraint de Negocio: Solo una solicitud pendiente por atleta
CREATE UNIQUE INDEX idx_solicitud_peso_pendiente
    ON checkins_peso_solicitudes (atleta_id)
    WHERE (estado = 'PENDIENTE');

CREATE TABLE checkins_peso (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               solicitud_id UUID NOT NULL REFERENCES checkins_peso_solicitudes(id) ON DELETE CASCADE,
                               atleta_id UUID NOT NULL REFERENCES atletas(id) ON DELETE CASCADE,
                               peso_kg DECIMAL(5,2) NOT NULL CHECK (peso_kg BETWEEN 30 AND 300),
                               fecha DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 5. NOTAS DEL NUTRICIONISTA (Tab Dieta)
CREATE TABLE notas_nutricionista (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     entrenador_id UUID NOT NULL REFERENCES entrenadores(id),
                                     atleta_id UUID NOT NULL REFERENCES atletas(id),
                                     texto TEXT NOT NULL,
                                     fecha DATE NOT NULL DEFAULT CURRENT_DATE,
                                     creada_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. REFACTORIZACIÓN DE ALIMENTOS RECIENTES (UPSERT Ready)
-- Eliminamos la tabla anterior para asegurar la nueva estructura de constraints si es necesario
DROP TABLE IF EXISTS alimentos_recientes;
CREATE TABLE alimentos_recientes (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                                     nombre_comida VARCHAR(100) NOT NULL,
                                     alimento_id UUID NOT NULL, -- ID del alimento en tu catálogo maestro
                                     usado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT uk_usuario_comida_alimento UNIQUE (usuario_id, nombre_comida, alimento_id)
);

-- 7. REFACTORIZACIÓN DE EJERCICIOS EN SESIÓN (Snapshot Pattern)
-- Actualizamos la tabla existente para incluir los campos del dataset externo
ALTER TABLE ejercicios_en_sesion
    ADD COLUMN IF NOT EXISTS ejercicio_categoria VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ejercicio_musculo_principal VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ejercicio_imagen_url TEXT;

-- 8. ACTUALIZACIÓN DE COMIDAS
ALTER TABLE comidas
    ADD COLUMN IF NOT EXISTS notas TEXT;

-- 9. ÍNDICES DE RENDIMIENTO ADICIONALES
CREATE INDEX idx_checkins_atleta ON checkins_peso(atleta_id);
CREATE INDEX idx_notas_atleta_fecha ON notas_nutricionista(atleta_id, fecha);

-- 1. Función que limpia los registros sobrantes
CREATE OR REPLACE FUNCTION fn_limpiar_alimentos_recientes()
RETURNS TRIGGER AS $$
BEGIN
    -- Borra los registros que exceden los 8 más recientes para el usuario y tipo de comida
DELETE FROM alimentos_recientes
WHERE id IN (
    SELECT id
    FROM alimentos_recientes
    WHERE usuario_id = NEW.usuario_id
      AND nombre_comida = NEW.nombre_comida
    ORDER BY usado_en DESC
    OFFSET 8 -- Mantiene los 8 más recientes y selecciona el resto para borrar
);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Trigger que se ejecuta tras insertar un nuevo alimento reciente
CREATE TRIGGER tr_limitar_alimentos_recientes
    AFTER INSERT ON alimentos_recientes
    FOR EACH ROW
    EXECUTE FUNCTION fn_limpiar_alimentos_recientes();

-- 1. Creación de la tabla con Constraint de Unicidad
CREATE TABLE ejercicios_recientes (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      entrenador_id UUID NOT NULL REFERENCES entrenadores(id) ON DELETE CASCADE,
                                      nombre VARCHAR(255) NOT NULL,
                                      usado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT uk_entrenador_ejercicio UNIQUE (entrenador_id, nombre)
);

-- 2. Función de limpieza (Mantenemos los 8 más recientes)
CREATE OR REPLACE FUNCTION fn_limpiar_ejercicios_recientes()
RETURNS TRIGGER AS $$
BEGIN
DELETE FROM ejercicios_recientes
WHERE id IN (
    SELECT id
    FROM ejercicios_recientes
    WHERE entrenador_id = NEW.entrenador_id
    ORDER BY usado_en DESC
    OFFSET 8
);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. Trigger post-inserción
CREATE TRIGGER tr_limitar_ejercicios_recientes
    AFTER INSERT ON ejercicios_recientes
    FOR EACH ROW
    EXECUTE FUNCTION fn_limpiar_ejercicios_recientes();

-- 4. Índice para búsquedas rápidas por entrenador
CREATE INDEX idx_ejercicios_recientes_entrenador ON ejercicios_recientes(entrenador_id);

CREATE UNIQUE INDEX idx_asignacion_activa_unica
    ON asignaciones (atleta_id, servicio)
    WHERE (activa = true);