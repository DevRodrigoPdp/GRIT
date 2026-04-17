# GRIT — Especificación Backend

> **Rama de trabajo:** `frontend` (en desarrollo) · **Stack frontend:** Angular 21 + Tailwind CSS
> **Este documento** describe todos los contratos de API, modelos de datos y requisitos que el equipo de backend debe implementar para dar soporte a la plataforma GRIT.
> **Última actualización:** 17 de abril de 2026 — sección 3.14 completa: dashboard del atleta (check-in peso, hilo ejercicio, hilo comida, chat, profesionales asignados, ajustes)

---

## 0. Arquitectura Frontend

### 0.1 Estructura de carpetas

```
frontend/src/app/
├── core/                          ← Global: guards, interceptors, auth
│   ├── guards/
│   │   └── auth.guard.ts          — Protección de rutas por rol
│   ├── interceptors/
│   │   └── auth.interceptor.ts    — Refresco automático de access_token
│   └── services/
│       └── auth.service.ts        — Sesión, login, registro, me(), logout()
│
├── shared/                        ← Componentes reutilizables globales
│   ├── splash/
│   ├── header/
│   └── footer/
│
└── features/                      ← Módulos por dominio
    ├── landing/                   — Página de inicio pública
    │   └── components/
    │       ├── hero/
    │       ├── dashboard/
    │       └── verification/
    │
    ├── auth/                      — Autenticación y registro
    │   ├── pages/
    │   │   ├── login/
    │   │   ├── onboarding/
    │   │   ├── registro-atleta/
    │   │   ├── registro-entrenador/
    │   │   └── pendiente/
    │   └── components/
    │       └── role-selector/
    │
    ├── dashboard-entrenador/      — Dashboard del entrenador (ruta única)
    │   ├── dashboard-entrenador.ts/.html
    │   ├── services/
    │   │   ├── entrenador.service.ts
    │   │   ├── entrenamiento.service.ts
    │   │   ├── nutricion.service.ts
    │   │   ├── recetas.service.ts
    │   │   ├── ejercicio.service.ts
    │   │   └── open-food-facts.service.ts
    │   └── components/
    │       ├── perfil-entrenador/
    │       ├── gestion-entrenamiento/
    │       ├── gestion-nutricion/
    │       ├── buscador-alimento/
    │       └── buscador-ejercicio/
    │
    └── dashboard-atleta/          — Dashboard del atleta
        ├── dashboard-atleta.ts/.html
        └── services/
            └── atleta.service.ts
```

### 0.2 Rutas frontend

| Ruta | Componente | Guard | Descripción |
|---|---|---|---|
| `/` | `LandingPage` | — | Página pública de inicio |
| `/login` | `LoginPage` | — | Inicio de sesión |
| `/registro` | `OnboardingPage` | — | Selector de rol |
| `/registro/atleta` | `AtletaPage` | — | Formulario registro atleta |
| `/registro/entrenador` | `EntrenadorPage` | — | Formulario registro entrenador |
| `/pendiente` | `PendientePage` | `ENTRENADOR` | Cuenta pendiente de revisión |
| `/dashboard/atleta` | `DashboardAtletaPage` | `ATLETA` | Dashboard del atleta |
| `/dashboard/entrenador` | `DashboardEntrenadorPage` | `ENTRENADOR` | Dashboard único del entrenador |
| `/admin` | `AdminPage` | `ADMIN` | Panel de administración |

> **Cambio respecto a versión anterior:** Las rutas `/dashboard/entrenador/nutricion` y `/dashboard/entrenador/solo-nutricion` han sido eliminadas. Ahora existe una única ruta `/dashboard/entrenador` que adapta su contenido en función de los signals `tituloEntrenamiento` y `tituloNutricion` recibidos del backend.

### 0.3 Autenticación

- Las cookies `access_token` (15 min) y `refresh_token` (7 días, `Path=/api/v1/auth/refresh`) son **HttpOnly** — el frontend nunca las lee directamente.
- El interceptor `auth.interceptor.ts` detecta respuestas `401` y llama automáticamente a `POST /api/v1/auth/refresh` antes de reintentar la petición original.
- Las rutas protegidas usan `rolGuard(rol)` que, si los signals están vacíos (recarga de página), llama a `GET /api/v1/auth/me` para restaurar la sesión desde la cookie.

---

## 1. Contexto del Producto

GRIT es una plataforma de rendimiento deportivo de alto nivel con dos tipos de usuario y **cuatro dashboards diferenciados**:

| Rol | Titulaciones | Dashboard | Descripción |
|---|---|---|---|
| **Entrenador** | Entrenamiento + Nutrición | `/dashboard/entrenador` | Acceso completo: entrenamiento + planes nutricionales |
| **Entrenador** | Solo Entrenamiento | `/dashboard/entrenador` | Solo entrenamiento. Sin acceso a módulos de nutrición |
| **Entrenador** | Solo Nutrición | `/dashboard/entrenador` | Solo nutrición. Sin acceso a módulos de entrenamiento |
| **Atleta** | — | `/dashboard/atleta` | Métricas, planes y seguimiento personal |

Todos los entrenadores van a la misma ruta `/dashboard/entrenador`. El contenido que se muestra dentro del dashboard se adapta en función de `tituloEntrenamiento` y `tituloNutricion` que devuelve el backend:

| `tituloEntrenamiento` | `tituloNutricion` | Módulos visibles |
|---|---|---|
| `true` | `true` | Entrenamiento + Nutrición |
| `true` | `false` | Solo Entrenamiento |
| `false` | `true` | Solo Nutrición |

> **Principio clave — No intrusión laboral:** Un entrenador solo puede acceder a los módulos para los que tiene titulación acreditada. Esta restricción se aplica tanto en frontend (rutas protegidas) como en backend (validación en cada endpoint de entrenamiento y nutrición).

---

## 2. Stack

- **Lenguaje:** Java con Spring Boot
- **Frontend:** TypeScript con Angular
- **Base de datos:** PostgreSQL (ver carpeta `/bbdd`)
- **Almacenamiento de archivos:** S3-compatible (AWS S3, MinIO, Cloudflare R2)
- **Autenticación:** JWT (access token + refresh token) via cookies HttpOnly
- **Email:** SendGrid / Resend / SMTP

---

## 3. Endpoints Requeridos

### 3.1 Registro de Entrenador

**`POST /api/v1/auth/registro/entrenador`**

Recibe el formulario de registro del entrenador **como `multipart/form-data`** porque incluye archivos adjuntos.

**Campos del body:**

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `nombre` | `string` | ✅ | Min 3 caracteres, solo letras y espacios |
| `email` | `string` | ✅ | Formato email válido, único en BBDD |
| `password` | `string` | ✅ | Min 8 caracteres |
| `codigoProfesional` | `string` | ⚠️ Condicional | Alfanumérico, 4–20 caracteres. **Obligatorio** si tiene titulación universitaria. **Opcional** si solo tiene FP o certificado. Ver nota abajo |
| `titulacionEntrenamiento` | `enum` | ⚠️ Condicional | `null` si no tiene titulación en entrenamiento. Ver valores válidos abajo |
| `titulacionNutricion` | `enum` | ⚠️ Condicional | `null` si no tiene titulación en nutrición. Ver valores válidos abajo |
| `documentos` | `File[]` | ✅ | 1–10 archivos, formatos: PDF/JPG/JPEG/PNG, max 10 MB cada uno |

**Validación cruzada obligatoria en servidor:**
```
Si titulacionEntrenamiento == null Y titulacionNutricion == null → 400 (debe tener al menos una)
Si titulacionEntrenamiento == GRADO_CAFYD o titulacionNutricion == GRADO_NUTRICION_DIETETICA → codigoProfesional obligatorio (400 si falta)
```

> **Nota sobre `codigoProfesional` — quién tiene cada tipo de número:**
>
> En España solo las titulaciones universitarias dan acceso a un Colegio Profesional y, por tanto, a un número de colegiado oficial:
> - **`GRADO_CAFYD`** → Número de colegiado en el **COLEF** (Colegio Oficial de Licenciados en Educación Física). Obligatorio para ejercer legalmente como entrenador personal en la mayoría de CCAA.
> - **`GRADO_NUTRICION_DIETETICA`** → Número de colegiado en el **Colegio de Dietistas-Nutricionistas** de la comunidad (ej. CODINMA en Madrid). La colegiación puede ser obligatoria o voluntaria según la CCAA.
>
> Los técnicos de FP y los certificados de profesionalidad **no pertenecen a ningún Colegio Profesional** y por tanto no tienen número de colegiado:
> - **`TSAF_TSEAS` / `CERT_AFDA0210`** → Pueden tener un **número de registro** en el Registro Oficial de Profesionales del Deporte de su comunidad (disponible en CCAA como Cataluña o Extremadura), pero no es universal ni obligatorio.
> - **`TSD`** → Pueden pertenecer a asociaciones como **ADDEPA**, que otorgan un número de asociado, pero sin el mismo peso legal que un número de colegiado.
>
> El frontend adapta dinámicamente el label del campo (`Número de colegiado` vs `Número de registro`) y su carácter obligatorio según las titulaciones seleccionadas. El backend debe aplicar la misma lógica de validación.

