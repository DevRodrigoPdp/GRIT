# GRIT — Flujo de Navegación del Usuario

> Documento actualizado a partir del router real de Angular (`app.routes.ts`) y `AuthService`.
> Última actualización: 2026-04-08 | Branch: `feat/nutricion`

---

## Visión General

La app tiene **dos tipos de usuario** (Entrenador y Atleta). El entrenador tiene además **tres dashboards distintos** según sus titulaciones acreditadas. La sesión se gestiona mediante **cookies HttpOnly** enviadas por el backend — el frontend nunca almacena el JWT.

---

## 1. Flujo Completo

```
┌─────────────────────────────────────────────────────────────────┐
│                          / (Landing)                            │
│                                                                 │
│            [EMPEZAR]                   [INICIAR SESIÓN]         │
└──────────────┬──────────────────────────────┬───────────────────┘
               │                              │
               ▼                              ▼
          /empezar                        /login
      (Role Selector)               (Email + Contraseña)
            │                                 │
       ┌────┴────┐                   POST /api/v1/auth/login
       ▼         ▼                            │
/empezar/atleta  /empezar/entrenador     { rol, estado, nombre,
       │                │                 tituloEntrenamiento,
       │                │                 tituloNutricion,
       ▼                ▼                 servicio }
POST /api/v1/auth/   POST /api/v1/auth/        │
registro/atleta      registro/entrenador        │
       │                │                      │
       │           (multipart/form-data         │
       │            + documentos PDF)           │
       │                │                      │
       ▼                ▼                      │
  ACTIVO         PENDIENTE_REVISION            │
       │                │                      │
       ▼                ▼                      │
/dashboard/atleta   /pendiente            (ver sección 2)
```

---

## 2. Lógica de Redirección Post-Login / Post-Registro

La redirección la ejecuta `AuthService.redirigir()` en función de los datos de sesión:

| `rol` | `estado` | `tituloEntrenamiento` | `tituloNutricion` | Destino |
|---|---|---|---|---|
| `ATLETA` | `ACTIVO` | `null` | `null` | `/dashboard/atleta` |
| `ENTRENADOR` | `ACTIVO` | `true` | `true` | `/dashboard/entrenador/nutricion` |
| `ENTRENADOR` | `ACTIVO` | `true` | `false` | `/dashboard/entrenador` |
| `ENTRENADOR` | `ACTIVO` | `false` | `true` | `/dashboard/entrenador/solo-nutricion` |
| `ENTRENADOR` | `PENDIENTE_REVISION` | cualquiera | cualquiera | `/pendiente` |
| cualquiera | `RECHAZADO` | — | — | `/login` (con mensaje de error) |

> **TODO — consolidación de rutas:** Los tres dashboards de entrenador están en proceso de unificarse en `/dashboard/entrenador`. La lógica de tabs (mostrar/ocultar ENTRENAMIENTO o NUTRICIÓN) ya funciona con signals en `DashboardEntrenadorPage`; el objetivo es eliminar las rutas `/nutricion` y `/solo-nutricion` cuando el backend devuelva los títulos en `/me`.

---

## 3. Página `/pendiente` — Estados del Entrenador

```
/pendiente
    │
    ├── auth.estado() === 'PENDIENTE_REVISION'
    │       → "SOLICITUD EN REVISIÓN" — plazo 48h + email
    │
    └── auth.estado() === 'RECHAZADO'
            → "SOLICITUD RECHAZADA" — motivo + CTA soporte
```

> La aprobación/rechazo la gestiona el backend (endpoints protegidos por rol `ADMIN`).

---

## 4. Rutas Definidas

| Ruta | Componente | Estado |
|---|---|---|
| `/` | `LandingPage` | ✅ Implementado |
| `/login` | `LoginPage` | ✅ Implementado |
| `/pendiente` | `PendientePage` | ✅ Implementado |
| `/empezar` | `OnboardingPage` | ✅ Implementado |
| `/empezar/entrenador` | `EntrenadorPage` | ✅ Implementado |
| `/empezar/atleta` | `AtletaPage` | ✅ Implementado |
| `/dashboard/atleta` | `DashboardAtletaPage` | 🔲 Pendiente |
| `/dashboard/entrenador` | `DashboardEntrenadorPage` | ✅ Implementado (ver sección 6) |
| `/dashboard/entrenador/nutricion` | `DashboardEntrenadorNutricionPage` | 🔲 Pendiente unificación |
| `/dashboard/entrenador/solo-nutricion` | `DashboardEntrenadorSoloNutricionPage` | 🔲 Pendiente unificación |

