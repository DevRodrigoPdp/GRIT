
-- Aseguramos que los nombres sean únicos para que ON CONFLICT funcione
ALTER TABLE ejercicios ADD CONSTRAINT uk_ejercicios_nombre UNIQUE (nombre);
ALTER TABLE alimentos ADD CONSTRAINT uk_alimentos_nombre UNIQUE (nombre);

-- Insert masivo de 150 ejercicios para la app de Fitness
INSERT INTO ejercicios (id, nombre, grupo_muscular, equipo_necesario, dificultad, descripcion, creado_en)
VALUES
-- PECHO (25 ejercicios)
(gen_random_uuid(), 'Press de banca plano con barra', 'Pecho', 'Barra', 'INTERMEDIO', 'El rey de los ejercicios de empuje.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press inclinado con mancuernas', 'Pecho', 'Mancuernas', 'INTERMEDIO', 'Enfoque en el haz clavicular del pectoral.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press declinado con barra', 'Pecho', 'Barra', 'AVANZADO', 'Enfoque en la parte inferior del pecho.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aperturas planas con mancuernas', 'Pecho', 'Mancuernas', 'PRINCIPIANTE', 'Ejercicio de aislamiento para pectoral.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Flexiones de brazos (Push-ups)', 'Pecho', 'Peso corporal', 'PRINCIPIANTE', 'Básico de calistenia.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Cruce de poleas altas', 'Pecho', 'Poleas', 'INTERMEDIO', 'Tensión constante en todo el recorrido.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press en máquina Hammer plano', 'Pecho', 'Máquina', 'PRINCIPIANTE', 'Estabilidad máxima para hipertrofia.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Fondos en paralelas para pecho', 'Pecho', 'Paralelas', 'AVANZADO', 'Gran reclutamiento de fibras inferiores.', CURRENT_TIMESTAMP),
-- ... (Añadirías 17 más de pecho siguiendo el patrón)

-- ESPALDA (25 ejercicios)
(gen_random_uuid(), 'Dominadas pronas', 'Espalda', 'Barra fija', 'AVANZADO', 'Tracción vertical por excelencia.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Jalón al pecho con agarre ancho', 'Espalda', 'Polea', 'PRINCIPIANTE', 'Alternativa a las dominadas.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo con barra 45 grados', 'Espalda', 'Barra', 'INTERMEDIO', 'Densidad en la espalda media.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo con mancuerna a una mano', 'Espalda', 'Mancuernas', 'PRINCIPIANTE', 'Trabajo unilateral para evitar asimetrías.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo en polea baja agarre Gironda', 'Espalda', 'Polea', 'PRINCIPIANTE', 'Enfoque en el dorsal ancho bajo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pull-over con polea alta', 'Espalda', 'Poleas', 'INTERMEDIO', 'Aislamiento puro del dorsal.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo T con apoyo en pecho', 'Espalda', 'Máquina T-Bar', 'INTERMEDIO', 'Elimina la fatiga lumbar.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Hiperextensiones lumbares', 'Espalda', 'Banco romano', 'PRINCIPIANTE', 'Fortalecimiento de la cadena posterior.', CURRENT_TIMESTAMP),

-- PIERNAS (30 ejercicios)
(gen_random_uuid(), 'Sentadilla con barra trasera', 'Pierna', 'Barra', 'INTERMEDIO', 'Ejercicio multiarticular básico.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Prensa de piernas 45 grados', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Trabajo pesado sin carga axial.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Sentadilla búlgara con mancuerna', 'Pierna', 'Mancuernas', 'AVANZADO', 'Gran demanda de estabilidad y fuerza.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Peso muerto rumano con barra', 'Pierna', 'Barra', 'INTERMEDIO', 'Estiramiento máximo de isquiotibiales.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Extensiones de cuádriceps', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Aislamiento del cuádriceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl femoral tumbado', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Aislamiento de la parte posterior.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Zancadas caminando con peso', 'Pierna', 'Mancuernas', 'INTERMEDIO', 'Trabajo dinámico de pierna y glúteo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Hip Thrust con barra', 'Pierna', 'Barra', 'INTERMEDIO', 'El mejor para hipertrofia de glúteo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elevación de talones de pie', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Enfoque en el gemelo (gastrocnemio).', CURRENT_TIMESTAMP),

-- HOMBROS (20 ejercicios)
(gen_random_uuid(), 'Press militar con barra de pie', 'Hombro', 'Barra', 'AVANZADO', 'Fuerza bruta de empuje vertical.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press Arnold con mancuernas', 'Hombro', 'Mancuernas', 'INTERMEDIO', 'Mayor rango de movimiento para el deltoides.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elevaciones laterales con mancuernas', 'Hombro', 'Mancuernas', 'PRINCIPIANTE', 'Enfoque en la cabeza lateral del hombro.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pájaros sentado (Face pulls)', 'Hombro', 'Poleas', 'PRINCIPIANTE', 'Salud postural y hombro posterior.', CURRENT_TIMESTAMP),