**Valores válidos para `titulacionEntrenamiento`** (títulos oficiales en España):
```
GRADO_CAFYD    → Grado en Ciencias de la Actividad Física y del Deporte
TSAF_TSEAS     → Técnico Superior en Animación de Actividades Físicas / TSEAS
CERT_AFDA0210  → Certificado de Profesionalidad AFDA0210
```

**Valores válidos para `titulacionNutricion`** (títulos oficiales en España):
```
GRADO_NUTRICION_DIETETICA  → Grado en Nutrición Humana y Dietética
TSD                        → Técnico Superior en Dietética
```

> Los campos `titulo_entrenamiento` y `titulo_nutricion` **no se envían desde el frontend** — el backend los deriva automáticamente: si `titulacionEntrenamiento` tiene valor → `titulo_entrenamiento = true`, si `titulacionNutricion` tiene valor → `titulo_nutricion = true`.

**Respuesta 201 (éxito):**
```json
{
  "ok": true,
  "message": "Solicitud recibida. Revisaremos tus credenciales en un plazo máximo de 48h y te notificaremos por correo.",
  "data": {
    "id": "uuid-del-entrenador",
    "nombre": "Carlos Martínez",
    "rol": "ENTRENADOR",
    "estado": "PENDIENTE_REVISION",
    "tituloEntrenamiento": true,
    "tituloNutricion": false
  }
}
```

> El frontend utiliza `tituloEntrenamiento` y `tituloNutricion` para saber a qué dashboard redirigir cuando el admin apruebe la cuenta.

**Respuesta 409 (correo o código duplicado):**
```json
{
  "ok": false,
  "error": "EMAIL_DUPLICADO" | "CODIGO_COLEGIADO_DUPLICADO",
  "message": "Ya existe una cuenta con ese correo / código de colegiado."
}
```

**Notas importantes:**
- Los documentos se almacenan en S3, **nunca en el servidor**.
- El estado inicial es siempre `PENDIENTE_REVISION`. No se activa automáticamente.
- Se debe enviar un **email de confirmación** al entrenador indicando que su solicitud está en revisión.
- Guardar la URL de S3 de cada documento en BBDD vinculada al entrenador.

---

### 3.2 Registro de Atleta

**`POST /api/v1/auth/registro/atleta`**

El atleta rellena un formulario en el frontend con sus datos personales, físicos, el servicio que contrata y (si aplica) su objetivo deportivo. El servidor recibe todo como JSON, valida los campos, crea el usuario en base de datos y devuelve las cookies de sesión directamente — el atleta queda activo de inmediato sin revisión manual. Los tokens viajan únicamente en cookies `HttpOnly` y nunca en el body de la respuesta, lo que impide que el JavaScript del frontend pueda leerlos y previene ataques XSS. El campo `objetivo` es condicional: solo es obligatorio cuando el atleta ha contratado entrenamiento; si elige únicamente nutrición, se envía como `null` y el servidor debe aceptarlo sin error.

Recibe el formulario como `application/json`. No hay archivos.

**Campos del body:**

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `nombre` | `string` | ✅ | Min 3 caracteres |
| `email` | `string` | ✅ | Formato email válido, único en BBDD |
| `password` | `string` | ✅ | Min 8 caracteres |
| `fechaNac` | `string` (ISO 8601) | ✅ | Formato `YYYY-MM-DD`, mayor de 14 años |
| `genero` | `enum` | ✅ | `hombre` \| `mujer` \| `otro` |
| `pesoKg` | `number` | ✅ | Entre 30 y 300 (kg) |
| `alturaCm` | `number` | ✅ | Entre 100 y 250 (cm) |
| `deporte` | `string` | ✅ | Min 3 caracteres |
| `nivel` | `enum` | ✅ | Ver valores válidos abajo |
| `servicio` | `enum` | ✅ | `ENTRENAMIENTO` \| `NUTRICION` \| `AMBOS` |
| `objetivo` | `enum` | ⚠️ Condicional | **Requerido** si `servicio` es `ENTRENAMIENTO` o `AMBOS`. **Null** si `servicio` es `NUTRICION`. |

**Valores válidos para `nivel`:**
```
PRINCIPIANTE | INTERMEDIO | AVANZADO | ELITE
```

**Valores válidos para `objetivo`:**
```
RENDIMIENTO | MASA_MUSCULAR | PERDER_PESO | SALUD | RESISTENCIA
```

**Validación cruzada obligatoria en servidor:**
```
si servicio == NUTRICION  → objetivo debe ser null o ausente
si servicio == ENTRENAMIENTO o AMBOS → objetivo es obligatorio (400 si falta)
```

**Respuesta 201 (éxito):**
```json
{
  "ok": true,
  "message": "Perfil creado correctamente.",
  "data": {
    "id": "uuid-del-atleta",
    "estado": "ACTIVO",
    "rol": "ATLETA"
  }
}
```

**Respuesta 400 (validación cruzada fallida):**
```json
{
  "ok": false,
  "error": "OBJETIVO_REQUERIDO",
  "message": "El campo objetivo es obligatorio cuando el servicio incluye entrenamiento."
}
```

**Cookies que debe setear el servidor:**
```
Set-Cookie: access_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900
Set-Cookie: refresh_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=604800
```

**Notas importantes:**
- El atleta se activa **inmediatamente** tras el registro.
- Los tokens **nunca se devuelven en el body**, solo se setean como cookies `HttpOnly` para prevenir XSS.
- El frontend no necesita almacenar nada — el navegador gestiona las cookies automáticamente.
- Enviar **email de bienvenida** al atleta.

---

### 3.3 Login (ambos roles)

**`POST /api/v1/auth/login`**

> ⚠️ El frontend envía el campo como `email`, no `correo`.

```json
// Request body
{
  "email": "usuario@ejemplo.com",
  "password": "contraseña"
}

// Response 200 — los tokens van en cookies, no en el body
{
  "ok": true,
  "data": {
    "rol": "ENTRENADOR" | "ATLETA",
    "estado": "ACTIVO" | "PENDIENTE_REVISION" | "RECHAZADO",
    "nombre": "Carlos Martínez",
    "tituloEntrenamiento": true | false | null,
    "tituloNutricion": true | false | null,
    "servicio": "ENTRENAMIENTO" | "NUTRICION" | "AMBOS" | null
  }
}
```

> `tituloEntrenamiento` y `tituloNutricion` solo son relevantes cuando `rol === "ENTRENADOR"`. Para atletas devolver `null` en ambos. `servicio` solo es relevante para atletas; para entrenadores devolver `null`.

**Cookies que debe setear el servidor:**
```
Set-Cookie: access_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900
Set-Cookie: refresh_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=604800
```

**Lógica de redirección que aplica el frontend según la respuesta:**

| `rol` | `estado` | `tituloEntrenamiento` | `tituloNutricion` | Redirección |
|---|---|---|---|---|
| `ATLETA` | `ACTIVO` | `null` | `null` | `/dashboard/atleta` |
| `ENTRENADOR` | `ACTIVO` | `true` | `true` | `/dashboard/entrenador/nutricion` |
| `ENTRENADOR` | `ACTIVO` | `true` | `false` | `/dashboard/entrenador` |
| `ENTRENADOR` | `ACTIVO` | `false` | `true` | `/dashboard/entrenador/solo-nutricion` |
| `ENTRENADOR` | `PENDIENTE_REVISION` | cualquiera | cualquiera | `/pendiente` |
| cualquiera | `RECHAZADO` | cualquiera | cualquiera | `/login` con mensaje de error |

---

### 3.4 Refresh Token

**`POST /api/v1/auth/refresh`**

No necesita body. El `refresh_token` viaja automáticamente en la cookie.

```json
// Response 200 — renueva la cookie access_token automáticamente
{ "ok": true }
```

**El servidor debe:**
1. Leer la cookie `refresh_token`
2. Validarla y setear una nueva cookie `access_token`
3. Responder `200 ok`

---

### 3.5 Logout

**`POST /api/v1/auth/logout`**

No necesita body. Elimina las cookies del navegador.

```json
// Response 200
{ "ok": true }
```

El servidor debe sobreescribir ambas cookies con `Max-Age=0`:
```
Set-Cookie: access_token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0
Set-Cookie: refresh_token=; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=0
```

---

### 3.6 Me (restaurar sesión)

**`GET /api/v1/auth/me`**

Llamado al cargar cualquier dashboard para restaurar la sesión tras un F5 o cierre de pestaña. Lee la cookie `access_token` automáticamente.

```json
// Response 200
{
  "ok": true,
  "data": {
    "id": "uuid",
    "nombre": "Carlos Martínez",
    "rol": "ENTRENADOR",
    "estado": "ACTIVO",
    "servicio": null,
    "tituloEntrenamiento": true,
    "tituloNutricion": false,
    "titulacionEntrenamiento": "GRADO_CAFYD",
    "titulacionNutricion": null
  }
}
```

> Para atletas: `tituloEntrenamiento` y `tituloNutricion` son `null`; `servicio` tiene valor (`ENTRENAMIENTO` | `NUTRICION` | `AMBOS`).
> Para entrenadores: `servicio` es `null`; `tituloEntrenamiento` y `tituloNutricion` indican los módulos activos.

**Response 401** si la cookie no existe o ha caducado:
```json
{ "ok": false, "error": "NO_AUTENTICADO" }
```

