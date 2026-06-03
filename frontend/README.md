# GRIT — Visión de Producto y Roadmap Frontend

## El problema real que resuelve GRIT

GRIT no es un dashboard de métricas. Es una **plataforma de comunicación
y seguimiento deportivo privada** que elimina los tres problemas principales
que tienen hoy los entrenadores:

1. **45 minutos por mesociclo** usando Canva y Excel
2. **WhatsApp saturado** de dudas técnicas a cualquier hora
3. **Sin contexto visual** — fotos y vídeos perdidos en conversaciones

---

## Contexto de la investigación

Estos módulos responden a hallazgos directos de encuestas a entrenadores:

| Hallazgo | Impacto | Solución GRIT |
|---|---|---|
| Canva + Excel = 45 min/mesociclo | Tiempo perdido cada semana | Generador de rutinas automático |
| Dudas técnicas por WhatsApp | Vida personal saturada | Mensajería interna categorizada |
| RPE, sueño y estrés como métricas clave | Sin sistema de alerta | Registro diario de bienestar |
| Tendencia hacia comunidad con fotos y vídeos | Aislamiento del atleta | Feed de progreso visual privado |

---

## Los 5 módulos core

### 1. Generador de rutinas
> El argumento central del TFG — resuelve el cuello de botella real.

El entrenador configura parámetros (mesociclo, días, objetivo, nivel del atleta)
y el sistema genera la plantilla automáticamente estructurada con ejercicios,
series, repeticiones y progresión calculada.
El atleta la recibe limpia y ejecutable dentro de la plataforma.

**Objetivo:** reducir 45 minutos → menos de 10 minutos por mesociclo.

Los ejercicios se obtienen de una API externa (ver sección de APIs).
El entrenador puede buscar cualquier ejercicio por nombre, músculo o equipo
sin tener que escribirlo a mano.

---

### 2. Nutrición y seguimiento calórico
> Los datos no los introduce el entrenador — los calcula la API.

El atleta registra lo que come buscando el alimento en un campo de búsqueda.
La API devuelve automáticamente: calorías, proteínas, carbohidratos y grasas.
El sistema calcula el TDEE (gasto calórico total) según el perfil del atleta
(peso, altura, edad, nivel de actividad) y compara con la ingesta real.

**Lo que ve el atleta:**
- Resumen diario de macros (progreso hacia el objetivo)
- Comparativa ingesta vs objetivo calórico
- Historial semanal de cumplimiento nutricional

**Lo que ve el entrenador:**
- Cumplimiento nutricional de cada atleta
- Alertas si hay déficit o superávit sostenido
- Ajuste de objetivos calóricos desde su panel

> **Importante:** las fotos en la plataforma son de **ejecución de ejercicios
> y progreso corporal** — no de comidas. La nutrición se registra por texto/búsqueda,
> no por fotografía.

---

### 3. Registro diario de bienestar
> Menos de 1 minuto al día para el atleta. Visibilidad total para el entrenador.

El atleta registra cada día:
- **RPE** — esfuerzo percibido del entrenamiento (escala 1-10)
- **Sueño** — horas y calidad subjetiva
- **Estrés** — nivel subjetivo (1-5)
- **Hidratación** — litros aproximados

El entrenador ve estos datos en su dashboard como **señales de alerta**:
si un atleta lleva 3 días con sueño < 6h y RPE alto, el sistema lo marca visualmente.

---

### 4. Feed de progreso visual
> Sustituye los vídeos y fotos por WhatsApp. Privado, contextualizado.

Fotos y vídeos del atleta subidos directamente a la plataforma:
- **Ejecución de ejercicios** — para corrección técnica por parte del entrenador
- **Fotos de físico** — progreso corporal semana a semana

Solo visibles para el entrenador y el atleta — **no es público, no es una red social.**
El entrenador puede comentar directamente sobre cada foto o vídeo
con contexto ("baja más las caderas en el rack").

---

### 5. Mensajería interna categorizada
> Saca las dudas técnicas de WhatsApp sin perder el contexto.

Los mensajes están **ligados a un ejercicio concreto o a un día del plan**,
no son un chat genérico. El atleta tiene una duda sobre la sentadilla →
abre el ejercicio → manda el mensaje ahí.
El entrenador lo ve con contexto, no a las 11 de la noche en el móvil personal.

---

## APIs externas necesarias

> **Nota:** Las APIs concretas están pendientes de selección final.
> Los criterios y requisitos están definidos — la elección se hará
> antes de comenzar la implementación de cada módulo.

---

### API de Ejercicios *(por determinar)*

Qué debe ofrecer:
- Base de datos amplia de ejercicios con nombre, músculo principal, músculo secundario
  y equipo necesario
