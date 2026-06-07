/* MIGRACIÓN V3: Refactorización a Modelo Profesional
   Arquitecto: SPRINGBOOT
*/

-- 1. Definición de Tipos ENUM (Garantizan integridad)
DO $$ BEGIN
CREATE TYPE usuario_rol AS ENUM ('ENTRENADOR', 'ATLETA', 'ADMIN');
CREATE TYPE usuario_estado AS ENUM ('PENDIENTE_REVISION', 'ACTIVO', 'RECHAZADO', 'SUSPENDIDO');
CREATE TYPE genero_tipo AS ENUM ('HOMBRE', 'MUJER', 'OTRO');
CREATE TYPE nivel_atleta AS ENUM ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO', 'ELITE');
CREATE TYPE servicio_tipo AS ENUM ('ENTRENAMIENTO', 'NUTRICION', 'AMBOS');
CREATE TYPE objetivo_tipo AS ENUM ('RENDIMIENTO', 'MASA_MUSCULAR', 'PERDER_PESO', 'SALUD', 'RESISTENCIA');
CREATE TYPE titulacion_ent_tipo AS ENUM ('GRADO_CAFYD', 'TSAF_TSEAS', 'CERT_AFDA0210');
CREATE TYPE titulacion_nut_tipo AS ENUM ('GRADO_NUTRICION_DIETETICA', 'TSD');
CREATE TYPE doc_status AS ENUM ('pending', 'verified', 'rejected');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 2. Limpieza de tablas previas (OJO: En prod esto requiere backup y migración de datos)
DROP TABLE IF EXISTS documentos_entrenador CASCADE;
DROP TABLE IF EXISTS atletas CASCADE;
DROP TABLE IF EXISTS entrenadores CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS ejercicios CASCADE;
DROP TABLE IF EXISTS entrenamientos CASCADE;

-- 3. Tabla USUARIOS
CREATE TABLE usuarios (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          nombre VARCHAR(255) NOT NULL,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          password_hash VARCHAR(255) NOT NULL, -- Se gestionará con BCrypt (cost 12) desde Java
                          rol usuario_rol NOT NULL,
                          estado usuario_estado DEFAULT 'PENDIENTE_REVISION',
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabla ENTRENADORES
CREATE TABLE entrenadores (
                              id UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
                              codigo_profesional VARCHAR(20) UNIQUE,
                              titulacion_entrenamiento titulacion_ent_tipo,
                              titulacion_nutricion titulacion_nut_tipo,
                              experiencia_anos SMALLINT,
                              descripcion TEXT,
    -- Nota: Los booleanos 'titulo_entrenamiento' se calculan en el DTO o Entity, no ocupamos espacio en DB.
                              CONSTRAINT check_alguna_titulacion CHECK (titulacion_entrenamiento IS NOT NULL OR titulacion_nutricion IS NOT NULL)
);

-- 5. Tabla ATLETAS
CREATE TABLE atletas (
                         id UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
                         fecha_nac DATE NOT NULL,
                         genero genero_tipo NOT NULL,
                         peso_kg DECIMAL(5,2),
                         altura_cm SMALLINT,
                         deporte VARCHAR(100),
                         nivel nivel_atleta NOT NULL,
                         servicio servicio_tipo NOT NULL,
                         objetivo objetivo_tipo
);

-- 6. Tabla DOCUMENTOS_ENTRENADOR
CREATE TABLE documentos_entrenador (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       entrenador_id UUID NOT NULL REFERENCES entrenadores(id) ON DELETE CASCADE,
                                       nombre_archivo VARCHAR(255) NOT NULL,
                                       url_s3 TEXT NOT NULL, -- Almacena el Key o el Path del Bucket
                                       tipo_mime VARCHAR(50),
                                       tamanyo_bytes INTEGER,
                                       status doc_status DEFAULT 'pending',
                                       rejection_reason TEXT,
                                       uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                       reviewed_at TIMESTAMP WITH TIME ZONE
);

-- Indices para optimización de búsquedas
CREATE INDEX idx_usuarios_correo ON usuarios(email);
CREATE INDEX idx_docs_entrenador ON documentos_entrenador(entrenador_id);