---

### 3.7 Solicitud de Ampliación de Permisos *(implementación futura)*

Un entrenador podrá solicitar acceso a los módulos para los que no tenía titulación en el momento del registro, aportando nueva documentación.

#### Ampliar acceso a Nutrición

**`POST /api/v1/entrenador/solicitar-nutricion`**

Requiere cookie `access_token` válida con `rol === 'ENTRENADOR'` y `titulo_nutricion === false`.

Recibe `multipart/form-data`:

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `documentos` | `File[]` | ✅ | 1–10 archivos PDF/JPG/PNG, max 10 MB c/u |

**Respuesta 200:**
```json
{
  "ok": true,
  "message": "Solicitud de ampliación recibida. Revisaremos tu documentación en un plazo máximo de 48h.",
  "data": { "estadoSolicitud": "PENDIENTE_REVISION_NUTRICION" }
}
```

#### Ampliar acceso a Entrenamiento

**`POST /api/v1/entrenador/solicitar-entrenamiento`**

Requiere cookie `access_token` válida con `rol === 'ENTRENADOR'` y `titulo_entrenamiento === false`.

Recibe `multipart/form-data`:

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `documentos` | `File[]` | ✅ | 1–10 archivos PDF/JPG/PNG, max 10 MB c/u |

**Respuesta 200:**
```json
{
  "ok": true,
  "message": "Solicitud de ampliación recibida. Revisaremos tu documentación en un plazo máximo de 48h.",
  "data": { "estadoSolicitud": "PENDIENTE_REVISION_ENTRENAMIENTO" }
}
```

---

### 3.8 Protección de endpoints por titulación

Todos los endpoints bajo `/api/v1/nutricion/**` y `/api/v1/entrenamiento/**` requieren que el usuario tenga la titulación correspondiente.

**Middleware para rutas de nutrición:**
1. Verificar cookie `access_token` válida
2. Verificar que `rol === 'ENTRENADOR'`
3. Verificar que `titulo_nutricion === true` en BBDD

**Respuesta 403:**
```json
{
  "ok": false,
  "error": "ACCESO_DENEGADO_SIN_TITULACION_NUTRICION",
  "message": "No tienes autorización para acceder a los módulos de nutrición. Se requiere titulación acreditada."
}
```

**Middleware para rutas de entrenamiento:**
1. Verificar cookie `access_token` válida
2. Verificar que `rol === 'ENTRENADOR'`
3. Verificar que `titulo_entrenamiento === true` en BBDD

**Respuesta 403:**
```json
{
  "ok": false,
  "error": "ACCESO_DENEGADO_SIN_TITULACION_ENTRENAMIENTO",
  "message": "No tienes autorización para acceder a los módulos de entrenamiento. Se requiere titulación acreditada."
}
```

> Esta validación **siempre ocurre en servidor**, independientemente de lo que muestre el frontend.

---

### 3.9 Endpoints de Administración

> **Seguridad:** Todos estos endpoints requieren cookie `access_token` válida con `rol === 'ADMIN'`.

**`GET /api/v1/admin/entrenadores?status=pending`**

Devuelve la lista de entrenadores cuya documentación está pendiente de revisión.

```json
{
  "ok": true,
  "data": [
    {
      "id": "uuid",
      "nombre": "Carlos Martínez",
      "correo": "carlos@example.com",
      "titulacionEntrenamiento": "GRADO_CAFYD",
      "titulacionNutricion": null,
      "codigoProfesional": "12345",
      "uploaded_at": "2026-03-29T10:30:00Z",
      "documentos": [
        {
          "id": "uuid-doc",
          "nombre_archivo": "Grado_CAFYD.pdf",
          "url_firmada": "https://s3.../...",
          "uploaded_at": "2026-03-29T10:30:00Z"
        }
      ]
    }
  ]
}
```

> `url_firmada` es una pre-signed URL de S3 con expiración corta (ej. 15 min).

**`POST /api/v1/admin/entrenadores/:id/aprobar`**

1. Cambiar `usuarios.estado` → `ACTIVO`
2. Cambiar `documentos_entrenador.status` → `verified` y setear `reviewed_at`
3. Enviar email de aprobación

```json
{ "ok": true, "message": "Entrenador aprobado correctamente." }
```

**`POST /api/v1/admin/entrenadores/:id/rechazar`**

Body: `{ "motivo": "El PDF es ilegible..." }`

1. Cambiar `usuarios.estado` → `RECHAZADO`
2. Cambiar `documentos_entrenador.status` → `rejected`, guardar motivo
3. Enviar email de rechazo con motivo

```json
{ "ok": true, "message": "Solicitud rechazada. El entrenador ha sido notificado." }
```

---

### 3.10 Healthcheck

**`GET /api/v1/health`**

```json
{ "status": "ok", "timestamp": "2026-03-21T12:00:00Z" }
```

---

### 3.11 Módulo Entrenador

> Requieren cookie `access_token` válida con `rol === 'ENTRENADOR'`.

**`GET /api/v1/entrenador/perfil`**

Devuelve el perfil del entrenador autenticado.

```json
{
  "ok": true,
  "data": {
    "id": "uuid",
    "nombre": "Carlos Martínez",
    "correo": "carlos@ejemplo.com",
    "titulacionEntrenamiento": "GRADO_CAFYD",
    "titulacionNutricion": null,
    "experienciaAnos": 5,
    "descripcion": "Especialista en rendimiento deportivo.",
    "estado": "ACTIVO"
  }
}
```

---

**`GET /api/v1/entrenador/atletas`**

Devuelve la lista de atletas asignados al entrenador autenticado.

```json
{
  "ok": true,
  "data": [
    {
      "id": "uuid-atleta",
      "nombre": "Carlos Ruiz",
      "deporte": "Fútbol",
      "nivel": "AVANZADO",
      "servicio": "AMBOS",
      "tienePlanActivo": true
    }
  ]
}
```

> `tienePlanActivo` es `true` si el atleta tiene al menos un plan de entrenamiento O nutrición activo creado por este entrenador.

---

### 3.12 Módulo Nutrición (Entrenador)

> Requieren cookie `access_token` con `rol === 'ENTRENADOR'` y `titulo_nutricion === true`.

**`GET /api/v1/nutricion/planes?atletaId=<uuid>`**

Devuelve todos los planes de nutrición creados para un atleta.

```json
{
  "ok": true,
  "data": [
    {
      "id": "uuid-plan",
      "atletaId": "uuid-atleta",
      "nombre": "Plan definición verano",
      "descripcion": "Déficit calórico moderado",
      "comidas": [
        {
          "nombre": "Desayuno",
          "alimentos": [
            {
              "alimento": {
                "codigo": "3017620425400",
                "nombre": "Avena",
                "marca": "Quaker",
                "kcalPor100g": 366,
                "proteinasPor100g": 13.2,
                "carbsPor100g": 58.7,
                "grasasPor100g": 6.9
              },
              "cantidadG": 80
            }
          ]
        }
      ],
      "creadoEn": "2026-04-01T10:00:00Z"
    }
  ]
}
```

---

**`POST /api/v1/nutricion/planes`**

Crea un nuevo plan de nutrición para un atleta.

```json
// Request body
{
  "atletaId": "uuid-atleta",
  "nombre": "Plan definición verano",
  "descripcion": "Déficit calórico moderado",
  "comidas": [
    {
      "nombre": "Desayuno",
      "alimentos": [
        {
          "alimento": {
            "codigo": "3017620425400",
            "nombre": "Avena",
            "marca": "Quaker",
            "kcalPor100g": 366,
            "proteinasPor100g": 13.2,
            "carbsPor100g": 58.7,
            "grasasPor100g": 6.9
          },
          "cantidadG": 80
        }
      ]
    }
  ]
}

// Response 201
{
  "ok": true,
  "data": { "id": "uuid-nuevo-plan", "creadoEn": "2026-04-10T12:00:00Z" }
}
```

---

**`DELETE /api/v1/nutricion/planes/:id`**

Elimina un plan de nutrición. Solo puede borrarlo el entrenador que lo creó.

```json
// Response 200
{ "ok": true }
```

**Response 403** si intenta borrar un plan de otro entrenador:
```json
{ "ok": false, "error": "ACCESO_DENEGADO" }
```

---

### 3.13 Módulo Entrenamiento (Entrenador)

> Requieren cookie `access_token` con `rol === 'ENTRENADOR'` y `titulo_entrenamiento === true`.

**`GET /api/v1/entrenamiento/rutinas?atletaId=<uuid>`**

Devuelve todas las rutinas creadas para un atleta.

```json
{
  "ok": true,
  "data": [
    {
      "id": "uuid-rutina",
      "atletaId": "uuid-atleta",
      "nombre": "Fuerza Semana A",
      "descripcion": "Rutina de fuerza máxima",
      "sesiones": [
        {
          "id": "uuid-sesion",
          "nombre": "Piernas",
          "ejercicios": [
            {
              "ejercicio": {
                "id": "barbell-squat",
                "nombre": "Sentadilla con barra",
                "categoria": "Fuerza",
                "nivel": "Intermedio",
                "equipamiento": "Barra",
                "musculoPrincipal": "Cuádriceps",
                "musculosSecundarios": ["Glúteos", "Isquiotibiales"],
                "instrucciones": [],
                "imagenes": []
              },
              "series": 4,
              "reps": "6",
              "notas": "Con pausa abajo"
            }
          ]
        }
      ],
      "creadoEn": "2026-04-10T10:00:00Z"
    }
  ]
}
```

