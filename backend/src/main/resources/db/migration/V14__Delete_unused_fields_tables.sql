-- 1. Asegurar que el nuevo campo existe antes de migrar datos
ALTER TABLE atletas ADD COLUMN IF NOT EXISTS restricciones_dieteticas TEXT;
ALTER TABLE atletas ADD COLUMN IF NOT EXISTS restricciones_fisicas TEXT;

-- 2. Migrar datos: Concatenar arreglos antiguos al nuevo formato de texto
UPDATE atletas
SET restricciones_dieteticas = 'Alergias: ' || array_to_string(alergias, ', ') ||
                               ' | Intolerancias: ' || array_to_string(intolerancias, ', ')
WHERE (alergias IS NOT NULL AND alergias != '{}')
   OR (intolerancias IS NOT NULL AND intolerancias != '{}');

-- 3. Limpieza de columnas (Sintaxis corregida: se repite DROP COLUMN)
ALTER TABLE atletas
DROP COLUMN IF EXISTS alergias,
    DROP COLUMN IF EXISTS intolerancias;

-- 4. Eliminación de tablas obsoletas (Schema Cleanup)
DROP TABLE IF EXISTS ejercicios_recientes;
DROP TABLE IF EXISTS alimentos_recientes;
DROP TABLE IF EXISTS notas_nutricionista;