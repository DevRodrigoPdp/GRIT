# GRIT — Flujo de Navegación del Usuario

> Documento actualizado a partir del router real de Angular (`app.routes.ts`) y `AuthService`.
> Fecha: 2026-03-31 | Branch: `frontend`

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

---

## 3. Página `/pendiente` — Estados del Entrenador

Esta página se muestra cuando el entrenador ha registrado su documentación pero aún no ha sido revisada, o cuando ha sido rechazada.

```
/pendiente
    │
    ├── auth.estado() === 'PENDIENTE_REVISION'
    │       → Indicadores verdes pulsantes
    │         "SOLICITUD EN REVISIÓN"
    │         Plazo máximo 48h + email de notificación
    │
    └── auth.estado() === 'RECHAZADO'
            → Indicadores rojos
              "SOLICITUD RECHAZADA"
              Motivo de rechazo (cuando backend devuelva rejection_reason en /me)
              CTA → soporte@grit.app
```

> La aprobación/rechazo la gestiona el backend exclusivamente (endpoints protegidos por rol `ADMIN`). Ver `BACKEND.md` sección 3.8.

---

## 4. Rutas Definidas

| Ruta | Componente | Descripción |
|---|---|---|
| `/` | `LandingPage` | Página de inicio / marketing |
| `/login` | `LoginPage` | Inicio de sesión (ambos roles) |
| `/pendiente` | `PendientePage` | Espera de revisión o rechazo |
| `/empezar` | `OnboardingPage` | Selector de rol |
| `/empezar/entrenador` | `EntrenadorPage` | Registro de entrenador + subida de documentos |
| `/empezar/atleta` | `AtletaPage` | Registro de atleta |
| `/dashboard/atleta` | `DashboardAtletaPage` | Dashboard atleta (tabs por servicio) |
| `/dashboard/entrenador` | `DashboardEntrenadorPage` | Dashboard entrenador (solo entrenamiento) |
| `/dashboard/entrenador/nutricion` | `DashboardEntrenadorNutricionPage` | Dashboard entrenador (entrenamiento + nutrición) |
| `/dashboard/entrenador/solo-nutricion` | `DashboardEntrenadorSoloNutricionPage` | Dashboard entrenador (solo nutrición) |
| `**` | — | Redirige a `/` |

---

## 5. Gestión de Sesión — AuthService

La sesión se mantiene en **signals de Angular** (única fuente de verdad en el frontend):

| Signal | Tipo | Descripción |
|---|---|---|
| `rol` | `'ATLETA' \| 'ENTRENADOR' \| null` | Rol del usuario autenticado |
| `estado` | `'ACTIVO' \| 'PENDIENTE_REVISION' \| 'RECHAZADO' \| null` | Estado de la cuenta |
| `tituloEntrenamiento` | `boolean \| null` | Tiene titulación de entrenamiento acreditada |
| `tituloNutricion` | `boolean \| null` | Tiene titulación de nutrición acreditada |
| `servicio` | `'ENTRENAMIENTO' \| 'NUTRICION' \| 'AMBOS' \| null` | Solo para atletas |
| `nombre` | `string \| null` | Nombre para mostrar en UI |

La sesión se restaura tras F5 llamando a `GET /api/v1/auth/me` (pendiente de conectar con backend).

---

## 6. Flujo Completo con Sesión Real

```
App arranca
    │
    ▼
AuthService.me() → GET /api/v1/auth/me
    │
    ├── 401 → signals vacíos → usuario ve /  o /login
    │
    └── 200 → setSession(rol, estado, títulos...)
                    │
                    └── redirigir() → dashboard correspondiente
```

---

## 7. Cookies de Sesión

| Cookie | Expiración | Scope |
|---|---|---|
| `access_token` | 15 min | Todas las rutas `/api/v1/**` |
| `refresh_token` | 7 días | Solo `POST /api/v1/auth/refresh` |

Ambas son `HttpOnly; Secure; SameSite=Strict` — inaccesibles desde JavaScript (previene XSS).