> Los datos del ejercicio (`EjercicioAPI`) se guardan embebidos en la sesión tal como los devuelve el buscador del frontend. No hay una tabla separada de ejercicios en la BBDD de GRIT — el dataset proviene de la fuente externa `yuhonas/free-exercise-db`.

---

**`POST /api/v1/entrenamiento/rutinas`**

Crea una nueva rutina para un atleta.

```json
// Request body
{
  "atletaId": "uuid-atleta",
  "nombre": "Fuerza Semana A",
  "descripcion": "Rutina de fuerza máxima",
  "sesiones": [
    {
      "id": "uuid-sesion-local",
      "nombre": "Piernas",
      "ejercicios": [
        {
          "ejercicio": { /* objeto EjercicioAPI completo */ },
          "series": 4,
          "reps": "6",
          "notas": "Con pausa abajo"
        }
      ]
    }
  ]
}

// Response 201
{
  "ok": true,
  "data": { "id": "uuid-nueva-rutina", "creadoEn": "2026-04-10T12:00:00Z" }
}
```

---

**`DELETE /api/v1/entrenamiento/rutinas/:id`**

Elimina una rutina. Solo puede borrarla el entrenador que la creó.

```json
// Response 200
{ "ok": true }
```

---

### 3.15 Recetas del Entrenador

> Requieren cookie `access_token` con `rol === 'ENTRENADOR'` y `titulo_nutricion === true`.
>
> Actualmente las recetas se guardan en `localStorage`. **Deben persistirse en backend** para que el entrenador las tenga disponibles desde cualquier dispositivo y sesión.

**`GET /api/v1/nutricion/recetas`**

Devuelve todas las recetas propias del entrenador autenticado.

```json
{
  "ok": true,
  "data": [
    {
      "id": "uuid-receta",
      "nombre": "Arroz con pollo",
      "gramosTotal": 350,
      "ingredientes": [
        {
          "alimento": {
            "codigo": "3017620425400",
            "nombre": "Arroz blanco",
            "marca": null,
            "kcalPor100g": 360,
            "proteinasPor100g": 7.0,
            "carbsPor100g": 79.0,
            "grasasPor100g": 0.6
          },
          "cantidadG": 200
        }
      ],
      "creadoEn": "2026-04-10T12:00:00Z"
    }
  ]
}
```

---

**`POST /api/v1/nutricion/recetas`**

Crea una nueva receta para el entrenador autenticado.

```json
// Request body
{
  "nombre": "Arroz con pollo",
  "gramosTotal": 350,
  "ingredientes": [
    {
      "alimento": {
        "codigo": "3017620425400",
        "nombre": "Arroz blanco",
        "marca": null,
        "kcalPor100g": 360,
        "proteinasPor100g": 7.0,
        "carbsPor100g": 79.0,
        "grasasPor100g": 0.6
      },
      "cantidadG": 200
    }
  ]
}

// Response 201
{
  "ok": true,
  "data": { "id": "uuid-nueva-receta", "creadoEn": "2026-04-10T12:00:00Z" }
}
```

---

**`DELETE /api/v1/nutricion/recetas/:id`**

Elimina una receta. Solo puede borrarla el entrenador que la creó.

```json
// Response 200
{ "ok": true }
```

**Response 403** si intenta borrar una receta de otro entrenador:
```json
{ "ok": false, "error": "ACCESO_DENEGADO" }
```

---

### 3.16 Alimentos Recientes por Comida

> Requieren cookie `access_token` con `rol === 'ENTRENADOR'` y `titulo_nutricion === true`.
>
> Almacenan los últimos alimentos usados en cada tipo de comida ("Desayuno", "Almuerzo", etc.) por entrenador. Actualmente se guardan en `localStorage`. **Deben persistirse en backend** para que el historial esté disponible entre sesiones y dispositivos.

**`GET /api/v1/nutricion/recientes?comida=<nombre>`**

Devuelve los últimos alimentos usados en una comida concreta por el entrenador autenticado. Máximo 8 resultados, ordenados por `usado_en DESC`.

```json
{
  "ok": true,
  "data": [
    {
      "codigo": "3017620425400",
      "nombre": "Avena",
      "marca": "Quaker",
      "kcalPor100g": 366,
      "proteinasPor100g": 13.2,
      "carbsPor100g": 58.7,
      "grasasPor100g": 6.9
    }
  ]
}
```

---

**`POST /api/v1/nutricion/recientes`**

Registra el uso de un alimento en una comida. Si ya existe la combinación `(usuario_id, nombre_comida, codigo_alimento)`, actualiza `usado_en`. Si hay más de 8 para ese `nombre_comida`, elimina el más antiguo.

```json
// Request body
{
  "nombreComida": "Desayuno",
  "alimento": {
    "codigo": "3017620425400",
    "nombre": "Avena",
    "marca": "Quaker",
    "kcalPor100g": 366,
    "proteinasPor100g": 13.2,
    "carbsPor100g": 58.7,
    "grasasPor100g": 6.9
  }
}

// Response 200
{ "ok": true }
```

---

### 3.14 Módulo Atleta

> Todos los endpoints de esta sección requieren cookie `access_token` válida con `rol === 'ATLETA'`.
> El frontend consume la base `/api/v1/atleta`.

---

#### 3.14.1 Perfil

**`GET /api/v1/atleta/perfil`**

Devuelve el perfil completo del atleta autenticado (datos registrados en el formulario de alta).

```json
{
  "ok": true,
  "data": {
    "id": "uuid",
    "nombre": "Carlos Ruiz",
    "correo": "carlos@ejemplo.com",
    "fechaNac": "1995-03-20",
    "genero": "HOMBRE",
    "peso": 80,
    "altura": 180,
    "deporte": "Ciclismo",
    "nivel": "AVANZADO",
    "servicio": "AMBOS",
    "objetivo": "RENDIMIENTO"
  }
}
```

> `objetivo` puede ser `null` si el atleta contrató solo nutrición.

---

**`GET /api/v1/atleta/entrenador?servicio=ENTRENAMIENTO|NUTRICION`**

Devuelve el entrenador asignado al atleta para el servicio indicado. Devuelve `null` si no tiene ninguno aún.

```json
{
  "ok": true,
  "data": {
    "id": "uuid-entrenador",
    "nombre": "Carlos Martínez",
    "titulacion": "GRADO_CAFYD",
    "servicio": "ENTRENAMIENTO"
  }
}
```

---

#### 3.14.2 Plan de Entrenamiento

**`GET /api/v1/atleta/entrenamiento/plan-activo`**

Devuelve la rutina activa del atleta (la más reciente). Devuelve `null` si no tiene ninguna.

El campo `semanaActual` se calcula en el servidor como `FLOOR(días_desde_creacion / 7) + 1`, limitado a `semanas`.

```json
{
  "ok": true,
  "data": {
    "id": "uuid-rutina",
    "nombre": "Fuerza — Mesociclo 2",
    "descripcion": "Bloque de hipertrofia con énfasis en tren superior.",
    "semanas": 8,
    "semanaActual": 3,
    "sesiones": [
      {
        "dia": "Lunes",
        "ejercicios": [
          {
            "nombre": "Press de banca",
            "series": 4,
            "reps": "8-10",
            "descanso": "90s",
            "notas": "Codos a 45° del torso"
          },
          {
            "nombre": "Remo en polea baja",
            "series": 4,
            "reps": "10-12",
            "descanso": "60s",
            "notas": null
          }
        ]
      },
      {
        "dia": "Miércoles",
        "ejercicios": [
          {
            "nombre": "Sentadilla",
            "series": 4,
            "reps": "6-8",
            "descanso": "120s",
            "notas": null
          }
        ]
      }
    ]
  }
}
```

**Campos de cada ejercicio:**

| Campo | Tipo | Notas |
|---|---|---|
| `nombre` | `string` | Nombre del ejercicio |
| `series` | `number` | Número de series |
| `reps` | `string` | Puede ser "8-10", "Máx", "Al fallo", "30s" |
| `descanso` | `string \| null` | Ej. "90s", "2 min". Opcional |
| `notas` | `string \| null` | Nota breve del entrenador sobre este ejercicio en la sesión |

> El campo `notas` de cada ejercicio es la nota que el entrenador dejó al diseñar la rutina. Es diferente del hilo de conversación (ver sección 3.14.6).

---

#### 3.14.3 Plan de Nutrición

**`GET /api/v1/atleta/nutricion/plan-activo`**

Devuelve el plan nutricional activo del atleta. Devuelve `null` si no tiene ninguno.

Los macros a nivel de plan (`proteinas`, `carbos`, `grasas`) son opcionales — el nutricionista puede o no incluirlos. Los macros a nivel de alimento también son opcionales.