---

## 5. Gestión de Sesión — AuthService

| Signal | Tipo | Descripción |
|---|---|---|
| `rol` | `'ATLETA' \| 'ENTRENADOR' \| null` | Rol del usuario autenticado |
| `estado` | `'ACTIVO' \| 'PENDIENTE_REVISION' \| 'RECHAZADO' \| null` | Estado de la cuenta |
| `tituloEntrenamiento` | `boolean \| null` | Titulación de entrenamiento acreditada |
| `tituloNutricion` | `boolean \| null` | Titulación de nutrición acreditada |
| `servicio` | `'ENTRENAMIENTO' \| 'NUTRICION' \| 'AMBOS' \| null` | Solo para atletas |
| `nombre` | `string \| null` | Nombre para mostrar en UI |

**Restauración de sesión tras F5:**
```
App arranca → AuthService.me() → GET /api/v1/auth/me
    ├── 401 → signals vacíos → usuario ve / o /login
    └── 200 → setSession(...) → redirigir() → dashboard
```
> **TODO backend:** `me()` actualmente usa mock. Al conectar, descomentar la llamada real en `auth.service.ts` y eliminar el bloque mock.

---

## 6. Dashboard Entrenador — Flujo Interno

El dashboard del entrenador (`/dashboard/entrenador`) es una **Single Page** — sin rutas hijas. Toda la navegación interna se gestiona con **signals de Angular**.

```
/dashboard/entrenador
        │
        ▼
┌───────────────────┐
│   LISTA ATLETAS   │  ← athletas cargados de GET /api/v1/entrenador/atletas
│                   │
│  Filtro por nombre│
│  Chips ENTR/NUTR  │  ← solo si tituloEntrenamiento && tituloNutricion
│                   │
│  [ Carlos Ruiz  ] │  ENTR + NUTR  ● ACTIVO
│  [ Laura Sánchez] │  ENTR         ● ACTIVO
│  [ Marcos Ibáñez] │  NUTR         ○ SIN PLAN
└────────┬──────────┘
         │ click atleta
         ▼
┌─────────────────────────────────┐
│   DETALLE ATLETA                │
│   (sidebar info + contenido)    │
│                                 │
│  Tabs visibles =                │
│    intersección entre:          │
│    - títulos del entrenador     │
│    - servicio del atleta        │
│                                 │
│  [ ENTRENAMIENTO ] [ NUTRICIÓN ]│
└────────┬────────────────────────┘
         │
    ┌────┴────┐
    ▼         ▼
ENTR tab   NUTR tab
(pendiente) (ver sección 7)
```

**Lógica de tabs:**
| Entrenador tiene | Atleta contratado | Tabs visibles |
|---|---|---|
| Ambos títulos | AMBOS | ENTRENAMIENTO + NUTRICIÓN |
| Ambos títulos | ENTRENAMIENTO | Solo ENTRENAMIENTO |
| Ambos títulos | NUTRICION | Solo NUTRICIÓN |
| Solo entrenamiento | cualquiera | Solo ENTRENAMIENTO |
| Solo nutrición | cualquiera | Solo NUTRICIÓN |

> **TODO backend:** `EntrenadorService.getMisAtletas()` usa mock. Al conectar, descomentar `GET /api/v1/entrenador/atletas`.

---

## 7. Módulo de Nutrición — Flujo Interno

Accesible desde la tab NUTRICIÓN del detalle de un atleta. Gestiona **planes de nutrición** con comidas, alimentos y recetas.

