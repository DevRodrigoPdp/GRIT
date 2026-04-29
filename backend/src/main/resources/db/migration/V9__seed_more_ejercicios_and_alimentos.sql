INSERT INTO ejercicios (id, nombre, grupo_muscular, equipo_necesario, dificultad, descripcion, creado_en) VALUES
-- PECHO
(gen_random_uuid(), 'Press de banca con mancuernas', 'Pecho', 'Mancuernas', 'INTERMEDIO', 'Mayor rango de movimiento que la barra.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aperturas inclinadas', 'Pecho', 'Mancuernas', 'INTERMEDIO', 'Enfoque en pectoral superior.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Cruce de poleas bajas', 'Pecho', 'Poleas', 'INTERMEDIO', 'Enfoque en la parte superior y separación.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Flexiones diamante', 'Pecho', 'Peso corporal', 'INTERMEDIO', 'Enfoque en tríceps y parte interna.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press sentado en máquina', 'Pecho', 'Máquina', 'PRINCIPIANTE', 'Control total del movimiento.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pullover con mancuerna', 'Pecho', 'Mancuernas', 'INTERMEDIO', 'Expansión torácica y pectoral.', CURRENT_TIMESTAMP),
-- ESPALDA
(gen_random_uuid(), 'Dominadas supinas', 'Espalda', 'Barra fija', 'INTERMEDIO', 'Mayor involucración del bíceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo con barra Pendlay', 'Espalda', 'Barra', 'AVANZADO', 'Remo explosivo desde el suelo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Jalón al pecho agarre V', 'Espalda', 'Polea', 'PRINCIPIANTE', 'Densidad central de la espalda.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo en máquina convergente', 'Espalda', 'Máquina', 'PRINCIPIANTE', 'Trabajo unilateral guiado.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Peso muerto convencional', 'Espalda', 'Barra', 'AVANZADO', 'Constructor de masa global.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Face Pulls', 'Espalda', 'Poleas', 'PRINCIPIANTE', 'Salud del hombro y trapecio medio.', CURRENT_TIMESTAMP),
-- PIERNAS
(gen_random_uuid(), 'Sentadilla Frontal', 'Pierna', 'Barra', 'AVANZADO', 'Énfasis en cuádriceps y core.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Hack Squat', 'Pierna', 'Máquina', 'INTERMEDIO', 'Estabilidad para carga pesada.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Step-up con mancuerna', 'Pierna', 'Mancuernas', 'INTERMEDIO', 'Trabajo unilateral de glúteo y pierna.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aductores en máquina', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Fortalecimiento de la cara interna.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Peso muerto sumo', 'Pierna', 'Barra', 'AVANZADO', 'Menor carga lumbar, más aductores.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl femoral sentado', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Aislamiento de isquios.', CURRENT_TIMESTAMP),
-- HOMBROS Y BRAZOS
(gen_random_uuid(), 'Press militar sentado', 'Hombro', 'Mancuernas', 'INTERMEDIO', 'Estabilidad del deltoides.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elevaciones frontales', 'Hombro', 'Mancuernas', 'PRINCIPIANTE', 'Aislamiento deltoides anterior.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl araña', 'Brazo', 'Barra Z', 'INTERMEDIO', 'Aislamiento del bíceps sin balanceo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Extensiones tras nuca', 'Brazo', 'Mancuernas', 'PRINCIPIANTE', 'Cabeza larga del tríceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl predicador', 'Brazo', 'Banco Scott', 'INTERMEDIO', 'Enfoque en el pico del bíceps.', CURRENT_TIMESTAMP),
-- CORE Y OTROS
(gen_random_uuid(), 'Plancha lateral', 'Core', 'Peso corporal', 'PRINCIPIANTE', 'Oblicuos y estabilidad.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mountain climbers', 'Core', 'Peso corporal', 'INTERMEDIO', 'Cardio y abdomen.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Burpees', 'Full Body', 'Peso corporal', 'AVANZADO', 'Capacidad cardiovascular máxima.', CURRENT_TIMESTAMP),
-- HOMBROS (Específicos)
    (gen_random_uuid(), 'Press Bradford', 'Hombro', 'Barra', 'AVANZADO', 'Movimiento combinado por delante y detrás de la nuca.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elevaciones tipo Y', 'Hombro', 'Mancuernas', 'INTERMEDIO', 'Enfoque en deltoides posterior y trapecio inferior.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press Landmine a una mano', 'Hombro', 'Barra Landmine', 'INTERMEDIO', 'Empuje diagonal excelente para salud de hombro.', CURRENT_TIMESTAMP),
