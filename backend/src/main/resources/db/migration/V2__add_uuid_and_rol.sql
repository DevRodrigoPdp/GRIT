-- 1. Aseguramos que la extensión para generar UUIDs esté disponible (por si acaso)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Añadimos la columna 'rol' con un valor por defecto
ALTER TABLE users
    ADD COLUMN rol VARCHAR(20) DEFAULT 'USER' NOT NULL;

-- 3. Añadimos la columna 'uuid'
-- Usamos 'uuid_generate_v4()' para que los usuarios actuales reciban un ID único al momento
ALTER TABLE users
    ADD COLUMN uuid UUID DEFAULT gen_random_uuid() NOT NULL;

-- 4. (Opcional) Si quieres que el UUID sea único para búsquedas rápidas
ALTER TABLE users
    ADD CONSTRAINT uk_usuarios_uuid UNIQUE (uuid);

-- 5. Ejemplo: Convertir a tu usuario principal en ADMIN (cambia el email)
UPDATE users SET rol = 'ADMIN' WHERE email = 'kevin@gmail.com';