```json
{
  "ok": true,
  "data": {
    "id": "uuid-plan",
    "nombre": "Definición — 2.400 kcal",
    "descripcion": "Déficit moderado con alta proteína.",
    "kcalDiarias": 2400,
    "proteinas": 195,
    "carbos": 260,
    "grasas": 65,
    "comidas": [
      {
        "nombre": "Desayuno",
        "alimentos": [
          {
            "nombre": "Avena",
            "cantidad": "80 g",
            "kcal": 300,
            "proteinas": 10,
            "carbos": 54,
            "grasas": 6
          },
          {
            "nombre": "Leche desnatada",
            "cantidad": "200 ml",
            "kcal": 70,
            "proteinas": 7,
            "carbos": 10,
            "grasas": 0
          }
        ]
      }
    ]
  }
}
```

**Notas sobre el campo `cantidad`:**
- Es un `string` libre tal como lo escribió el nutricionista: `"80 g"`, `"200 ml"`, `"1 unidad"`, `"2 cdas"`.
- El frontend lo muestra literalmente; no intenta parsearlo.

**Notas sobre los macros por alimento:**
- `kcal`, `proteinas`, `carbos`, `grasas` son todos opcionales (`null` si el nutricionista no los incluyó).
- Se calculan para la cantidad indicada (no por 100 g).
- El frontend suma las kcal de los alimentos de cada comida para mostrar el total de esa comida.

---

**`GET /api/v1/atleta/nutricion/notas`**

Devuelve las notas que el nutricionista ha dejado al atleta (consejos, observaciones generales). Ordenadas por `fecha DESC`.

```json
{
  "ok": true,
  "data": [
    {
      "id": "uuid-nota",
      "texto": "Intenta comer las comidas principales siempre a la misma hora.",
      "fecha": "2026-04-10"
    }
  ]
}
```

---

#### 3.14.4 Check-In de Peso

El registro de peso es **bajo demanda**: el atleta solo puede registrar su peso cuando el entrenador lo solicita explícitamente. El atleta no puede registrar peso libremente.

**`GET /api/v1/atleta/peso/solicitud-pendiente`**

Devuelve la solicitud de check-in pendiente del atleta (solo puede haber una activa a la vez). Devuelve `null` si no hay ninguna pendiente.

El frontend muestra un banner en el tab ENTRENAMIENTO cuando hay una solicitud activa.

```json
{
  "ok": true,
  "data": {
    "id": "uuid-solicitud",
    "fecha": "2026-04-16",
    "solicitadoPor": "Carlos López"
  }
}
```

---

**`POST /api/v1/atleta/peso`**

El atleta registra su peso en respuesta a una solicitud del entrenador.

```json
// Request body
{
  "solicitudId": "uuid-solicitud",
  "pesoKg": 81.5
}

// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-checkin",
    "fecha": "2026-04-16",
    "pesoKg": 81.5
  }
}
```

**Validaciones:**
- `pesoKg`: entre 30 y 300
- `solicitudId`: debe existir, estar en estado `PENDIENTE` y pertenecer al atleta autenticado
- Al registrar el peso, marcar la solicitud como `COMPLETADA`

**Response 400** si la solicitud no existe o ya fue completada:
```json
{ "ok": false, "error": "SOLICITUD_INVALIDA", "message": "La solicitud no existe o ya fue completada." }
```

---

**`GET /api/v1/atleta/peso/historial`**

Devuelve el historial de check-ins de peso del atleta, ordenados por fecha ASC. El frontend usa esto para renderizar la gráfica de evolución.

```json
{
  "ok": true,
  "data": [
    { "id": "uuid-w1", "fecha": "2026-02-03", "pesoKg": 85.2 },
    { "id": "uuid-w2", "fecha": "2026-02-17", "pesoKg": 84.0 },
    { "id": "uuid-w3", "fecha": "2026-03-03", "pesoKg": 83.1 }
  ]
}
```

> La gráfica solo se muestra si hay 2 o más registros. Con 0 o 1 registros el frontend no muestra nada.

---

#### 3.14.5 Solicitar Check-In (Entrenador → Atleta)

> Este endpoint lo llama el **entrenador** desde su dashboard, no el atleta.
> Requiere `rol === 'ENTRENADOR'` y `titulo_entrenamiento === true`.

**`POST /api/v1/entrenador/atletas/:atletaId/peso/solicitar`**

Crea una nueva solicitud de check-in de peso para el atleta indicado. Si ya existe una solicitud pendiente para ese atleta, devuelve `409` (no se puede acumular más de una).

```json
// Request body: vacío

// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-solicitud",
    "atletaId": "uuid-atleta",
    "fecha": "2026-04-16",
    "estado": "PENDIENTE"
  }
}
```

**Response 409** si ya hay una solicitud pendiente:
```json
{ "ok": false, "error": "SOLICITUD_YA_PENDIENTE", "message": "Este atleta ya tiene una solicitud de peso pendiente." }
```

---

#### 3.14.6 Hilo de Ejercicio

Cada ejercicio del plan de entrenamiento tiene un **hilo** propio donde:
- El entrenador puede haber dejado una **nota** al crear el plan (campo `notas` del ejercicio).
- El atleta puede **subir fotos o vídeos** de su técnica en ese ejercicio.
- Atleta y entrenador pueden **intercambiar mensajes** sobre ese ejercicio en particular.

El hilo se identifica por el nombre del día (`sesionDia`) y el nombre del ejercicio (`ejercicioNombre`), vinculados al atleta autenticado. En BBDD se recomienda resolverlo a través del `ejercicios_en_sesion.id` correcto.

---

**`GET /api/v1/atleta/entrenamiento/hilo`**

Query params: `?dia=Lunes&ejercicio=Press%20de%20banca`

Devuelve el hilo completo del ejercicio indicado. Si el hilo no existe aún (el atleta abre un ejercicio por primera vez), devuelve un hilo vacío con `media: []` y `mensajes: []`.

```json
{
  "ok": true,
  "data": {
    "sesionDia": "Lunes",
    "ejercicioNombre": "Press de banca",
    "notaEntrenador": "Codos a 45° del torso, baja explosivo.",
    "media": [
      {
        "id": "uuid-media",
        "tipo": "foto",
        "url": "https://s3.../...",
        "fecha": "2026-04-15"
      }
    ],
    "mensajes": [
      {
        "id": "uuid-msg",
        "texto": "¿Estoy bajando suficiente los codos?",
        "fecha": "2026-04-15",
        "esAtleta": true,
        "autor": "Carlos Ruiz"
      },
      {
        "id": "uuid-msg2",
        "texto": "Sí, pero baja un poco más la barra.",
        "fecha": "2026-04-15",
        "esAtleta": false,
        "autor": "Carlos López"
      }
    ]
  }
}
```

**Campos del response:**

| Campo | Tipo | Notas |
|---|---|---|
| `notaEntrenador` | `string \| null` | Viene del campo `notas` del ejercicio en la rutina activa |
| `media` | `MediaAdjunto[]` | Archivos subidos por el atleta (fotos/vídeos en S3) |
| `mensajes` | `MensajeHilo[]` | Mensajes de texto del hilo, ordenados por `fecha ASC` |
| `esAtleta` | `boolean` | `true` si lo envió el atleta, `false` si lo envió el entrenador |

> Las URLs de media son **pre-signed URLs de S3** con expiración corta (ej. 15 min). No URLs públicas permanentes.

---

**`POST /api/v1/atleta/entrenamiento/hilo/mensaje`**

El atleta envía un mensaje de texto al hilo de un ejercicio.

```json
// Request body
{
  "sesionDia": "Lunes",
  "ejercicioNombre": "Press de banca",
  "texto": "¿Bajo los codos más?"
}

// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-msg",
    "texto": "¿Bajo los codos más?",
    "fecha": "2026-04-17",
    "esAtleta": true,
    "autor": "Carlos Ruiz"
  }
}
```

---

**`POST /api/v1/atleta/entrenamiento/hilo/media`**

El atleta sube una foto o vídeo al hilo de un ejercicio. Requiere S3.

Recibe `multipart/form-data`:

| Campo | Tipo | Descripción |
|---|---|---|
| `archivo` | `File` | Imagen (JPG/PNG/WEBP) o vídeo (MP4/MOV/WEBM). Max 100 MB |
| `sesionDia` | `string` | Nombre del día ("Lunes", "Miércoles", …) |
| `ejercicioNombre` | `string` | Nombre exacto del ejercicio |

```json
// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-media",
    "tipo": "foto",
    "url": "https://s3.presigned.../...",
    "fecha": "2026-04-17"
  }
}
```

> El campo `tipo` se infiere del MIME type del archivo: `image/*` → `"foto"`, `video/*` → `"video"`.

---

#### 3.14.7 Chat General

Chat de texto entre el atleta y su entrenador/nutricionista. Hay un chat por cada relación de servicio activa:
- Si `servicio = ENTRENAMIENTO` → un único chat con el entrenador
- Si `servicio = NUTRICION` → un único chat con el nutricionista
- Si `servicio = AMBOS` → dos chats separados (uno con cada profesional)

El frontend identifica el chat con `tipo: 'entrenador' | 'nutricionista'`. El backend resuelve eso al entrenador real asignado a través de la tabla `asignaciones`.

---

**`GET /api/v1/atleta/chat/:tipo`**

`:tipo` es `entrenador` o `nutricionista`.

Devuelve el historial completo del chat, ordenado por `fecha ASC`.

