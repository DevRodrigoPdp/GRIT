-- 1. Tabla de Hilos (Contenedor principal)
CREATE TABLE hilos_comunicacion (
                                    id UUID PRIMARY KEY,
                                    atleta_id UUID NOT NULL,
                                    entrenador_id UUID NOT NULL,
                                    titulo VARCHAR(120) NOT NULL,
                                    categoria VARCHAR(20) NOT NULL, -- TECNICA, DUDA, APUNTE
                                    contexto VARCHAR(20) NOT NULL,  -- ENTRENAMIENTO, NUTRICION
                                    creado_por VARCHAR(20) NOT NULL, -- ENTRENADOR, ATLETA
                                    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_hilos_atleta FOREIGN KEY (atleta_id) REFERENCES usuarios(id),
                                    CONSTRAINT fk_hilos_entrenador FOREIGN KEY (entrenador_id) REFERENCES usuarios(id)
);

-- Índices para acelerar el filtrado por usuario en el dashboard
CREATE INDEX idx_hilos_atleta ON hilos_comunicacion(atleta_id);
CREATE INDEX idx_hilos_entrenador ON hilos_comunicacion(entrenador_id);

-- 2. Tabla de Mensajes
CREATE TABLE mensajes_hilo (
                               id UUID PRIMARY KEY,
                               hilo_id UUID NOT NULL,
                               texto TEXT, -- Puede ser null si solo se envían archivos
                               enviado_por VARCHAR(20) NOT NULL,
                               enviado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_mensajes_hilo FOREIGN KEY (hilo_id) REFERENCES hilos_comunicacion(id) ON DELETE CASCADE
);

-- 3. Tabla de Adjuntos (S3 Metadata)
CREATE TABLE adjuntos_mensaje (
                                  id UUID PRIMARY KEY,
                                  mensaje_id UUID NOT NULL,
                                  s3_key VARCHAR(255) NOT NULL,
                                  tipo VARCHAR(20) NOT NULL, -- IMAGEN, VIDEO
                                  nombre_original VARCHAR(255) NOT NULL,

                                  CONSTRAINT fk_adjuntos_mensaje FOREIGN KEY (mensaje_id) REFERENCES mensajes_hilo(id) ON DELETE CASCADE
);

-- 4. Tabla de Control de Lectura (Optimización de UI)
CREATE TABLE lecturas_hilo (
                               hilo_id UUID NOT NULL,
                               usuario_id UUID NOT NULL,
                               leido_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               PRIMARY KEY (hilo_id, usuario_id),
                               CONSTRAINT fk_lecturas_hilo FOREIGN KEY (hilo_id) REFERENCES hilos_comunicacion(id) ON DELETE CASCADE,
                               CONSTRAINT fk_lecturas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);