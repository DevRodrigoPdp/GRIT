# Ironmetric — Flujo de Navegación del Usuario

> Documento generado a partir del análisis del router frontend.
> Fecha: 2026-03-21 | Branch: `frontend`

---

## Visión General

La app tiene **dos tipos de usuario** (Coach y Atleta), cada uno con su propio dashboard.
El punto de entrada siempre es la Landing Page (`/`).

El token de sesión se almacena en una **cookie del navegador** (`ironmetric_token`) gestionada por `tokenService.js`.

---

## 1. Flujo Completo

```
┌──────────────────────────────────────────────────────────────────┐
│                         / (Landing)                              │
│                                                                  │
│          [Registrarse]               [Iniciar Sesión]            │
└───────────────┬──────────────────────────────┬───────────────────┘
                │                              │
                ▼                              ▼
      /auth/register                     /auth/login
      (Elegir tipo)                  (Email + Contraseña)
            │                                  │
       ┌────┴────┐                    POST /api/auth/login
       ▼         ▼                             │
  /auth/register/atleta   /auth/register/coach │ { accessToken, user: { role } }
       │                        │              │
  POST /api/auth/register  POST /api/auth/register   setToken(cookie)
  /athlete                  /coach                        │
       │                        │              ┌──────────┴───────────┐
  setToken(cookie)          setToken(cookie)   ▼                      ▼
       │                        │        /athlete/dashboard   /coach/dashboard
       ▼                        ▼
/auth/success-atleta  /auth/success-coach
  (Cuenta creada ✓)    (Cuenta creada ✓)
```

---

## 2. Dashboard — Atleta

**Ruta base:** `/athlete/dashboard`

| Ruta | Página | Descripción |
|------|--------|-------------|
| `/athlete/dashboard` | `AthleteDashboard` | Resumen general del atleta |
| `/athlete/dashboard/training` | `AthleteTrainingPage` | Plan de entrenamiento |
| `/athlete/dashboard/nutrition` | `AthleteNutritionPage` | Plan nutricional |
| `/athlete/dashboard/progress` | `AthleteProgressPage` | Historial y métricas de progreso |
| `/athlete/dashboard/settings` | `AthleteSettingsPage` | Perfil y configuración |

---

## 3. Dashboard — Coach

**Ruta base:** `/coach/dashboard`

| Ruta | Página | Descripción |
|------|--------|-------------|
| `/coach/dashboard` | `CoachDashboard` | Resumen general del coach |
| `/coach/dashboard/athletes` | `AthletesDashboard` | Gestión y listado de atletas |
| `/coach/dashboard/training` | `TrainingDashboard` | Gestión de entrenamientos |
| `/coach/dashboard/exercises` | `TrainingDashboard` | Alias de training (misma vista) |
| `/coach/dashboard/nutrition` | `NutritionDashboard` | Gestión nutricional |
| `/coach/dashboard/settings` | `SettingsDashboard` | Perfil y configuración |

---

## 4. Rutas de Autenticación

| Ruta | Componente | Descripción |
|------|-----------|-------------|
| `/` | `Landing` | Página de inicio / marketing |
| `/auth/register` | `Register` | Selector de tipo de cuenta |
| `/auth/register/atleta` | `RegisterAtleta` | Formulario registro atleta |
| `/auth/register/coach` | `RegisterCoach` | Formulario registro coach |
| `/auth/login` | `Login` | Inicio de sesión |
| `/auth/success-atleta` | `SuccessAtleta` | Confirmación registro atleta |
| `/auth/success-coach` | `SuccessCoach` | Confirmación registro coach |

> Cualquier ruta no definida (`*`) redirige automáticamente a `/`.

---

## 5. Gestión del Token de Sesión

El token se guarda en una cookie mediante `tokenService.js` (`frontend/src/services/tokenService.js`).

| Función | Descripción |
|---------|-------------|
| `setToken(token)` | Guarda el token en cookie con 7 días de expiración y `SameSite=Strict` |
| `getToken()` | Lee el token de las cookies |
| `removeToken()` | Elimina la cookie (logout) |
| `isAuthenticated()` | Devuelve `true` si existe un token válido |

### Comportamiento Post-Login

Tras un login o registro exitoso, el backend debe devolver el **token y el rol** del usuario:

```
POST /api/auth/login
  └─► 200 OK { accessToken: "...", user: { id, nombre, email, role: "coach" | "athlete" } }
        │
        ├─ role === "coach"   → redirect /coach/dashboard
        └─ role === "athlete" → redirect /athlete/dashboard

POST /api/auth/register/coach
POST /api/auth/register/athlete
  └─► 200 OK { accessToken: "...", user: { ... } }
        └─► redirect /auth/success-coach | /auth/success-atleta
```

### Cookie — Especificaciones

| Atributo | Valor | Motivo |
|----------|-------|--------|
| Nombre | `ironmetric_token` | Identificador único de la app |
| Expiración | 7 días | Balance entre UX y seguridad |
| `SameSite` | `Strict` | Protección CSRF |
| `Secure` | Activar en producción | Solo HTTPS |
| `HttpOnly` | Pendiente — solo servidor | Protección XSS total |

> **Nota para el backend:** cuando el servidor gestione la sesión con cookies `HttpOnly`, el frontend dejará de almacenar el token con `setToken()` y pasará a depender exclusivamente de la cookie que el servidor envíe en la respuesta.

---

## 6. Protección de Rutas (pendiente de implementar)

Actualmente **no existen rutas protegidas**. El backend debe exponer `GET /api/auth/me` que el frontend consultará para:

1. Verificar si el token en cookie es válido al cargar la app.
2. Obtener el rol y datos del usuario para renderizar el dashboard correcto.
3. Redirigir a `/auth/login` si el token ha expirado o no existe.

```
GET /api/auth/me
  Authorization: Bearer <token desde cookie>
  │
  ├─► 401 Unauthorized → frontend borra cookie → redirige a /auth/login
  └─► 200 OK { id, nombre, email, role } → renderiza dashboard correspondiente
```

---

## 7. Flujo Completo con Autenticación Real

```
App arranca
    │
    ▼
¿Hay token en cookie (ironmetric_token)?
    │
    ├── NO ──► / (Landing) o /auth/login
    │
    └── SÍ ──► GET /api/auth/me
                    │
                    ├── 401 ──► removeToken() → /auth/login
                    │
                    └── 200 ──► role === "coach"   → /coach/dashboard
                                role === "athlete" → /athlete/dashboard
```