```json
{
  "ok": true,
  "data": {
    "tipo": "entrenador",
    "interlocutor": "Carlos López",
    "mensajes": [
      {
        "id": "uuid-msg",
        "texto": "¡Hola! ¿Cómo llevas la semana?",
        "fecha": "2026-04-10",
        "esAtleta": false,
        "autor": "Carlos López"
      },
      {
        "id": "uuid-msg2",
        "texto": "Bien, aunque noto las piernas cargadas los lunes",
        "fecha": "2026-04-10",
        "esAtleta": true,
        "autor": "Carlos Ruiz"
      }
    ]
  }
}
```

**Response 400** si el tipo no corresponde a un servicio contratado:
```json
{ "ok": false, "error": "CHAT_NO_DISPONIBLE", "message": "No tienes un servicio de nutrición activo." }
```

---

**`POST /api/v1/atleta/chat/:tipo/mensaje`**

El atleta envía un mensaje al chat con su entrenador o nutricionista.

```json
// Request body
{ "texto": "¿Puedo cambiar el press militar por press inclinado?" }

// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-msg",
    "texto": "¿Puedo cambiar el press militar por press inclinado?",
    "fecha": "2026-04-17",
    "esAtleta": true,
    "autor": "Carlos Ruiz"
  }
}
```

---

#### 3.14.8 Ajustes de Cuenta (Atleta)

**`PUT /api/v1/atleta/password`**

El atleta cambia su contraseña. Se requiere la contraseña actual para verificar identidad.

```json
// Request body
{
  "actual": "contraseñaActual123",
  "nueva": "contraseñaNueva456"
}

// Response 200
{ "ok": true }
```

**Response 400** si la contraseña actual es incorrecta:
```json
{ "ok": false, "error": "PASSWORD_INCORRECTO", "message": "La contraseña actual no es correcta." }
```

**Validaciones:**
- `nueva`: mínimo 8 caracteres (el frontend lo valida también, pero el backend debe confirmarlo)
- Hashear con bcrypt antes de guardar

---

#### 3.14.9 Profesionales Asignados

**`GET /api/v1/atleta/profesionales`**

Devuelve los profesionales asignados al atleta según sus servicios contratados. Máximo 2 registros (uno por servicio). Si el mismo profesional cubre ambos servicios, puede aparecer dos veces con distinto `rol`.

```json
// Response 200
[
  {
    "id": "uuid-prof-1",
    "nombre": "Carlos López",
    "titulacion": "Grado en Ciencias de la Actividad Física y del Deporte",
    "rol": "ENTRENADOR",
    "descripcion": "Especialista en fuerza e hipertrofia..."
  },
  {
    "id": "uuid-prof-2",
    "nombre": "María González",
    "titulacion": "Dietista-Nutricionista (Graduada en Nutrición Humana y Dietética)",
    "rol": "NUTRICIONISTA",
    "descripcion": "Especializada en nutrición deportiva..."
  }
]
```

**Lógica de construcción:**
- Consultar `asignaciones` filtrando por `atleta_id` y `estado = ACTIVO`
- Para cada asignación, hacer JOIN con `entrenadores` → `usuarios` para obtener nombre, titulación y descripción
- Mapear `servicio = ENTRENAMIENTO` → `rol = ENTRENADOR`, `servicio = NUTRICION` → `rol = NUTRICIONISTA`
- Si el atleta tiene `servicio = AMBOS` con el mismo profesional, devolver dos entradas con el mismo `id` pero distinto `rol`

**Campos de `entrenadores` necesarios para esta respuesta:**
- `titulacion` (ya existe)
- `descripcion` — campo libre que el entrenador rellena en su perfil (añadir si no existe)

---

#### 3.14.10 Hilo de Comida

Conversación entre el atleta y su nutricionista vinculada a una comida concreta del plan nutricional.

**`GET /api/v1/atleta/nutricion/hilo?comida={comidaNombre}`**

```json
// Response 200
{
  "comidaNombre": "Desayuno",
  "mensajes": [
    {
      "id": "uuid-msg",
      "texto": "¿Puedo sustituir la leche desnatada por bebida de avena?",
      "fecha": "2026-04-12",
      "esAtleta": true,
      "autor": "Rodrigo"
    },
    {
      "id": "uuid-msg-2",
      "texto": "Sí, sin problema. Elige la variante sin azúcares añadidos.",
      "fecha": "2026-04-12",
      "esAtleta": false,
      "autor": "María González"
    }
  ]
}
```

**`POST /api/v1/atleta/nutricion/hilo/mensaje`**

```json
// Request body
{
  "comidaNombre": "Desayuno",
  "texto": "¿Puedo sustituir la leche desnatada por bebida de avena?"
}

// Response 201
{
  "id": "uuid-nuevo-msg",
  "texto": "¿Puedo sustituir la leche desnatada por bebida de avena?",
  "fecha": "2026-04-17",
  "esAtleta": true,
  "autor": "Rodrigo"
}
```

**BBDD — tabla `mensajes_hilo_comida`:**

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `atleta_id` | UUID FK → atletas | |
| `comida_nombre` | VARCHAR(100) | nombre de la comida tal como aparece en el plan (ej: "Desayuno") |
| `texto` | TEXT | |
| `es_atleta` | BOOLEAN | `true` si lo escribió el atleta, `false` si fue el nutricionista |
| `autor` | VARCHAR(255) | nombre del autor para mostrar en la UI |
| `created_at` | TIMESTAMP | se usa como `fecha` en la respuesta |

**Resolución del hilo:**
- El hilo se identifica por `(atleta_id, comida_nombre)`
- `comida_nombre` es el valor exacto del campo `nombre` de la comida en el plan nutricional activo
- Si no existe hilo para esa comida, devolver `{ comidaNombre, mensajes: [] }`
- El nutricionista puede responder desde el dashboard del entrenador (endpoint pendiente de definir en sección 3.x del entrenador)

---

## 4. Modelos de Base de Datos

> El esquema SQL detallado estará en `/bbdd`. Aquí se describen las entidades a modo de contrato.

### Tabla `usuarios`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `nombre` | VARCHAR(255) | |
| `correo` | VARCHAR(255) UNIQUE | |
| `password_hash` | VARCHAR(255) | bcrypt, min cost 12 |
| `rol` | ENUM | `ENTRENADOR`, `ATLETA` |
| `estado` | ENUM | `PENDIENTE_REVISION`, `ACTIVO`, `RECHAZADO`, `SUSPENDIDO` |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

### Tabla `entrenadores`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK FK → usuarios | |
| `codigo_profesional` | VARCHAR(20) UNIQUE NULLABLE | |
| `titulacion_entrenamiento` | ENUM NULLABLE | `GRADO_CAFYD`, `TSAF_TSEAS`, `CERT_AFDA0210` |
| `titulacion_nutricion` | ENUM NULLABLE | `GRADO_NUTRICION_DIETETICA`, `TSD` |
| `titulo_entrenamiento` | BOOLEAN | Derivado: `titulacion_entrenamiento IS NOT NULL` |
| `titulo_nutricion` | BOOLEAN | Derivado: `titulacion_nutricion IS NOT NULL` |
| `experiencia_anos` | SMALLINT NULLABLE | |
| `descripcion` | TEXT NULLABLE | |

### Tabla `documentos_entrenador`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | |
| `nombre_archivo` | VARCHAR(255) | |
| `url_s3` | TEXT | URL privada (nunca pública) |
| `tipo_mime` | VARCHAR(50) | |
| `tamanyo_bytes` | INTEGER | |
| `status` | ENUM | `pending`, `verified`, `rejected` |
| `rejection_reason` | TEXT NULLABLE | |
| `uploaded_at` | TIMESTAMP | |
| `reviewed_at` | TIMESTAMP NULLABLE | |

### Tabla `atletas`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK FK → usuarios | |
| `fecha_nac` | DATE | |
| `genero` | ENUM | `HOMBRE`, `MUJER`, `OTRO` |
| `peso_kg` | DECIMAL(5,2) | |
| `altura_cm` | SMALLINT | |
| `deporte` | VARCHAR(100) | |
| `nivel` | ENUM | `PRINCIPIANTE`, `INTERMEDIO`, `AVANZADO`, `ELITE` |
| `servicio` | ENUM | `ENTRENAMIENTO`, `NUTRICION`, `AMBOS` |
| `objetivo` | ENUM NULLABLE | `RENDIMIENTO`, `MASA_MUSCULAR`, `PERDER_PESO`, `SALUD`, `RESISTENCIA` |

### Tabla `asignaciones`

Relación entre entrenador y atleta para un servicio concreto.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | |
| `atleta_id` | UUID FK → atletas | |
| `servicio` | ENUM | `ENTRENAMIENTO` \| `NUTRICION` — un registro por servicio |
| `activa` | BOOLEAN | `true` mientras esté vigente |
| `creada_en` | TIMESTAMP | |

> Un atleta con `servicio = 'AMBOS'` tendrá dos registros: uno para `ENTRENAMIENTO` y otro para `NUTRICION` (pueden ser con distintos entrenadores).

### Tabla `planes_nutricion`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | Quien lo creó |
| `atleta_id` | UUID FK → atletas | Para quien es |
| `nombre` | VARCHAR(255) | |
| `descripcion` | TEXT NULLABLE | |
| `creado_en` | TIMESTAMP | |

