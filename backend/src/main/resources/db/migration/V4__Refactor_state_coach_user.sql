/* MIGRACIÓN: REFACTORIZACIÓN DE ESTADOS V2 (CORREGIDA) */

-- A. Lógica para la tabla ENTRENADORES
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_revision_tipo') THEN
CREATE TYPE estado_revision_tipo AS ENUM ('PENDIENTE_REVISION', 'APROBADO', 'RECHAZADO');
END IF;
END $$;

-- Añadir columna (Si ya existe de un intento fallido parcial, usamos este check)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='entrenadores' AND column_name='estado_revision') THEN
ALTER TABLE entrenadores ADD COLUMN estado_revision estado_revision_tipo DEFAULT 'PENDIENTE_REVISION';
END IF;
END $$;

-- B. Lógica para la tabla USUARIOS (La parte crítica)
-- 1. Creamos el nuevo tipo
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'usuario_estado_nuevo') THEN
CREATE TYPE usuario_estado_nuevo AS ENUM ('ACTIVO', 'SUSPENDIDO', 'BLOQUEADO');
END IF;
END $$;

-- 2. PASO CLAVE: Eliminar el DEFAULT por completo en una sentencia separada
ALTER TABLE usuarios ALTER COLUMN estado DROP DEFAULT;

-- 3. Ahora sí, cambiamos el tipo con la conversión explícita
ALTER TABLE usuarios
ALTER COLUMN estado TYPE usuario_estado_nuevo
    USING (
        CASE
            WHEN estado::text = 'ACTIVO' THEN 'ACTIVO'::usuario_estado_nuevo
            WHEN estado::text = 'SUSPENDIDO' THEN 'SUSPENDIDO'::usuario_estado_nuevo
            ELSE 'ACTIVO'::usuario_estado_nuevo
        END
    );

-- 4. Restauramos el nuevo valor por defecto
ALTER TABLE usuarios ALTER COLUMN estado SET DEFAULT 'ACTIVO'::usuario_estado_nuevo;

-- 5. Limpieza de tipos antiguos
-- Nota: Solo si el tipo antiguo se llamaba exactamente 'usuario_estado'
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'usuario_estado' AND typname != 'usuario_estado_nuevo') THEN
        -- Renombrar tipos para que coincidan con la entidad Java
DROP TYPE IF EXISTS usuario_estado CASCADE;
ALTER TYPE usuario_estado_nuevo RENAME TO usuario_estado;
END IF;
EXCEPTION
    WHEN OTHERS THEN NULL; -- Evita que falle si ya se renombró
END $$;