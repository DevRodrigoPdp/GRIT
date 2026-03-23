CREATE TABLE ejercicios (
                            id BIGSERIAL PRIMARY KEY,
                            uuid UUID NOT NULL UNIQUE,
                            nombre VARCHAR(100) NOT NULL,
                            series INTEGER NOT NULL,
                            repeticiones INTEGER NOT NULL,
                            peso_kg DOUBLE PRECISION NOT NULL,
                            entrenamiento_id BIGINT NOT NULL,
                            CONSTRAINT fk_ejercicio_entrenamiento
                                FOREIGN KEY(entrenamiento_id)
                                    REFERENCES entrenamientos(id)
                                    ON DELETE CASCADE
);