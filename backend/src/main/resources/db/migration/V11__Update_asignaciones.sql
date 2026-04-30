-- 1. Aseguramos que la columna 'servicio' tenga el Check Constraint correcto
-- (Primero borramos el anterior si existe para evitar duplicidad)
ALTER TABLE asignaciones
DROP CONSTRAINT IF EXISTS ck_tipo_servicio;

ALTER TABLE asignaciones
    ADD CONSTRAINT ck_tipo_servicio
        CHECK (servicio IN ('ENTRENAMIENTO', 'NUTRICION', 'AMBOS'));

-- 2. Limpieza de datos (OPCIONAL/SENIOR):
-- Si tienes datos duplicados que romperían el índice único parcial,
-- debes decidir si desactivarlos antes de crear el índice.
UPDATE asignaciones
SET activa = FALSE
WHERE id NOT IN (
    SELECT DISTINCT ON (atleta_id, servicio) id
FROM asignaciones
WHERE activa = TRUE
ORDER BY atleta_id, servicio, creada_en DESC
    );

-- 3. Aplicar el Índice Único Parcial
-- Borramos cualquier índice previo que pueda estar causando el error que mencionaste
DROP INDEX IF EXISTS unique_asignacion_activa;
DROP INDEX IF EXISTS unique_atleta_servicio_activo;

CREATE UNIQUE INDEX unique_atleta_servicio_activo
    ON asignaciones (atleta_id, servicio)
    WHERE (activa = TRUE);