### Tabla `comidas`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `plan_id` | UUID FK → planes_nutricion | |
| `nombre` | VARCHAR(100) | "Desayuno", "Almuerzo", etc. |
| `orden` | SMALLINT | Para mantener el orden de las comidas del día |

### Tabla `alimentos_en_comida`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `comida_id` | UUID FK → comidas | |
| `codigo_alimento` | VARCHAR(50) | Código de Open Food Facts (barcode) |
| `nombre` | VARCHAR(255) | Nombre del alimento (snapshot en el momento de guardar) |
| `marca` | VARCHAR(255) NULLABLE | |
| `kcal_por_100g` | DECIMAL(7,2) | |
| `proteinas_por_100g` | DECIMAL(7,2) | |
| `carbs_por_100g` | DECIMAL(7,2) | |
| `grasas_por_100g` | DECIMAL(7,2) | |
| `cantidad_g` | DECIMAL(7,2) | Cantidad en gramos para este plan |

> Los macros se guardan como snapshot porque los datos de Open Food Facts pueden cambiar. No hay FK a una tabla de alimentos propia.

### Tabla `recetas`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | Quien la creó |
| `nombre` | VARCHAR(255) | |
| `gramos_total` | DECIMAL(7,2) | Peso total del plato (puede diferir de la suma de ingredientes) |
| `creado_en` | TIMESTAMP | |

### Tabla `ingredientes_receta`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `receta_id` | UUID FK → recetas | |
| `codigo_alimento` | VARCHAR(50) | Código de Open Food Facts (barcode) |
| `nombre` | VARCHAR(255) | Snapshot del nombre al guardar |
| `marca` | VARCHAR(255) NULLABLE | |
| `kcal_por_100g` | DECIMAL(7,2) | |
| `proteinas_por_100g` | DECIMAL(7,2) | |
| `carbs_por_100g` | DECIMAL(7,2) | |
| `grasas_por_100g` | DECIMAL(7,2) | |
| `cantidad_g` | DECIMAL(7,2) | |
| `orden` | SMALLINT | |

### Tabla `alimentos_recientes`

Historial de alimentos usados por el entrenador en cada tipo de comida. Máximo 8 registros por combinación `(usuario_id, nombre_comida)`.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `usuario_id` | UUID FK → usuarios | Entrenador que lo usó |
| `nombre_comida` | VARCHAR(100) | "Desayuno", "Almuerzo", etc. |
| `codigo_alimento` | VARCHAR(50) | |
| `nombre` | VARCHAR(255) | Snapshot |
| `marca` | VARCHAR(255) NULLABLE | |
| `kcal_por_100g` | DECIMAL(7,2) | |
| `proteinas_por_100g` | DECIMAL(7,2) | |
| `carbs_por_100g` | DECIMAL(7,2) | |
| `grasas_por_100g` | DECIMAL(7,2) | |
| `usado_en` | TIMESTAMP | Se actualiza cada vez que se usa |

> **Constraint único:** `(usuario_id, nombre_comida, codigo_alimento)` — evita duplicados por alimento y comida. El backend debe hacer `UPSERT` actualizando `usado_en` si ya existe.

### Tabla `checkins_peso_solicitudes`

Solicitudes de check-in de peso creadas por el entrenador. Solo puede haber una `PENDIENTE` por atleta a la vez.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | Quien la solicitó |
| `atleta_id` | UUID FK → atletas | Para quien es |
| `estado` | ENUM | `PENDIENTE`, `COMPLETADA` |
| `creada_en` | TIMESTAMP | |
| `completada_en` | TIMESTAMP NULLABLE | |

> **Constraint:** `UNIQUE (atleta_id) WHERE estado = 'PENDIENTE'` — evita más de una solicitud pendiente por atleta.

### Tabla `checkins_peso`

Registros de peso enviados por el atleta en respuesta a una solicitud.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `solicitud_id` | UUID FK → checkins_peso_solicitudes | Solicitud que originó este registro |
| `atleta_id` | UUID FK → atletas | |
| `peso_kg` | DECIMAL(5,2) | Entre 30 y 300 |
| `fecha` | DATE | Fecha del registro (`YYYY-MM-DD`) |

### Tabla `notas_nutricionista`

Notas generales que el nutricionista deja al atleta (visibles en el tab DIETA).

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | Nutricionista que la escribió |
| `atleta_id` | UUID FK → atletas | |
| `texto` | TEXT | |
| `fecha` | DATE | |
| `creada_en` | TIMESTAMP | |

### Tabla `hilos_ejercicio`

Un hilo por combinación (atleta, ejercicio de su rutina activa). Se crea la primera vez que el atleta abre un ejercicio.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `atleta_id` | UUID FK → atletas | |
| `ejercicio_en_sesion_id` | UUID FK → ejercicios_en_sesion | Ejercicio concreto de la rutina |
| `creado_en` | TIMESTAMP | |

> **Constraint:** `UNIQUE (atleta_id, ejercicio_en_sesion_id)`
>
> Para resolver la clave (`sesionDia`, `ejercicioNombre`) del frontend a un `ejercicio_en_sesion_id`, el backend debe buscar en la rutina activa del atleta la sesión con `sesiones_rutina.nombre = sesionDia` y el ejercicio con `ejercicios_en_sesion.ejercicio_nombre = ejercicioNombre`.

### Tabla `media_hilo`

Fotos y vídeos subidos por el atleta a un hilo de ejercicio. Almacenados en S3.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `hilo_id` | UUID FK → hilos_ejercicio | |
| `tipo` | ENUM | `foto`, `video` |
| `url_s3` | TEXT | URL privada en S3 |
| `fecha` | DATE | |
| `subido_en` | TIMESTAMP | |

### Tabla `mensajes_hilo`

Mensajes de texto del hilo de un ejercicio (atleta ↔ entrenador).

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `hilo_id` | UUID FK → hilos_ejercicio | |
| `usuario_id` | UUID FK → usuarios | Quien lo envió |
| `texto` | TEXT | |
| `es_atleta` | BOOLEAN | `true` si lo envió el atleta, `false` si el entrenador |
| `autor` | VARCHAR(255) | Nombre del autor (snapshot) |
| `enviado_en` | TIMESTAMP | |
| `fecha` | DATE | Derivada de `enviado_en` (para el frontend) |

### Tabla `mensajes_chat`

Mensajes del chat general atleta ↔ entrenador/nutricionista.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `asignacion_id` | UUID FK → asignaciones | Identifica la relación entrenador-atleta-servicio |
| `usuario_id` | UUID FK → usuarios | Quien lo envió |
| `texto` | TEXT | |
| `es_atleta` | BOOLEAN | `true` si lo envió el atleta |
| `autor` | VARCHAR(255) | Nombre del autor (snapshot) |
| `enviado_en` | TIMESTAMP | |
| `fecha` | DATE | Derivada de `enviado_en` |

> El `tipo` ('entrenador' o 'nutricionista') que envía el frontend se resuelve a un `asignacion_id` buscando en `asignaciones` por `(atleta_id, servicio)` donde `servicio = 'ENTRENAMIENTO'` para 'entrenador' y `servicio = 'NUTRICION'` para 'nutricionista'.

### Tabla `rutinas`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | |
| `atleta_id` | UUID FK → atletas | |
| `nombre` | VARCHAR(255) | |
| `descripcion` | TEXT NULLABLE | |
| `creado_en` | TIMESTAMP | |

### Tabla `sesiones_rutina`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `rutina_id` | UUID FK → rutinas | |
| `nombre` | VARCHAR(100) | "Piernas", "Pecho", etc. |
| `orden` | SMALLINT | |

### Tabla `ejercicios_en_sesion`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `sesion_id` | UUID FK → sesiones_rutina | |
| `ejercicio_id` | VARCHAR(100) | ID del ejercicio en el dataset yuhonas (ej. `barbell-squat`) |
| `ejercicio_nombre` | VARCHAR(255) | Snapshot del nombre traducido al guardar |
| `ejercicio_categoria` | VARCHAR(100) | Snapshot |
| `ejercicio_musculo_principal` | VARCHAR(100) | Snapshot |
| `ejercicio_imagen_url` | TEXT NULLABLE | Snapshot de la primera imagen |
| `series` | SMALLINT | |
| `reps` | VARCHAR(50) | "10", "8-12", "Al fallo", "30 seg" |
| `notas` | TEXT NULLABLE | |
| `orden` | SMALLINT | |

> Los datos del ejercicio se guardan como snapshot porque el dataset externo no es propiedad de GRIT. La URL de imagen puede quedarse obsoleta si el dataset externo cambia.

---

## 5. Seguridad

- Todas las rutas bajo `/api/v1/` deben servirse **únicamente por HTTPS** en producción.
- Los endpoints de registro y login deben tener **rate limiting**: máx. 10 peticiones por IP cada 15 minutos.
- Los archivos subidos deben validarse en servidor (no solo por extensión): verificar el **magic number / MIME type real** del binario.
- Las URLs de S3 deben ser **privadas con firma temporal** (pre-signed URLs), nunca públicas.
- Contraseñas: **bcrypt** con cost factor mínimo 12.
- **Tokens via cookies HttpOnly** — los JWTs nunca se exponen al JavaScript del frontend.
  - `access_token`: `HttpOnly; Secure; SameSite=Strict; Max-Age=900` (15 min)
  - `refresh_token`: `HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=604800` (7 días)