- GIF o imagen de ejecución por ejercicio
- Búsqueda por nombre, grupo muscular, equipo o parte del cuerpo
- Respuesta en JSON, plan gratuito suficiente para desarrollo y demo

Cómo se usa en GRIT:
- El entrenador busca un ejercicio → la API devuelve variantes con imagen y músculos
- Se guarda el ejercicio seleccionado en el plan — no se almacena la BD entera
- El atleta ve la imagen de ejecución en su rutina del día

---

### API de Nutrición *(por determinar)*

Qué debe ofrecer:
- Base de datos amplia de alimentos (productos procesados y alimentos básicos)
- Búsqueda por nombre de alimento
- Macros completos: calorías, proteínas, carbohidratos, grasas
- Plan gratuito o sin coste para desarrollo y demo del TFG

Cómo se usa en GRIT:
- El atleta escribe el alimento → la API devuelve los macros
- Los macros se calculan automáticamente según el gramaje introducido
- Se suma al registro nutricional del día

---

### Cálculo de TDEE (sin API — cálculo local)
El gasto calórico diario se calcula en el frontend con la fórmula
**Mifflin-St Jeor** usando los datos del perfil del atleta:

```
Hombre: (10 × peso_kg) + (6.25 × altura_cm) − (5 × edad) + 5
Mujer:  (10 × peso_kg) + (6.25 × altura_cm) − (5 × edad) − 161
```

Multiplicado por el factor de actividad:
| Nivel | Factor |
|---|---|
| Sedentario | × 1.2 |
| Ligero (1-3 días/semana) | × 1.375 |
| Moderado (3-5 días/semana) | × 1.55 |
| Activo (6-7 días/semana) | × 1.725 |
| Muy activo (2x/día) | × 1.9 |

Este cálculo se hace en el frontend al guardar el perfil del atleta
y se actualiza si cambia el peso o el nivel de actividad.

---

## Diseño de los dashboards

### Dashboard Atleta
No es solo gráficas — es la **primera acción del día**.

| Bloque | Contenido | Módulo |
|---|---|---|
| Check-in diario | RPE, sueño, estrés, agua — formulario rápido al entrar | Bienestar |
| Nutrición del día | Barra de progreso de macros, buscador de alimentos | Nutrición |
| Plan del día | Entrenamiento del día con ejercicios, series y GIFs | Generador |
| Feed de progreso | Sus fotos de ejecución y físico por semana | Feed visual |
| Métricas | Gráficas de rendimiento y bienestar a lo largo del tiempo | — |

---

### Dashboard Entrenador
Panel de control real — **visión de todos sus atletas a la vez**.

| Bloque | Contenido | Módulo |
|---|---|---|
| Alertas de bienestar | Quién va mal esta semana (sueño, RPE, estrés) | Bienestar |
| Cumplimiento nutricional | Atletas por debajo/encima de su objetivo calórico | Nutrición |
| Generador de rutinas | Crear y asignar mesociclos con búsqueda de ejercicios | Generador |
| Feed pendiente | Fotos y vídeos de atletas esperando revisión y comentario | Feed visual |
| Acceso rápido | Vista individual de cada atleta sin búsqueda | — |

---

## Priorización para el TFG (entrega 25 mayo 2026)

| Prioridad | Módulo | Justificación |
|---|---|---|
| 1 | Generador de rutinas + API ejercicios | Diferenciador del TFG, soluciona el problema más documentado |
| 2 | Registro diario de bienestar | Rápido de implementar, muy visual, alto impacto en el dashboard |
| 3 | Nutrición + API calorías | Completa el perfil del atleta, cálculo automático sin trabajo manual |
| 4 | Feed de fotos de progreso | Cambia la percepción de app a plataforma real |
| 5 | Mensajería interna | Más complejo — puede quedar como trabajo futuro en la memoria |

---

## Lo que esto no es

- No es una app de nutrición genérica
- No es un tracker de calorías con foto de comida — **las fotos son de ejercicios y cuerpo**
- No es una red social pública
- No es otro Excel con mejor diseño

**Es la capa de comunicación y seguimiento que falta entre el entrenador y el atleta.**

---

## Trabajo futuro (post-TFG)

- Videollamadas integradas para revisión técnica en directo
- IA para detección automática de patrones de sobreentrenamiento
- Sistema de comunidad entre atletas del mismo entrenador
- App móvil nativa para el check-in diario y registro nutricional
- Integración con wearables (Garmin, Polar, Apple Watch)
- Escaneo de código de barras para registro nutricional instantáneo
- Análisis automático de vídeo de ejecución con IA
