-- 1. LIMPIEZA DE TABLAS OBSOLETAS
DROP TABLE IF EXISTS empleados CASCADE;
DROP TABLE IF EXISTS departamentos CASCADE;

-- 2. RENOMBRAR Y EVOLUCIONAR TABLA BASE
-- Renombramos la tabla de 'users' a 'usuarios'
ALTER TABLE users RENAME TO usuarios;

-- Eliminar columna innecesaria 'enabled'
ALTER TABLE usuarios DROP COLUMN IF EXISTS enabled;

-- Añadimos/Modificamos columnas para cumplir el contrato
ALTER TABLE usuarios
    -- Renombramos password a password_hash por claridad semántica
    RENAME COLUMN password TO password_hash;

ALTER TABLE usuarios
    -- Añadimos campos faltantes con restricciones
    ADD COLUMN IF NOT EXISTS estado VARCHAR(30) DEFAULT 'ACTIVO' CHECK (estado IN ('PENDIENTE_REVISION', 'ACTIVO', 'RECHAZADO', 'SUSPENDIDO'));

-- 3. CREACIÓN DE TABLAS DE ESPECIALIZACIÓN (FK a usuarios.id)
CREATE TABLE entrenadores (
                              id BIGINT PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
                              codigo_profesional VARCHAR(20) UNIQUE,
                              titulacion_entrenamiento VARCHAR(50),
                              titulacion_nutricion VARCHAR(50),
                              titulo_entrenamiento BOOLEAN GENERATED ALWAYS AS (titulacion_entrenamiento IS NOT NULL) STORED,
                              titulo_nutricion BOOLEAN GENERATED ALWAYS AS (titulacion_nutricion IS NOT NULL) STORED,
                              CONSTRAINT check_alguna_titulacion CHECK (titulacion_entrenamiento IS NOT NULL OR titulacion_nutricion IS NOT NULL)
);

CREATE TABLE atletas (
                         id BIGINT PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
                         fecha_nac DATE NOT NULL,
                         genero VARCHAR(20) CHECK (genero IN ('hombre', 'mujer', 'otro')),
                         peso_kg DECIMAL(5,2),
                         altura_cm SMALLINT,
                         deporte VARCHAR(100),
                         nivel VARCHAR(20) CHECK (nivel IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO', 'ELITE')),
                         servicio VARCHAR(20) NOT NULL CHECK (servicio IN ('ENTRENAMIENTO', 'NUTRICION', 'AMBOS')),
                         objetivo VARCHAR(20)
);

-- 4. TABLA DE DOCUMENTOS (FK a entrenadores.id)
CREATE TABLE documentos_entrenador (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       entrenador_id BIGINT NOT NULL REFERENCES entrenadores(id) ON DELETE CASCADE,
                                       nombre_archivo VARCHAR(255) NOT NULL,
                                       url_s3 TEXT NOT NULL,
                                       tipo_mime VARCHAR(50),
                                       tamanyo_bytes INTEGER,
                                       status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'verified', 'rejected')),
                                       rejection_reason TEXT,
                                       uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                       reviewed_at TIMESTAMP WITH TIME ZONE
);