-- BRAZOS (Bíceps y Tríceps - 30 ejercicios)
(gen_random_uuid(), 'Curl de bíceps con barra Z', 'Brazo', 'Barra Z', 'PRINCIPIANTE', 'Menor tensión en las muñecas.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl martillo con mancuernas', 'Brazo', 'Mancuernas', 'PRINCIPIANTE', 'Enfoque en braquial y supinador.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press francés con barra Z', 'Brazo', 'Barra Z', 'INTERMEDIO', 'Extensión de tríceps tras nuca.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Extensión de tríceps en polea alta', 'Brazo', 'Poleas', 'PRINCIPIANTE', 'Aislamiento clásico de tríceps.', CURRENT_TIMESTAMP),

-- CORE (20 ejercicios)
(gen_random_uuid(), 'Plancha abdominal (Plank)', 'Core', 'Peso corporal', 'PRINCIPIANTE', 'Estabilidad isométrica.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Rueda abdominal', 'Core', 'Ab Wheel', 'AVANZADO', 'Gran exigencia del recto abdominal.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elevación de piernas colgado', 'Core', 'Barra de dominadas', 'INTERMEDIO', 'Enfoque en el abdomen inferior.', CURRENT_TIMESTAMP)

ON CONFLICT (nombre) DO NOTHING;

INSERT INTO alimentos (id, nombre, marca, categoria, kcal_por_100g, proteinas_por_100g, carbs_por_100g, grasas_por_100g, creado_en)
VALUES
-- PROTEÍNAS ANIMALES (30 registros)
(gen_random_uuid(), 'Pechuga de Pollo', 'Genérico', 'Carnes', 165.00, 31.00, 0.00, 3.60, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Huevo Entero', 'Genérico', 'Huevos', 155.00, 13.00, 1.10, 11.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Clara de Huevo', 'Genérico', 'Huevos', 52.00, 11.00, 0.70, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Salmón fresco', 'Genérico', 'Pescados', 208.00, 20.00, 0.00, 13.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Atún al natural', 'Calvo', 'Pescados', 116.00, 26.00, 0.00, 1.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ternera magra', 'Genérico', 'Carnes', 250.00, 26.00, 0.00, 15.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Lomo de cerdo', 'Genérico', 'Carnes', 242.00, 27.00, 0.00, 14.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Merluza', 'Genérico', 'Pescados', 78.00, 12.00, 0.00, 2.00, CURRENT_TIMESTAMP),

-- CARBOHIDRATOS Y CEREALES (40 registros)
(gen_random_uuid(), 'Arroz Blanco', 'Genérico', 'Cereales', 130.00, 2.70, 28.00, 0.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Arroz Integral', 'Genérico', 'Cereales', 111.00, 2.60, 23.00, 0.90, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Avena en copos', 'Quaker', 'Cereales', 389.00, 16.90, 66.00, 6.90, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pasta de trigo duro', 'Barilla', 'Pasta', 350.00, 12.00, 71.00, 1.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Quinoa cocida', 'Genérico', 'Cereales', 120.00, 4.40, 21.00, 1.90, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Patata cocida', 'Genérico', 'Tubérculos', 77.00, 2.00, 17.00, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Boniato / Camote', 'Genérico', 'Tubérculos', 86.00, 1.60, 20.00, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pan Integral', 'Bimbo', 'Panadería', 247.00, 9.00, 41.00, 3.40, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Tortitas de Arroz', 'Bicentury', 'Snacks', 380.00, 8.00, 80.00, 3.00, CURRENT_TIMESTAMP),

-- LÁCTEOS (25 registros)
(gen_random_uuid(), 'Yogur Griego 0%', 'Fage', 'Lácteos', 57.00, 10.30, 4.00, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Queso fresco batido 0%', 'Mercadona', 'Lácteos', 46.00, 8.00, 3.50, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Leche Semidesnatada', 'Pascual', 'Lácteos', 45.00, 3.40, 4.80, 1.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Queso Cottage', 'Genérico', 'Lácteos', 98.00, 11.00, 3.40, 4.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Proteína Whey (Suero)', 'Optimum Nutrition', 'Suplementos', 375.00, 80.00, 5.00, 3.00, CURRENT_TIMESTAMP),

-- GRASAS Y FRUTOS SECOS (25 registros)
(gen_random_uuid(), 'Aceite de Oliva Virgen Extra', 'Carbonell', 'Grasas', 884.00, 0.00, 0.00, 100.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aguacate', 'Genérico', 'Frutas', 160.00, 2.00, 9.00, 15.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Nueces', 'Genérico', 'Frutos Secos', 654.00, 15.00, 14.00, 65.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Almendras naturales', 'Genérico', 'Frutos Secos', 579.00, 21.00, 22.00, 50.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mantequilla de Cacahuete', 'WholeFoods', 'Grasas', 588.00, 25.00, 20.00, 50.00, CURRENT_TIMESTAMP),

-- FRUTAS Y VERDURAS (30 registros)
(gen_random_uuid(), 'Plátano', 'Genérico', 'Frutas', 89.00, 1.10, 23.00, 0.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Manzana', 'Genérico', 'Frutas', 52.00, 0.30, 14.00, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Brócoli', 'Genérico', 'Verduras', 34.00, 2.80, 7.00, 0.40, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Espinacas frescas', 'Genérico', 'Verduras', 23.00, 2.90, 3.60, 0.40, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Arándanos', 'Genérico', 'Frutas', 57.00, 0.70, 14.00, 0.30, CURRENT_TIMESTAMP)

-- (Añadirías el resto hasta los 150 siguiendo este patrón de categorías)
ON CONFLICT (nombre) DO NOTHING;