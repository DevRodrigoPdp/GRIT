CREATE TABLE entrenamientos (
                                id BIGSERIAL PRIMARY KEY,
                                uuid UUID NOT NULL UNIQUE,
                                nombre VARCHAR(100) NOT NULL,
                                descripcion TEXT,
                                fecha_entrenamiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                duracion_minutos INTEGER,
                                usuario_id BIGINT NOT NULL,

    -- Relación con la tabla usuarios
                                CONSTRAINT fk_usuario_entrenamiento
                                    FOREIGN KEY (usuario_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);

-- Un índice para que las búsquedas por usuario sean ultra rápidas
CREATE INDEX idx_entrenamientos_usuario ON entrenamientos(usuario_id);