```
Tab NUTRICIÓN (atletaId activo)
        │
        ▼
┌─────────────────┐
│  LISTA PLANES   │  ← GET /api/v1/nutricion/planes?atletaId=
│                 │
│  [Plan semana 1]│  → macros totales del día
│  [Plan semana 2]│
│                 │
│  + NUEVO PLAN   │
└────────┬────────┘
         │ click nuevo plan
         ▼
┌──────────────────────────────────────────┐
│  CREAR PLAN                              │
│                                          │
│  Nombre del plan + descripción           │
│                                          │
│  ┌── COMIDA 1 ─────────────────────────┐ │
│  │  Últimos usados (por nombre comida) │ │  ← localStorage
│  │  [+ ALIMENTO] [RECETA]              │ │
│  │  Pollo 100g · 165 kcal · P 31g ...  │ │
│  └────────────────────────────────────┘ │
│                                          │
│  [+ AÑADIR COMIDA]                       │
│                                          │
│  TOTALES DEL DÍA: kcal / prot / carbs /g │
│                                          │
│  [GUARDAR PLAN] → POST /api/v1/nutricion/planes
└──────────────────────────────────────────┘
```

### 7.1 Búsqueda de alimentos (USDA + MyMemory)

```
Entrenador escribe "pechuga de pollo"
        │
        ▼
MyMemory API (ES → EN): "chicken breast"    [externo, gratuito]
        │
        ▼
USDA FoodData Central: 15 resultados         [externo, API key requerida]
        │
        ▼
MyMemory API (EN → ES): traduce nombres      [externo, gratuito]
        │
        ▼
Resultados en español con macros por 100g
```

> **TODO producción:** La API key de USDA está en `environment.ts`. En producción debe moverse a una variable de entorno del servidor o hacer las llamadas a USDA desde el backend para no exponer la key en el bundle del cliente.

### 7.2 Recetas propias

El entrenador puede crear **recetas** (combinación de ingredientes) que se guardan localmente y se reutilizan en cualquier plan.

```
[RECETA] en cabecera comida
        │
        ▼
Panel Recetas
    ├── Lista recetas guardadas → seleccionar → ¿cuántos gramos? → añadir
    └── Nueva receta:
            nombre
            + ingredientes (usa buscador USDA)
            peso total ajustable (cocinado ≠ crudo)
            [GUARDAR] → localStorage
```

> **TODO backend:** Las recetas se guardan en `localStorage`. En el futuro deberían persistir en el backend para que el entrenador las tenga disponibles desde cualquier dispositivo: `GET/POST/DELETE /api/v1/nutricion/recetas`.

### 7.3 Persistencia actual vs. futura

| Dato | Ahora | Con backend |
|---|---|---|
| Planes de nutrición | En memoria (se pierden al recargar) | `POST /api/v1/nutricion/planes` |
| Recetas propias | `localStorage` | `POST /api/v1/nutricion/recetas` |
| Últimos alimentos usados | `localStorage` por nombre de comida | Puede quedarse en localStorage |
| Atleta ve sus planes | ❌ No implementado | `GET /api/v1/nutricion/planes?atletaId=` desde dashboard atleta |

### 7.4 Endpoints de nutrición necesarios en el backend

```
# Planes
POST   /api/v1/nutricion/planes          crear plan (payload: atletaId, nombre, descripcion, comidas[])
GET    /api/v1/nutricion/planes          listar planes del atleta autenticado
GET    /api/v1/nutricion/planes?atletaId entrenador ve planes de un atleta concreto
DELETE /api/v1/nutricion/planes/:id      eliminar plan

# Recetas (futuro)
GET    /api/v1/nutricion/recetas         recetas del entrenador autenticado
POST   /api/v1/nutricion/recetas         crear receta
DELETE /api/v1/nutricion/recetas/:id     eliminar receta
```

> **Importante:** Los alimentos deben guardarse como **snapshot** en el momento de crear el plan (nombre + macros copiados), no como referencia a USDA. Los datos de USDA pueden cambiar o desaparecer.

---

## 8. Cookies de Sesión

| Cookie | Expiración | Scope |
|---|---|---|
| `access_token` | 15 min | Todas las rutas `/api/v1/**` |
| `refresh_token` | 7 días | Solo `POST /api/v1/auth/refresh` |

Ambas son `HttpOnly; Secure; SameSite=Strict` — inaccesibles desde JavaScript (previene XSS).
