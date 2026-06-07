-- 1. Habilitar extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. TABLA USUARIOS (PK es UUID)
CREATE TABLE usuarios (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          nombre VARCHAR(255) NOT NULL,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,
                          rol VARCHAR(30) NOT NULL,
                          estado VARCHAR(30) DEFAULT 'PENDIENTE_REVISION'
                              CHECK (estado IN ('PENDIENTE_REVISION', 'ACTIVO', 'RECHAZADO', 'SUSPENDIDO')),
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. TABLA ENTRENADORES (PK es UUID, hereda de usuarios)
CREATE TABLE entrenadores (
                              usuario_id UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
                              codigo_profesional VARCHAR(20) UNIQUE,
                              titulacion_entrenamiento VARCHAR(50),
                              titulacion_nutricion VARCHAR(50),
                              tiene_titulo_entrenamiento BOOLEAN GENERATED ALWAYS AS (titulacion_entrenamiento IS NOT NULL) STORED,
                              tiene_titulo_nutricion BOOLEAN GENERATED ALWAYS AS (titulacion_nutricion IS NOT NULL) STORED,
                              estado VARCHAR(30) DEFAULT 'PENDIENTE_REVISION',
                              fecha_solicitud TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT check_alguna_titulacion CHECK (titulacion_entrenamiento IS NOT NULL OR titulacion_nutricion IS NOT NULL)
);

-- 4. TABLA ATLETAS (PK es UUID, hereda de usuarios)
CREATE TABLE atletas (
                         id UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
                         fecha_nac DATE NOT NULL,
                         genero VARCHAR(20) CHECK (genero IN ('hombre', 'mujer', 'otro')),
                         peso_kg DECIMAL(5,2),
                         altura_cm SMALLINT,
                         deporte VARCHAR(100),
                         nivel VARCHAR(20) CHECK (nivel IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO', 'ELITE')),
                         servicio VARCHAR(20) NOT NULL CHECK (servicio IN ('ENTRENAMIENTO', 'NUTRICION', 'AMBOS')),
                         objetivo VARCHAR(20)
);

-- 5. TABLA DE DOCUMENTOS (Relación con UUID)
CREATE TABLE documentos_entrenador (
                                       entrenador_id UUID NOT NULL REFERENCES entrenadores(usuario_id) ON DELETE CASCADE,
                                       url_documento VARCHAR(512) NOT NULL
);

-- 6. TABLA ENTRENAMIENTOS (PK es BIGINT, pero FK a usuario es UUID)
CREATE TABLE entrenamientos (
                                id BIGSERIAL PRIMARY KEY, -- Esto coincide con GenerationType.IDENTITY y Long id
                                uuid UUID NOT NULL UNIQUE, -- Tu campo privado para exposición
                                nombre VARCHAR(100) NOT NULL,
                                descripcion TEXT,
                                fecha_entrenamiento TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                duracion_minutos INTEGER,
                                usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE -- IMPORTANTE: FK debe ser UUID
);

CREATE INDEX idx_entrenamientos_usuario ON entrenamientos(usuario_id);

-- 7. TABLA EJERCICIOS (PK es BIGINT, FK a entrenamiento es BIGINT)
CREATE TABLE ejercicios (
                            id BIGSERIAL PRIMARY KEY, -- Esto coincide con Long id
                            uuid UUID NOT NULL UNIQUE,
                            nombre VARCHAR(100) NOT NULL,
                            series INTEGER NOT NULL,
                            repeticiones INTEGER NOT NULL,
                            peso_kg DOUBLE PRECISION NOT NULL,
                            entrenamiento_id BIGINT NOT NULL REFERENCES entrenamientos(id) ON DELETE CASCADE -- FK es BIGINT
);