CREATE TABLE departamentos (
                               id SERIAL PRIMARY KEY,
                               nombre VARCHAR(100) NOT NULL,
                               codigo VARCHAR(10) UNIQUE NOT NULL
);

CREATE TABLE empleados (
                           id SERIAL PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           email VARCHAR(100) UNIQUE NOT NULL,
                           departamento_id INTEGER,
                           CONSTRAINT fk_empleado_depto FOREIGN KEY (departamento_id) REFERENCES departamentos(id)
);

-- V1__create_users_table.sql
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       enabled BOOLEAN DEFAULT TRUE
);