-- PIERNAS (Potencia y Aislamiento)
(gen_random_uuid(), 'Sentadilla Zercher', 'Pierna', 'Barra', 'AVANZADO', 'Carga frontal en los codos, gran demanda de core.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Sissy Squat', 'Pierna', 'Peso corporal', 'INTERMEDIO', 'Aislamiento extremo de cuádriceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Paseo del granjero (Farmer Walk)', 'Full Body', 'Mancuernas pesadas', 'PRINCIPIANTE', 'Fuerza de agarre y estabilidad total.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Subidas al cajón (Step-ups)', 'Pierna', 'Cajón', 'PRINCIPIANTE', 'Trabajo unilateral de potencia.', CURRENT_TIMESTAMP),
-- BRAZOS (Detalle)
(gen_random_uuid(), 'Curl Zottman', 'Brazo', 'Mancuernas', 'INTERMEDIO', 'Trabaja bíceps en la subida y braquial en la bajada.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press cerrado (Close grip bench)', 'Brazo', 'Barra', 'INTERMEDIO', 'Press de banca enfocado en tríceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl Spider en banco inclinado', 'Brazo', 'Barra Z', 'INTERMEDIO', 'Elimina el balanceo por completo.', CURRENT_TIMESTAMP),
-- CALISTENIA / CORE
(gen_random_uuid(), 'Dragon Flag', 'Core', 'Banco', 'AVANZADO', 'Ejercicio abdominal popularizado por Bruce Lee.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'L-Sit en paralelas', 'Core', 'Paralelas', 'AVANZADO', 'Isométrico de altísima intensidad.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Toes to Bar', 'Core', 'Barra dominadas', 'INTERMEDIO', 'Dinámico de abdomen y flexores de cadera.', CURRENT_TIMESTAMP),
-- PIERNA (Nivel Pro)
(gen_random_uuid(), 'Sentadilla Cosaca', 'Pierna', 'Peso corporal', 'INTERMEDIO', 'Movilidad lateral de cadera y fuerza de aductores.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pistol Squat', 'Pierna', 'Peso corporal', 'AVANZADO', 'Sentadilla a una pierna, máxima demanda de equilibrio.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Nordic Curl', 'Pierna', 'Banco', 'AVANZADO', 'Excéntrico puro para isquiotibiales.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Box Jump (Salto al cajón)', 'Pierna', 'Cajón', 'INTERMEDIO', 'Potencia explosiva y pliometría.', CURRENT_TIMESTAMP),
-- BRAZOS Y TRACCIÓN
(gen_random_uuid(), 'Muscle Up', 'Full Body', 'Barra fija', 'AVANZADO', 'Transición explosiva de tracción a empuje.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo al mentón con barra Z', 'Hombro', 'Barra Z', 'INTERMEDIO', 'Enfoque en deltoides lateral y trapecio.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Dips en anillas', 'Pecho', 'Anillas', 'AVANZADO', 'Inestabilidad máxima para el pectoral.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl de bíceps en martillo cruzado', 'Brazo', 'Mancuernas', 'PRINCIPIANTE', 'Enfoque en el braquial y supinador largo.', CURRENT_TIMESTAMP),
-- CORE Y ESTABILIDAD
(gen_random_uuid(), 'Hollow Hold', 'Core', 'Peso corporal', 'PRINCIPIANTE', 'Activación profunda del transverso abdominal.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ab Wheel Rollout', 'Core', 'Rueda abdominal', 'AVANZADO', 'Antiextensión lumbar de alta intensidad.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Paseo del granjero unilateral', 'Core', 'Mancuernas', 'INTERMEDIO', 'Estabilidad lateral y fuerza de agarre.', CURRENT_TIMESTAMP),
-- CARDIO / HIIT
(gen_random_uuid(), 'Battle Ropes', 'Full Body', 'Cuerdas', 'INTERMEDIO', 'Resistencia muscular y cardiovascular.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Wall Balls', 'Full Body', 'Balón medicinal', 'INTERMEDIO', 'Coordinación y potencia tren superior/inferior.', CURRENT_TIMESTAMP),
-- MOVILIDAD Y YOGA (Nivel necesario para apps completas)
(gen_random_uuid(), 'Saludo al Sol A', 'Full Body', 'Esterilla', 'PRINCIPIANTE', 'Secuencia fluida de vinyasa yoga.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Postura del Cuervo (Bakasana)', 'Core', 'Peso corporal', 'AVANZADO', 'Equilibrio sobre brazos que requiere gran fuerza de core.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Gato-Camello', 'Core', 'Peso corporal', 'PRINCIPIANTE', 'Movilidad de la columna vertebral.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Copenhague Plank', 'Pierna', 'Banco', 'AVANZADO', 'Fortalecimiento intenso de aductores.', CURRENT_TIMESTAMP),
-- VARIANTES DE AISLAMIENTO
(gen_random_uuid(), 'Curl de bíceps en polea baja con cuerda', 'Brazo', 'Poleas', 'PRINCIPIANTE', 'Tensión constante y mayor libertad de giro.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Patada de glúteo en polea', 'Pierna', 'Poleas', 'PRINCIPIANTE', 'Aislamiento máximo del glúteo mayor.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Face Pull con agarre prono', 'Hombro', 'Poleas', 'INTERMEDIO', 'Enfoque en deltoides posterior y rotadores.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Sissy Squat', 'Pierna', 'Peso corporal', 'AVANZADO', 'Aislamiento extremo de cuádriceps sin máquinas.', CURRENT_TIMESTAMP),
-- CALISTENIA AVANZADA
(gen_random_uuid(), 'Dragon Flag', 'Core', 'Banco', 'AVANZADO', 'Ejercicio de core popularizado por Bruce Lee.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Front Lever (Progreso)', 'Espalda', 'Barra fija', 'AVANZADO', 'Isométrico de altísima intensidad dorsal.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Handstand Push-ups', 'Hombro', 'Pared', 'AVANZADO', 'Fuerza de empuje vertical invertida.', CURRENT_TIMESTAMP),
-- CARDIO ESPECÍFICO
(gen_random_uuid(), 'Sprint en cinta', 'Cardio', 'Cinta de correr', 'INTERMEDIO', 'Entrenamiento de intervalos de alta intensidad (HIIT).', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elíptica (Resistencia alta)', 'Cardio', 'Máquina', 'PRINCIPIANTE', 'Bajo impacto articular, alta quema calórica.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Paseo del granjero con pesas rusas', 'Full Body', 'Kettlebell', 'INTERMEDIO', 'Fuerza de agarre y estabilidad de core.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Swing con Kettlebell a una mano', 'Full Body', 'Kettlebell', 'AVANZADO', 'Potencia de cadera con componente anti-rotacional.', CURRENT_TIMESTAMP),
-- ESTIRAMIENTOS
(gen_random_uuid(), 'Estiramiento de Psoas', 'Pierna', 'Peso corporal', 'PRINCIPIANTE', 'Vital para personas que pasan mucho tiempo sentadas.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Estiramiento de Pectoral en marco de puerta', 'Pecho', 'Ninguno', 'PRINCIPIANTE', 'Apertura torácica y mejora postural.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Cuerda de saltar (Doble bajo)', 'Cardio', 'Cuerda', 'AVANZADO', 'Salto doble en una sola revolución.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Shadow Boxing con mancuernas ligeras', 'Cardio', 'Mancuernas', 'INTERMEDIO', 'Mejora de velocidad y resistencia de hombros.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Zancada atrás con déficit', 'Pierna', 'Step', 'INTERMEDIO', 'Mayor rango de movimiento para el glúteo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elevación de piernas colgado', 'Core', 'Barra de dominadas', 'INTERMEDIO', 'Enfoque en el abdomen inferior.', CURRENT_TIMESTAMP),
-- PECHO (Ampliación)
(gen_random_uuid(), 'Press de banca con mancuernas', 'Pecho', 'Mancuernas', 'INTERMEDIO', 'Mayor rango de movimiento y estabilización.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aperturas inclinadas con mancuernas', 'Pecho', 'Mancuernas', 'INTERMEDIO', 'Enfoque en el haz clavicular del pectoral.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Cruce de poleas bajas', 'Pecho', 'Poleas', 'INTERMEDIO', 'Enfoque en la parte superior del pecho.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Flexiones diamante', 'Pecho', 'Peso corporal', 'INTERMEDIO', 'Enfoque en tríceps y parte interna del pecho.', CURRENT_TIMESTAMP),
-- ESPALDA (Ampliación)
(gen_random_uuid(), 'Remo con barra Pendlay', 'Espalda', 'Barra', 'AVANZADO', 'Remo explosivo desde el suelo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Jalón al pecho agarre estrecho neutro', 'Espalda', 'Polea', 'PRINCIPIANTE', 'Gran estiramiento del dorsal ancho.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pull-over con mancuerna', 'Espalda', 'Mancuernas', 'INTERMEDIO', 'Expansión torácica y trabajo de dorsal.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo Meadows', 'Espalda', 'Barra Landmine', 'AVANZADO', 'Variante de remo unilateral para grosor.', CURRENT_TIMESTAMP),
-- PIERNAS (Ampliación)
(gen_random_uuid(), 'Sentadilla Frontal con barra', 'Pierna', 'Barra', 'AVANZADO', 'Máxima demanda de core y cuádriceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Hack Squat en máquina', 'Pierna', 'Máquina', 'INTERMEDIO', 'Aislamiento de pierna con espalda protegida.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Peso muerto sumo con barra', 'Pierna', 'Barra', 'AVANZADO', 'Enfoque en aductores y glúteos.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Abducción de cadera en máquina', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Fortalecimiento del glúteo medio.', CURRENT_TIMESTAMP),
-- BRAZOS Y HOMBROS (Ampliación)
(gen_random_uuid(), 'Press militar sentado con mancuernas', 'Hombro', 'Mancuernas', 'INTERMEDIO', 'Estabilidad del deltoides.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Face Pulls con cuerda', 'Hombro', 'Poleas', 'PRINCIPIANTE', 'Salud del hombro posterior.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl araña en banco inclinado', 'Brazo', 'Barra Z', 'INTERMEDIO', 'Aislamiento máximo de la cabeza corta del bíceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Extensiones de tríceps tras nuca con cuerda', 'Brazo', 'Poleas', 'PRINCIPIANTE', 'Enfoque en la cabeza larga del tríceps.', CURRENT_TIMESTAMP),
-- CORE Y CARDIO
(gen_random_uuid(), 'Deadbug', 'Core', 'Peso corporal', 'PRINCIPIANTE', 'Estabilidad lumbar y coordinación.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Burpees', 'Full Body', 'Peso corporal', 'AVANZADO', 'Ejercicio metabólico de alta intensidad.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Salto a la comba', 'Full Body', 'Cuerda', 'PRINCIPIANTE', 'Mejora de coordinación y resistencia.', CURRENT_TIMESTAMP),
-- PECHO
(gen_random_uuid(), 'Press inclinado con barra', 'Pecho', 'Barra', 'INTERMEDIO', 'Enfoque principal en la parte superior del pectoral.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aperturas en máquina (Peck Deck)', 'Pecho', 'Máquina', 'PRINCIPIANTE', 'Aislamiento seguro para el pectoral mayor.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Flexiones diamante', 'Pecho', 'Peso corporal', 'INTERMEDIO', 'Enfoque intenso en tríceps y centro del pecho.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Dips (Fondos) en anillas', 'Pecho', 'Anillas', 'AVANZADO', 'Máxima demanda de estabilidad y fuerza.', CURRENT_TIMESTAMP),
-- ESPALDA
(gen_random_uuid(), 'Remo con barra T', 'Espalda', 'Barra T', 'INTERMEDIO', 'Excelente para el grosor de la espalda media.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Jalón tras nuca', 'Espalda', 'Polea', 'INTERMEDIO', 'Variante clásica para amplitud dorsal.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo unilateral en polea baja', 'Espalda', 'Poleas', 'PRINCIPIANTE', 'Trabajo de tracción con rango extendido.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Dominadas con lastre', 'Espalda', 'Barra fija', 'AVANZADO', 'Progresión de fuerza para dominadas clásicas.', CURRENT_TIMESTAMP),
-- PIERNAS
(gen_random_uuid(), 'Prensa horizontal', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Ideal para trabajo de volumen sin carga lumbar.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aductores en máquina', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Aislamiento de la cara interna del muslo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Abductores en máquina', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Trabajo específico de glúteo medio.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elevación de talones sentado', 'Pierna', 'Máquina', 'PRINCIPIANTE', 'Enfoque en el músculo sóleo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Zancadas laterales', 'Pierna', 'Mancuernas', 'INTERMEDIO', 'Trabajo de piernas en el plano frontal.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Sentadilla Hack', 'Pierna', 'Máquina', 'INTERMEDIO', 'Estabilidad total para enfatizar cuádriceps.', CURRENT_TIMESTAMP),
-- HOMBROS
(gen_random_uuid(), 'Press militar sentado con mancuernas', 'Hombro', 'Mancuernas', 'INTERMEDIO', 'Gran rango de movimiento para deltoides.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Elevaciones frontales con disco', 'Hombro', 'Disco', 'PRINCIPIANTE', 'Aislamiento deltoides anterior.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo al mentón con barra Z', 'Hombro', 'Barra Z', 'INTERMEDIO', 'Enfoque en deltoides lateral y trapecios.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Face Pull con cuerda', 'Hombro', 'Poleas', 'PRINCIPIANTE', 'Vital para salud de hombro y deltoides posterior.', CURRENT_TIMESTAMP),
-- BRAZOS
(gen_random_uuid(), 'Curl predicador', 'Brazo', 'Banco Scott', 'INTERMEDIO', 'Aislamiento estricto del bíceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Press francés con mancuernas', 'Brazo', 'Mancuernas', 'INTERMEDIO', 'Evita el dolor de codos de la barra recta.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Patada de tríceps en polea', 'Brazo', 'Poleas', 'PRINCIPIANTE', 'Contracción máxima de la cabeza lateral.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Curl concentrado', 'Brazo', 'Mancuernas', 'PRINCIPIANTE', 'Clásico para buscar el "pico" del bíceps.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Extensión de tríceps con cuerda tras nuca', 'Brazo', 'Poleas', 'INTERMEDIO', 'Máximo estiramiento de la cabeza larga.', CURRENT_TIMESTAMP),
-- CORE
(gen_random_uuid(), 'Deadbug', 'Core', 'Peso corporal', 'PRINCIPIANTE', 'Estabilidad lumbopélvica profunda.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Bird-dog', 'Core', 'Peso corporal', 'PRINCIPIANTE', 'Coordinación y fortalecimiento de cadena posterior.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Russian Twists', 'Core', 'Disco', 'PRINCIPIANTE', 'Trabajo rotacional de oblicuos.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Plancha lateral', 'Core', 'Peso corporal', 'PRINCIPIANTE', 'Estabilidad lateral y cuadrado lumbar.', CURRENT_TIMESTAMP),
-- FULL BODY / CARDIO
(gen_random_uuid(), 'Kettlebell Swing', 'Full Body', 'Kettlebell', 'INTERMEDIO', 'Potencia de cadera y resistencia.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Salto al cajón (Box Jump)', 'Full Body', 'Cajón', 'INTERMEDIO', 'Potencia explosiva pliométrica.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mountain Climbers', 'Full Body', 'Peso corporal', 'PRINCIPIANTE', 'Resistencia cardiovascular y core.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Thrusters', 'Full Body', 'Barra', 'AVANZADO', 'Combinación de sentadilla y press militar.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Clean & Press', 'Full Body', 'Barra', 'AVANZADO', 'Movimiento de potencia olímpico.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Jumping Jacks', 'Cardio', 'Peso corporal', 'PRINCIPIANTE', 'Básico de calentamiento cardiovascular.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Remo en máquina (Ergómetro)', 'Cardio', 'Máquina', 'INTERMEDIO', 'Resistencia de cuerpo completo.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Wall Balls', 'Full Body', 'Balón medicinal', 'INTERMEDIO', 'Potencia y resistencia aeróbica.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Bear Crawl (Paso del oso)', 'Full Body', 'Peso corporal', 'INTERMEDIO', 'Movilidad y estabilidad dinámica.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Zancadas con salto', 'Pierna', 'Peso corporal', 'AVANZADO', 'Pliometría intensa para tren inferior.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Muscle Up en barra', 'Full Body', 'Barra fija', 'AVANZADO', 'Hito de calistenia: tracción + empuje.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Shadow Boxing', 'Cardio', 'Ninguno', 'PRINCIPIANTE', 'Trabajo de agilidad y coordinación.', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Battle Ropes', 'Full Body', 'Cuerdas', 'INTERMEDIO', 'Resistencia muscular en brazos y core.', CURRENT_TIMESTAMP)

    ON CONFLICT (nombre) DO NOTHING;


INSERT INTO alimentos (id, nombre, marca, categoria, kcal_por_100g, proteinas_por_100g, carbs_por_100g, grasas_por_100g, creado_en) VALUES
-- PROTEÍNAS
(gen_random_uuid(), 'Pavo fiambre 92%', 'Campofrío', 'Carnes', 90.00, 21.00, 1.00, 1.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Queso Tofu firme', 'Genérico', 'Legumbres', 80.00, 9.00, 2.00, 4.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Seitán orgánico', 'Soria Natural', 'Vegetal', 121.00, 24.00, 3.50, 1.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Gambas peladas', 'Pescanova', 'Pescados', 71.00, 18.00, 0.00, 1.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Bacalao fresco', 'Genérico', 'Pescados', 82.00, 18.00, 0.00, 0.70, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Lomo de embuchado', 'Genérico', 'Embutidos', 200.00, 32.00, 1.00, 8.00, CURRENT_TIMESTAMP),
-- CARBOHIDRATOS
(gen_random_uuid(), 'Cuscús cocido', 'Genérico', 'Cereales', 112.00, 3.80, 23.00, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pan de Centeno 100%', 'Genérico', 'Panadería', 259.00, 8.50, 48.00, 3.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Lentejas cocidas', 'Luengo', 'Legumbres', 116.00, 9.00, 20.00, 0.40, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Garbanzos cocidos', 'Luengo', 'Legumbres', 164.00, 8.90, 27.00, 2.60, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Yuca cocida', 'Genérico', 'Tubérculos', 160.00, 1.40, 38.00, 0.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Harina de Avena', 'Koro', 'Cereales', 370.00, 13.00, 60.00, 7.00, CURRENT_TIMESTAMP),
-- LÁCTEOS Y GRASAS
(gen_random_uuid(), 'Kéfir natural', 'Pastoret', 'Lácteos', 60.00, 3.50, 4.00, 3.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Skyr natural', 'Isey', 'Lácteos', 63.00, 11.00, 4.00, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Nueces de California', 'Genérico', 'Frutos Secos', 654.00, 15.00, 14.00, 65.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Semillas de Chía', 'Genérico', 'Superalimentos', 486.00, 17.00, 42.00, 31.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aceite de Coco', 'Genérico', 'Grasas', 862.00, 0.00, 0.00, 100.00, CURRENT_TIMESTAMP),
-- FRUTAS Y VERDURAS
(gen_random_uuid(), 'Kiwi Zespri', 'Zespri', 'Frutas', 61.00, 1.10, 15.00, 0.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Arándanos frescos', 'Genérico', 'Frutas', 57.00, 0.70, 14.00, 0.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Espárragos trigueros', 'Genérico', 'Verduras', 20.00, 2.20, 3.90, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pimiento Rojo', 'Genérico', 'Verduras', 31.00, 1.00, 6.00, 0.30, CURRENT_TIMESTAMP),
-- SUPLEMENTACIÓN (Alta Densidad)
(gen_random_uuid(), 'Aislado de Suero (Isolate)', 'MyProtein', 'Suplementos', 370.00, 90.00, 1.00, 0.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Caseína Micelar', 'Optimum Nutrition', 'Suplementos', 360.00, 80.00, 4.00, 1.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Creatina Monohidrato', 'Creapure', 'Suplementos', 0.00, 0.00, 0.00, 0.00, CURRENT_TIMESTAMP),
-- KETO / GRASAS SALUDABLES
(gen_random_uuid(), 'Aceitunas negras', 'Genérico', 'Grasas', 115.00, 0.80, 6.00, 10.70, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mantequilla de Almendras', 'Koro', 'Grasas', 614.00, 21.00, 10.00, 55.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pescado Espada', 'Genérico', 'Pescados', 144.00, 20.00, 0.00, 6.70, CURRENT_TIMESTAMP),
-- VEGANO / PROTEÍNA VEGETAL
(gen_random_uuid(), 'Tempeh fermentado', 'Genérico', 'Legumbres', 193.00, 19.00, 9.00, 11.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Levadura Nutricional', 'Genérico', 'Superalimentos', 340.00, 50.00, 15.00, 4.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Edamame cocido', 'Genérico', 'Legumbres', 122.00, 11.00, 10.00, 5.00, CURRENT_TIMESTAMP),
-- FRUTAS Y VARIOS
(gen_random_uuid(), 'Papaya', 'Genérico', 'Frutas', 43.00, 0.50, 11.00, 0.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Granada', 'Genérico', 'Frutas', 83.00, 1.70, 19.00, 1.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Espinacas Congeladas', 'Mercadona', 'Verduras', 28.00, 3.00, 1.00, 0.50, CURRENT_TIMESTAMP),
-- VEGANOS / PROTEÍNA VEGETAL
(gen_random_uuid(), 'Proteína de Guisante', 'MyProtein', 'Suplementos', 360.00, 80.00, 2.00, 1.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Soja Texturizada Fina', 'Genérico', 'Legumbres', 350.00, 50.00, 15.00, 1.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Heura (Bocados originales)', 'Heura', 'Vegetal', 126.00, 18.00, 0.70, 3.00, CURRENT_TIMESTAMP),
-- KETO / GRASAS Y PROTEÍNAS
(gen_random_uuid(), 'Mantequilla Ghee', 'Genérico', 'Grasas', 880.00, 0.00, 0.00, 99.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Queso Parmesano', 'Genérico', 'Lácteos', 431.00, 38.00, 4.10, 28.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Panceta de Cerdo', 'Genérico', 'Carnes', 541.00, 9.00, 0.00, 53.00, CURRENT_TIMESTAMP),
-- FRUTAS Y SUPERFOODS
(gen_random_uuid(), 'Semillas de Cáñamo', 'Genérico', 'Superalimentos', 553.00, 31.00, 8.00, 48.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Espirulina en polvo', 'Genérico', 'Superalimentos', 290.00, 57.00, 24.00, 7.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Frambuesas', 'Genérico', 'Frutas', 52.00, 1.20, 12.00, 0.60, CURRENT_TIMESTAMP),
-- VARIOS / DESPENSA
(gen_random_uuid(), 'Caldo de Huesos', 'Genérico', 'Sopas', 20.00, 5.00, 0.00, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pasta Konjac (Shirataki)', 'Genérico', 'Pasta', 9.00, 0.20, 0.00, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Salsa de Soja (Baja en sal)', 'Kikkoman', 'Salsas', 60.00, 9.00, 8.00, 0.00, CURRENT_TIMESTAMP),
-- BEBIDAS (Crítico para el conteo de calorías)
(gen_random_uuid(), 'Café solo sin azúcar', 'Genérico', 'Bebidas', 2.00, 0.10, 0.00, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Té Verde', 'Genérico', 'Bebidas', 1.00, 0.00, 0.20, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Cerveza Sin Alcohol', 'Genérico', 'Bebidas', 20.00, 0.20, 4.50, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Zumo de Naranja natural', 'Genérico', 'Bebidas', 45.00, 0.70, 10.40, 0.20, CURRENT_TIMESTAMP),
-- SALSAS Y CONDIMENTOS (Donde la gente falla en la dieta)
(gen_random_uuid(), 'Mostaza de Dijon', 'Maille', 'Salsas', 150.00, 7.00, 5.00, 12.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Ketchup Zero', 'Prima', 'Salsas', 45.00, 1.20, 8.00, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mayonesa Light', 'Hellmanns', 'Salsas', 260.00, 0.50, 6.00, 26.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Hummus de Garbanzo', 'Genérico', 'Legumbres', 166.00, 8.00, 9.00, 9.50, CURRENT_TIMESTAMP),
-- PRODUCTOS MODERNOS / VEGANOS
(gen_random_uuid(), 'Heura (Bocados Originales)', 'Heura', 'Vegetal', 126.00, 18.00, 0.70, 3.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Tempeh de soja', 'Genérico', 'Vegetal', 190.00, 19.00, 9.00, 11.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Seitán natural', 'Genérico', 'Vegetal', 120.00, 24.00, 3.50, 1.00, CURRENT_TIMESTAMP),
-- SNACKS Y VARIOS
(gen_random_uuid(), 'Gelatina 0% azúcar', 'Royal', 'Dulces', 10.00, 2.00, 0.00, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Tortitas de Maíz con Chocolate', 'Bicentury', 'Snacks', 470.00, 6.50, 60.00, 22.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Barrita de Proteínas (Cookies)', 'Quest', 'Suplementos', 350.00, 35.00, 15.00, 12.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Edamame congelado', 'Genérico', 'Legumbres', 122.00, 11.00, 10.00, 5.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Skyr Natural', 'Lidl', 'Lácteos', 63.00, 11.00, 4.00, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Kéfir de leche', 'Genérico', 'Lácteos', 60.00, 3.50, 4.50, 3.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Semillas de cáñamo', 'Genérico', 'Superalimentos', 553.00, 31.00, 8.00, 48.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Levadura Nutricional', 'Genérico', 'Complementos', 340.00, 50.00, 15.00, 4.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Espirulina en polvo', 'Genérico', 'Suplementos', 290.00, 57.00, 24.00, 7.00, CURRENT_TIMESTAMP),
-- PROTEÍNAS (Nuevas)
(gen_random_uuid(), 'Pavo fiambre 90%', 'El Pozo', 'Carnes', 95.00, 20.00, 1.00, 1.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Bacalao fresco', 'Genérico', 'Pescados', 82.00, 18.00, 0.00, 0.70, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Tofu Firme', 'Genérico', 'Vegetal', 76.00, 8.00, 1.90, 4.80, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Gambas peladas', 'Genérico', 'Pescados', 71.00, 18.00, 0.00, 1.00, CURRENT_TIMESTAMP),
-- CARBOHIDRATOS (Nuevos)
(gen_random_uuid(), 'Cuscús cocido', 'Genérico', 'Cereales', 112.00, 3.80, 23.00, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pan de Centeno', 'Genérico', 'Panadería', 259.00, 8.50, 48.00, 3.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Lentejas cocidas', 'Genérico', 'Legumbres', 116.00, 9.00, 20.00, 0.40, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Yuca cocida', 'Genérico', 'Tubérculos', 160.00, 1.40, 38.00, 0.30, CURRENT_TIMESTAMP),
-- GRASAS Y SNACKS (Nuevos)
(gen_random_uuid(), 'Pistachos naturales', 'Genérico', 'Frutos Secos', 562.00, 20.00, 28.00, 45.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Semillas de Chía', 'Genérico', 'Superalimentos', 486.00, 17.00, 42.00, 31.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Chocolate Negro 85%', 'Lindt', 'Dulces', 584.00, 8.00, 19.00, 46.00, CURRENT_TIMESTAMP),
-- VEGETALES (Nuevos)
(gen_random_uuid(), 'Espárragos trigueros', 'Genérico', 'Verduras', 20.00, 2.20, 3.90, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Kiwi', 'Genérico', 'Frutas', 61.00, 1.10, 15.00, 0.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Calabacín', 'Genérico', 'Verduras', 17.00, 1.20, 3.10, 0.30, CURRENT_TIMESTAMP),
-- CARNES Y EMBUTIDOS
(gen_random_uuid(), 'Carne picada vacuno 5% grasa', 'Genérico', 'Carnes', 124.00, 21.00, 0.00, 5.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Conejo fresco', 'Genérico', 'Carnes', 133.00, 22.00, 0.00, 5.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Jamón Serrano Reserva', 'Navidul', 'Embutidos', 240.00, 30.00, 0.50, 13.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Jamón Cocido Extra 95%', 'Campofrío', 'Embutidos', 101.00, 19.00, 1.00, 2.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pechuga de Pavo Lonchas', 'El Pozo', 'Embutidos', 92.00, 18.00, 1.00, 1.00, CURRENT_TIMESTAMP),
-- PESCADOS Y MARISCOS
(gen_random_uuid(), 'Dorada fresca', 'Genérico', 'Pescados', 115.00, 19.00, 0.00, 4.40, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Langostinos cocidos', 'Genérico', 'Mariscos', 95.00, 20.00, 0.50, 1.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Gulas', 'La Gula del Norte', 'Pescados', 165.00, 8.00, 10.00, 10.00, CURRENT_TIMESTAMP),
-- CEREALES Y DESPENSA
(gen_random_uuid(), 'Arroz Basmati', 'Genérico', 'Cereales', 350.00, 8.00, 78.00, 1.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Harina de avena sabor vainilla', 'MyProtein', 'Cereales', 370.00, 13.00, 60.00, 7.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Copos de maíz (Corn Flakes)', 'Kellogg', 'Cereales', 357.00, 7.00, 84.00, 0.40, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Tortitas de maíz', 'Bicentury', 'Snacks', 385.00, 7.00, 80.00, 3.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pasta de lenteja roja', 'Genérico', 'Pasta', 340.00, 25.00, 50.00, 1.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Lentejas cocidas bote', 'Luengo', 'Legumbres', 85.00, 6.50, 12.00, 0.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Garbanzos cocidos bote', 'Luengo', 'Legumbres', 115.00, 7.00, 15.00, 2.00, CURRENT_TIMESTAMP),
-- VERDURAS Y FRUTAS
(gen_random_uuid(), 'Guisantes congelados', 'Findus', 'Verduras', 75.00, 5.50, 10.00, 0.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Judías verdes frescas', 'Genérico', 'Verduras', 31.00, 1.80, 4.00, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Champiñones laminados', 'Genérico', 'Verduras', 22.00, 3.00, 1.00, 0.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Berenjena', 'Genérico', 'Verduras', 25.00, 1.00, 6.00, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Calabacín fresco', 'Genérico', 'Verduras', 17.00, 1.20, 3.10, 0.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pepino', 'Genérico', 'Verduras', 15.00, 0.70, 2.50, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Tomate Cherry', 'Genérico', 'Verduras', 18.00, 0.90, 3.90, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Piña fresca', 'Genérico', 'Frutas', 50.00, 0.50, 13.00, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Fresas frescas', 'Genérico', 'Frutas', 33.00, 0.70, 7.70, 0.30, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Sandía', 'Genérico', 'Frutas', 30.00, 0.60, 7.50, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Melón Galia', 'Genérico', 'Frutas', 34.00, 0.80, 8.00, 0.20, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mandarina', 'Genérico', 'Frutas', 53.00, 0.80, 13.00, 0.30, CURRENT_TIMESTAMP),
-- LÁCTEOS Y VEGETALES
(gen_random_uuid(), 'Yogur de proteínas fresa', 'Pascual', 'Lácteos', 60.00, 10.00, 4.50, 0.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Queso Grana Padano', 'Genérico', 'Lácteos', 398.00, 33.00, 0.00, 29.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Queso Mozzarella Light', 'Galbani', 'Lácteos', 160.00, 18.00, 1.50, 9.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Bebida de Almendras sin azúcar', 'Alpro', 'Lácteos', 13.00, 0.40, 0.00, 1.10, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Bebida de Soja sin azúcar', 'Alpro', 'Lácteos', 33.00, 3.30, 0.00, 1.80, CURRENT_TIMESTAMP),
-- GRASAS Y SUPLEMENTOS
(gen_random_uuid(), 'Crema de Almendras natural', 'Koro', 'Grasas', 614.00, 21.00, 10.00, 55.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Semillas de calabaza', 'Genérico', 'Frutos Secos', 559.00, 30.00, 11.00, 49.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Avellanas tostadas', 'Genérico', 'Frutos Secos', 628.00, 15.00, 16.00, 60.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Aceite de Coco Virgen', 'Genérico', 'Grasas', 862.00, 0.00, 0.00, 100.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Caseína Micelar Vainilla', 'Optimum Nutrition', 'Suplementos', 360.00, 80.00, 4.00, 1.50, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Pre-entrenamiento (Pre-workout)', 'C4', 'Suplementos', 5.00, 0.00, 1.00, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'BCAAs en polvo', 'Genérico', 'Suplementos', 0.00, 0.00, 0.00, 0.00, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Crema de Arroz', 'Babybel', 'Cereales', 345.00, 7.00, 78.00, 0.50, CURRENT_TIMESTAMP)
    ON CONFLICT (nombre) DO NOTHING;