---

## 6. CORS

El frontend corre en `http://localhost:4200` en desarrollo. Configurar CORS para:

```
Allowed Origins (dev):   http://localhost:4200
Allowed Origins (prod):  https://grit.app (pendiente definir)
Allowed Methods:         GET, POST, PUT, PATCH, DELETE, OPTIONS
Allowed Headers:         Content-Type, Authorization
Allow-Credentials:       true   ← imprescindible para que las cookies HttpOnly funcionen
```

---

## 7. Emails a implementar

| Trigger | Destinatario | Asunto sugerido |
|---|---|---|
| Registro entrenador | Entrenador | "Solicitud recibida — revisaremos tus credenciales en 48h" |
| Aprobación entrenador | Entrenador | "¡Bienvenido a GRIT! Tu cuenta está activa" |
| Rechazo entrenador | Entrenador | "Actualización sobre tu solicitud en GRIT" |
| Registro atleta | Atleta | "¡Bienvenido a GRIT! Tu perfil está listo" |

---

## 8. Variables de Entorno Necesarias

```env
# Servidor
PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/grit
SPRING_DATASOURCE_USERNAME=grit_user
SPRING_DATASOURCE_PASSWORD=cambiar-en-produccion

# JWT
JWT_SECRET=cambiar-en-produccion-min-256-bits
JWT_EXPIRES_IN=900          # segundos (15 min)
JWT_REFRESH_SECRET=cambiar-en-produccion-min-256-bits
JWT_REFRESH_EXPIRES_IN=604800  # segundos (7 días)

# S3
S3_BUCKET=grit-documentos
S3_REGION=eu-west-1
S3_ACCESS_KEY_ID=...
S3_SECRET_ACCESS_KEY=...

# Email
EMAIL_FROM=noreply@grit.app
SENDGRID_API_KEY=...
```

---

## 9. Orden de Implementación Sugerido

1. **Setup del proyecto** (estructura Spring Boot, Docker Compose con PostgreSQL)
2. **Migraciones de BBDD** (schema SQL inicial — tablas `usuarios`, `entrenadores`, `atletas`, `asignaciones`)
3. **Healthcheck** (`GET /api/v1/health`)
4. **Registro de Atleta** + **Login** + **Refresh** + **Logout** + **Me**
   - El bloque de auth completo: cookies HttpOnly, JWT, redirección
5. **Registro de Entrenador** (multipart/form-data, upload a S3, estado `PENDIENTE_REVISION`)
6. **Endpoints de administración** (aprobar/rechazar, pre-signed URLs de S3)
7. **Sistema de emails** (registro, aprobación, rechazo)
8. **Middleware de protección por titulación** (secciones 3.8)
9. **Módulo Entrenador** — perfil y lista de atletas (sección 3.11)
   - Migración: tabla `asignaciones`
10. **Módulo Nutrición** — planes (sección 3.12)
    - Migraciones: `planes_nutricion`, `comidas`, `alimentos_en_comida`
11. **Módulo Nutrición** — recetas y alimentos recientes (secciones 3.15 y 3.16)
    - Migraciones: `recetas`, `ingredientes_receta`, `alimentos_recientes`
12. **Módulo Entrenamiento** — rutinas (sección 3.13)
    - Migraciones: `rutinas`, `sesiones_rutina`, `ejercicios_en_sesion`
13. **Módulo Atleta — bloque 1:** perfil, plan activo entrenamiento, plan activo nutrición, notas nutricionista (secciones 3.14.1–3.14.3)
14. **Módulo Atleta — bloque 2:** check-in de peso (solicitud entrenador + registro atleta + historial) (sección 3.14.4–3.14.5)
    - Migraciones: `checkins_peso_solicitudes`, `checkins_peso`
15. **Módulo Atleta — bloque 3:** hilo de ejercicio con media S3 (sección 3.14.6)
    - Migraciones: `hilos_ejercicio`, `media_hilo`, `mensajes_hilo`
16. **Módulo Atleta — bloque 4:** chat general atleta ↔ entrenador/nutricionista (sección 3.14.7)
    - Migración: `mensajes_chat`
17. **Módulo Atleta — bloque 5:** ajustes de cuenta (cambiar contraseña) (sección 3.14.8)
18. **Módulo Atleta — bloque 6:** profesionales asignados + hilo de comida (secciones 3.14.9–3.14.10)
    - Migración: `mensajes_hilo_comida`; campo `descripcion` en `entrenadores` si no existe
19. **Notas nutricionista** (escritura desde el dashboard del entrenador)
    - Migración: `notas_nutricionista`
19. **Rate limiting + seguridad adicional**
20. **Tests de integración** para todos los endpoints

---

## 10. Notas de Integración Frontend ↔ Backend

- **Campo `email` en login y registro:** el frontend envía `email` (no `correo`) en todos los endpoints de auth. La BBDD puede almacenarlo como `correo` pero el campo JSON del body es `email`.
- **`withCredentials: true`:** todas las peticiones HTTP del frontend incluyen esta opción. El backend debe responder con `Access-Control-Allow-Credentials: true` y un `Origin` específico (no `*`) en la cabecera CORS.
- **Datos de ejercicios:** el frontend obtiene los ejercicios directamente del dataset externo `yuhonas/free-exercise-db` (GitHub raw) y de MyMemory para traducciones. No hay endpoint de ejercicios en GRIT. Los datos se guardan embebidos en `ejercicios_en_sesion` como snapshot.
- **Recetas:** se almacenan en backend (sección 3.15). El frontend llama a `GET /nutricion/recetas` al cargar el picker y `POST /nutricion/recetas` al guardar. La migración desde `localStorage` es responsabilidad del frontend al conectar con la API real.
- **Alimentos recientes:** se almacenan en backend (sección 3.16). El frontend llama a `POST /nutricion/recientes` cada vez que añade un alimento a una comida, y `GET /nutricion/recientes?comida=<nombre>` para prellenar los recientes en el buscador.
- **`tienePlanActivo` en `/entrenador/atletas`:** calcular en BBDD si el atleta tiene alguna rutina o plan de nutrición creado por este entrenador (JOIN con `rutinas` y `planes_nutricion`).
- **`semanaActual` en plan de entrenamiento:** el frontend lo usa solo para mostrar "Semana 3/8". Se calcula como `FLOOR((CURRENT_DATE - rutinas.creado_en::date) / 7) + 1`, con un tope de `semanas`.
- **Check-in de peso — flujo completo:** (1) entrenador llama a `POST /entrenador/atletas/:id/peso/solicitar` → (2) atleta ve el banner via `GET /atleta/peso/solicitud-pendiente` → (3) atleta registra con `POST /atleta/peso` pasando `solicitudId` → (4) backend marca la solicitud como `COMPLETADA` → (5) el banner desaparece (el endpoint devuelve `null`).
- **Hilo de ejercicio — identificación:** el frontend envía `dia` (nombre del día: "Lunes", "Miércoles"…) y `ejercicio` (nombre exacto: "Press de banca"). El backend debe resolver esto a un `ejercicios_en_sesion.id` haciendo JOIN: `rutinas → sesiones_rutina (nombre = dia) → ejercicios_en_sesion (ejercicio_nombre = ejercicio)` filtrando por atleta y rutina activa. Si no existe el hilo todavía, crear un registro en `hilos_ejercicio` y devolver hilo vacío.
- **Hilo de ejercicio — campo `notaEntrenador`:** se obtiene de `ejercicios_en_sesion.notas` de la rutina activa. No es un campo de `hilos_ejercicio`, es la nota que el entrenador escribió al crear el plan.
- **Media del hilo:** las URLs devueltas por `GET /atleta/entrenamiento/hilo` deben ser pre-signed URLs de S3 con expiración corta (15 min). El frontend las usa directamente en `<img>` y `<video>`.
- **Chat general — resolución del `tipo`:** el frontend envía `'entrenador'` o `'nutricionista'`. El backend resuelve al entrenador asignado: buscar en `asignaciones` por `atleta_id` del autenticado y `servicio = 'ENTRENAMIENTO'` (para tipo entrenador) o `servicio = 'NUTRICION'` (para tipo nutricionista). Si no hay asignación activa, devolver `400 CHAT_NO_DISPONIBLE`.
- **Respuesta del chat — campo `autor`:** el frontend muestra el nombre del interlocutor tal como viene en `interlocutor` (nombre del entrenador/nutricionista). Para los mensajes, `autor` es el nombre del usuario que lo envió. El frontend muestra "Tú" cuando `esAtleta === true`, ignorando el campo `autor` del mensaje — pero debe estar en la respuesta para cuando el entrenador consulte el chat desde su dashboard.
- **Cambio de contraseña del atleta:** `PUT /api/v1/atleta/password` — verificar `actual` contra el hash en BBDD antes de actualizar. El frontend valida que `nueva` tenga al menos 8 caracteres, pero el backend debe confirmarlo también.
- **`cantidad` en alimentos del plan nutricional:** es un campo de texto libre (`string`), no un número. El nutricionista escribe "80 g", "1 unidad", "2 cucharadas". El backend lo almacena y devuelve tal cual, sin parsear ni validar el formato.

---

