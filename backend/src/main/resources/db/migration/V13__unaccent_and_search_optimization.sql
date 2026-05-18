-- 1. Habilitar la extensión para ignorar tildes
CREATE EXTENSION IF NOT EXISTS unaccent;

-- 2. Crear el wrapper INMUTABLE (Imprescindible para índices)
CREATE OR REPLACE FUNCTION public.immutable_unaccent(text)
  RETURNS text AS
$func$
    -- Importante: Asegúrate de que el esquema coincida (public)
SELECT public.unaccent('public.unaccent', $1)
           $func$ LANGUAGE sql IMMUTABLE PARALLEL SAFE;

-- 3. Índices de Alimentos
CREATE INDEX IF NOT EXISTS idx_alimentos_nombre_unaccent
    ON alimentos (public.immutable_unaccent(LOWER(nombre)));

CREATE INDEX IF NOT EXISTS idx_alimentos_marca_unaccent
    ON alimentos (public.immutable_unaccent(LOWER(marca)));

-- 4. Índice Combinado (Buscador Global) - CORREGIDO
-- Todas las columnas deben usar la función inmutable
CREATE INDEX IF NOT EXISTS idx_alimentos_global_search_unaccent
    ON alimentos (
    public.immutable_unaccent(LOWER(nombre)),
    public.immutable_unaccent(LOWER(marca)),
    public.immutable_unaccent(LOWER(categoria))
    );

-- 5. Índices de Ejercicios
CREATE INDEX IF NOT EXISTS idx_ejercicios_nombre_unaccent
    ON ejercicios (public.immutable_unaccent(LOWER(nombre)));

CREATE INDEX IF NOT EXISTS idx_ejercicios_grupo_unaccent
    ON ejercicios (public.immutable_unaccent(LOWER(grupo_muscular)));

-- 2. Índices de Usuarios (Padre)
CREATE INDEX IF NOT EXISTS idx_usuarios_nombre_unaccent
    ON usuarios (public.immutable_unaccent(LOWER(nombre)));

CREATE INDEX IF NOT EXISTS idx_usuarios_email_lower
    ON usuarios (LOWER(email));

CREATE INDEX IF NOT EXISTS idx_usuarios_created_at
    ON usuarios (created_at); -- Importante para la ordenación de pendientes

-- 3. Índices de Entrenadores (Hijo)
-- En JOINED, el enlace es por la columna 'id'
CREATE INDEX IF NOT EXISTS idx_entrenadores_id ON entrenadores(id);

CREATE INDEX IF NOT EXISTS idx_entrenadores_estado ON entrenadores(estado_revision);