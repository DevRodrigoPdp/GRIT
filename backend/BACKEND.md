# GRIT — Especificación Backend

> **Rama de trabajo:** `frontend` (en desarrollo) · **Stack frontend:** Angular 21 + Tailwind CSS
> **Este documento** describe todos los contratos de API, modelos de datos y requisitos que el equipo de backend debe implementar para dar soporte a la plataforma GRIT.
> **Última actualización:** 22 de abril de 2026 — cobertura completa: historial y estado pendiente de peso desde entrenador (3.14.5); escritura notas nutricionista (3.12); endpoints admin para aprobar/rechazar ampliaciones (3.9); foto de perfil (3.14.8, 3.19); endpoint unificado ampliación formación (3.7); eliminación recetas

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
    │   │   ├── ejercicio.service.ts
    │   │   └── alimentos.service.ts
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
| `alergias` | `string[]` | ❌ Opcional | Array de strings libre. Ej: `["Frutos secos", "Marisco"]`. Si no se envía, guardar como `[]` |
| `intolerancias` | `string[]` | ❌ Opcional | Array de strings libre. Ej: `["Lactosa", "Gluten"]`. Si no se envía, guardar como `[]` |
| `codigoInvitacion` | `string` | ❌ Opcional | Código del entrenador que invitó al atleta. Si es válido, crear asignación automáticamente tras el registro |

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
  "password": "contraseña",
  "visitorId": "abc123xyz"
}
```

> `visitorId` es el identificador de dispositivo generado por FingerprintJS en el frontend. Si no se envía, tratarlo como dispositivo desconocido.

**Response 200 — dispositivo conocido** (tokens en cookies, flujo normal):
```json
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

**Response 200 — dispositivo desconocido** (sin cookies, requiere MFA):
```json
{
  "ok": true,
  "status": "MFA_REQUIRED",
  "mfaToken": "token-temporal-opaco"
}
```

> `mfaToken` identifica la solicitud MFA pendiente. No es un JWT de sesión. Expira en 10 minutos.

> `tituloEntrenamiento` y `tituloNutricion` solo son relevantes cuando `rol === "ENTRENADOR"`. Para atletas devolver `null` en ambos. `servicio` solo es relevante para atletas; para entrenadores devolver `null`.

**Cookies que debe setear el servidor (solo en login exitoso sin MFA):**
```
Set-Cookie: access_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900
Set-Cookie: refresh_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=604800
```

**Lógica de redirección que aplica el frontend según la respuesta:**

| `status` | `rol` | `estado` | `tituloEntrenamiento` | `tituloNutricion` | Acción frontend |
|---|---|---|---|---|---|
| `MFA_REQUIRED` | — | — | — | — | Mostrar pantalla OTP |
| — | `ATLETA` | `ACTIVO` | `null` | `null` | `/dashboard/atleta` |
| — | `ENTRENADOR` | `ACTIVO` | `true` | `true` | `/dashboard/entrenador/nutricion` |
| — | `ENTRENADOR` | `ACTIVO` | `true` | `false` | `/dashboard/entrenador` |
| — | `ENTRENADOR` | `ACTIVO` | `false` | `true` | `/dashboard/entrenador/solo-nutricion` |
| — | `ENTRENADOR` | `PENDIENTE_REVISION` | cualquiera | cualquiera | `/pendiente` |
| — | cualquiera | `RECHAZADO` | cualquiera | cualquiera | `/login` con error |

---

### 3.3.1 Verificación MFA

**`POST /api/v1/auth/mfa/verificar`**

```json
// Request body
{
  "mfaToken": "token-temporal-opaco",
  "codigo": "847291",
  "visitorId": "abc123xyz"
}
```

**Response 200 — código correcto** (cookies JWT igual que login normal):
```json
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

**Response 401 — código incorrecto:**
```json
{ "ok": false, "error": "CODIGO_INVALIDO", "intentosRestantes": 4 }
```

**Response 410 — token expirado o agotado:**
```json
{ "ok": false, "error": "MFA_EXPIRADO" }
```

---

### 3.3.2 Flujo completo MFA + FingerprintJS

#### Lo que hace el BACKEND

1. Recibe `POST /auth/login` con `{ email, password, visitorId }`
2. Verifica credenciales contra la BBDD
3. Busca `visitorId` en `dispositivos_verificados` para ese usuario
4. **Si el dispositivo es conocido:**
   - Emite cookies JWT (`access_token` + `refresh_token`)
   - Devuelve los datos de sesión normales (`rol`, `nombre`, etc.)
5. **Si el dispositivo es desconocido:**
   - Genera un código OTP de 6 dígitos aleatorio
   - Lo guarda hasheado en `mfa_codigos` con expiración de 10 min
   - Genera un `mfaToken` opaco (UUID) y lo guarda también en `mfa_codigos`
   - Envía el código al correo del usuario (asunto: "Tu código de verificación GRIT")
   - Devuelve `{ status: "MFA_REQUIRED", mfaToken }`
6. Recibe `POST /auth/mfa/verificar` con `{ mfaToken, codigo, visitorId }`
7. Busca el `mfaToken` en `mfa_codigos` y verifica que no ha expirado ni está usado
8. Compara el `codigo` con el hash almacenado
9. **Si es correcto:**
   - Registra `visitorId` en `dispositivos_verificados` para ese usuario
   - Marca `mfa_codigos.usado = true`
   - Emite cookies JWT
   - Devuelve los datos de sesión normales
10. **Si el código es incorrecto:** incrementa `intentos`, devuelve `CODIGO_INVALIDO` con intentos restantes. Al llegar a 5 intentos, invalida el `mfaToken`
11. **Si el token expiró o se agotaron los intentos:** devuelve `MFA_EXPIRADO`

**BBDD — tabla `dispositivos_verificados`:**

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `usuario_id` | UUID FK → usuarios | |
| `visitor_id` | VARCHAR(255) | `visitorId` de FingerprintJS |
| `verificado_en` | TIMESTAMP | |

> **Constraint único:** `(usuario_id, visitor_id)` — un dispositivo verificado no vuelve a pedir MFA a ese usuario.

**BBDD — tabla `mfa_codigos`:**

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `usuario_id` | UUID FK → usuarios | |
| `mfa_token` | VARCHAR(255) UNIQUE | Token opaco enviado al frontend |
| `codigo_hash` | VARCHAR(255) | bcrypt del código de 6 dígitos |
| `visitor_id` | VARCHAR(255) | Para asociar con el dispositivo al verificar |
| `intentos` | SMALLINT | Empieza en 0, máximo 5 |
| `usado` | BOOLEAN | `true` tras verificación correcta |
| `expira_en` | TIMESTAMP | `created_at + 10 minutos` |
| `creado_en` | TIMESTAMP | |

---

#### Lo que hace el FRONTEND

1. Al montar el componente `/login`: importar FingerprintJS (`@fingerprintjs/fingerprintjs`), llamar a `FingerprintJS.load()` y obtener `visitorId`. Guardarlo en un signal local
2. Al hacer submit del formulario de login: enviar `POST /auth/login` con `{ email, password, visitorId }`
3. **Si la respuesta tiene `status === "MFA_REQUIRED"`:**
   - Guardar `mfaToken` en un signal local
   - Ocultar el formulario de login y mostrar la pantalla de código OTP
   - Mostrar al usuario: *"Hemos enviado un código de verificación a tu correo. Válido 10 minutos."*
4. Al hacer submit del código: enviar `POST /auth/mfa/verificar` con `{ mfaToken, codigo, visitorId }`
5. **Si la respuesta es `ok: true`:** redirigir al dashboard igual que en un login normal
6. **Si la respuesta es `CODIGO_INVALIDO`:** mostrar *"Código incorrecto. Te quedan X intentos."*
7. **Si la respuesta es `MFA_EXPIRADO`:** volver al formulario de login mostrando *"El código ha caducado. Inicia sesión de nuevo."*

> **Instalación FingerprintJS:** `npm install @fingerprintjs/fingerprintjs`. La versión open source es gratuita y suficiente para identificar dispositivos.

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

### 3.7 Solicitud de Ampliación de Formación

Un entrenador con una sola titulación puede solicitar activar el módulo adicional aportando la nueva documentación. El frontend muestra esta opción en MI PERFIL cuando el entrenador tiene exactamente una de las dos titulaciones.

**`POST /api/v1/entrenador/ampliar-formacion`**

Requiere cookie `access_token` válida con `rol === 'ENTRENADOR'` y `estado === 'ACTIVO'`.

Recibe `multipart/form-data`:

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `modulo` | `enum` | ✅ | `ENTRENAMIENTO` o `NUTRICION` — el módulo que se quiere habilitar |
| `titulacion` | `string` | ✅ | Valor enum de la titulación obtenida (ver valores válidos en 3.1) |
| `documentos` | `File[]` | ✅ | 1–10 archivos PDF/JPG/PNG, max 10 MB c/u |

**Validación en servidor:**
- Si `modulo === 'NUTRICION'` → verificar que `titulo_nutricion === false` (400 si ya tiene módulo)
- Si `modulo === 'ENTRENAMIENTO'` → verificar que `titulo_entrenamiento === false` (400 si ya tiene módulo)
- Si ya existe una solicitud pendiente para ese módulo → 409 `SOLICITUD_PENDIENTE`
- `titulacion` debe pertenecer al conjunto válido del módulo indicado (400 si no coincide)

**Respuesta 200:**
```json
{
  "ok": true,
  "message": "Solicitud de ampliación recibida. Revisaremos tu documentación en un plazo máximo de 48h.",
  "data": { "solicitudAmpliacionPendiente": "NUTRICION" }
}
```

**Respuesta 409** (solicitud ya en curso para ese módulo):
```json
{ "ok": false, "error": "SOLICITUD_PENDIENTE", "message": "Ya existe una solicitud de ampliación pendiente para ese módulo." }
```

**Lógica en servidor:**
- Subir los documentos a S3 (`entrenadores/<uuid>/ampliacion/<modulo>/…`).
- Guardar en `documentos_entrenador` con `status = 'pending'`.
- Actualizar el campo `solicitud_ampliacion_pendiente` de la tabla `entrenadores` con el valor del `modulo`.
- Cuando el admin apruebe la solicitud: activar la titulación correspondiente, poner `solicitud_ampliacion_pendiente = NULL` y actualizar `titulo_entrenamiento` / `titulo_nutricion`.
- Enviar email al entrenador confirmando la recepción.

> `solicitudAmpliacionPendiente` se devuelve en `GET /api/v1/entrenador/perfil` para que el frontend muestre el estado "EN REVISIÓN" y deshabilite el botón de solicitud mientras está pendiente.

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
2. Setear `entrenadores.titulo_entrenamiento = (titulacion_entrenamiento IS NOT NULL)` y `titulo_nutricion = (titulacion_nutricion IS NOT NULL)` — estos booleanos son los que el frontend lee en `/auth/me` para determinar a qué dashboard redirigir al entrenador en el próximo login
3. Cambiar `documentos_entrenador.status` → `verified`, setear `reviewed_at = NOW()`
4. Enviar email de aprobación al entrenador

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

**`GET /api/v1/admin/ampliaciones?status=pending`**

Devuelve la lista de solicitudes de ampliación de formación pendientes de revisión (entrenadores que ya tienen cuenta activa y quieren habilitar el módulo adicional).

```json
{
  "ok": true,
  "data": [
    {
      "id": "uuid-entrenador",
      "nombre": "Carlos Martínez",
      "correo": "carlos@example.com",
      "moduloSolicitado": "NUTRICION",
      "titulacionSolicitada": "GRADO_NUTRICION_DIETETICA",
      "titulacionSolicitadaLabel": "Grado en Nutrición Humana y Dietética",
      "modulosActuales": {
        "entrenamiento": true,
        "nutricion": false
      },
      "solicitadaEn": "2026-04-20T10:00:00Z",
      "documentos": [
        {
          "id": "uuid-doc",
          "nombre_archivo": "Titulo_Nutricion.pdf",
          "url_firmada": "https://s3.../...",
          "uploaded_at": "2026-04-20T10:00:00Z"
        }
      ]
    }
  ]
}
```

> Filtrar por `documentos_entrenador.status = 'pending'` JOIN con `entrenadores.solicitud_ampliacion_pendiente IS NOT NULL`.

---

**`POST /api/v1/admin/ampliaciones/:entrenadorId/aprobar`**

Aprueba la solicitud de ampliación y activa el nuevo módulo en la cuenta del entrenador.

```json
// Request body — vacío
{}

// Response 200
{ "ok": true, "message": "Ampliación aprobada. El módulo ha sido activado en la cuenta del entrenador." }
```

**Lógica:**
1. Leer `entrenadores.solicitud_ampliacion_pendiente` para saber qué módulo aprobar.
2. Si `moduloSolicitado === 'NUTRICION'` → setear `titulacion_nutricion` al valor enviado en la solicitud y `titulo_nutricion = true`.
3. Si `moduloSolicitado === 'ENTRENAMIENTO'` → setear `titulacion_entrenamiento` al valor enviado y `titulo_entrenamiento = true`.
4. Poner `solicitud_ampliacion_pendiente = NULL`.
5. Cambiar los `documentos_entrenador` relacionados (los de esta solicitud) a `status = 'verified'`, setear `reviewed_at`.
6. Enviar email al entrenador notificando que su nuevo módulo está activo.

> Para identificar qué documentos pertenecen a esta solicitud: los documentos subidos tras la activación de la cuenta (después de `usuarios.created_at`) con `status = 'pending'` son de la solicitud de ampliación.

---

**`POST /api/v1/admin/ampliaciones/:entrenadorId/rechazar`**

Rechaza la solicitud de ampliación. El entrenador puede volver a solicitarla.

```json
// Request body
{ "motivo": "El título aportado no es válido para este módulo." }

// Response 200
{ "ok": true, "message": "Solicitud rechazada. El entrenador ha sido notificado." }
```

**Lógica:**
1. Poner `solicitud_ampliacion_pendiente = NULL` en `entrenadores`.
2. Cambiar los `documentos_entrenador` de esta solicitud a `status = 'rejected'`, guardar `rejection_reason` y setear `reviewed_at`.
3. Enviar email al entrenador con el motivo del rechazo (para que pueda aportar documentación correcta).

> Al poner `solicitud_ampliacion_pendiente = NULL`, el frontend vuelve a mostrar el botón "SOLICITAR AMPLIACIÓN" permitiendo al entrenador reintentar con documentación correcta.

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
    "descripcion": "Especialista en rendimiento deportivo y fuerza.",
    "masters": ["Máster en Alto Rendimiento Deportivo"],
    "codigoInvitacion": "GRIT-A1B2C3",
    "estado": "ACTIVO",
    "fotoUrl": "https://cdn.grit.app/entrenadores/uuid/perfil.jpg",
    "solicitudAmpliacionPendiente": null
  }
}
```

> **Nombre del campo `descripcion`:** el frontend usa `descripcion` (no `sobreMi`) para este campo. El backend debe devolverlo como `descripcion` en la respuesta JSON aunque la columna en BBDD se llame `sobre_mi`.

**Campos del objeto `data`:**

| Campo | Tipo | Notas |
|---|---|---|
| `descripcion` | `string \| null` | Texto libre de presentación. Columna `sobre_mi` en BBDD |
| `masters` | `string[]` | Lista de posgrados o títulos adicionales. `[]` si no tiene |
| `codigoInvitacion` | `string` | Código único generado al crear la cuenta. El atleta lo introduce al registrarse para vincularse automáticamente |
| `fotoUrl` | `string \| null` | URL pública de la foto de perfil en S3. `null` si no ha subido foto |
| `solicitudAmpliacionPendiente` | `'ENTRENAMIENTO' \| 'NUTRICION' \| null` | Si hay una solicitud de ampliación en revisión, indica el módulo solicitado. `null` si no hay ninguna |

---

**`POST /api/v1/entrenador/invitar`**

Envía un email de invitación al correo indicado. El email incluye el código de invitación del entrenador y un enlace al formulario de registro de atleta.

> Requiere cookie `access_token` con `rol === 'ENTRENADOR'` y `estado === 'ACTIVO'`.

```json
// Request body
{
  "email": "atleta@ejemplo.com"
}

// Response 200
{ "ok": true, "message": "Invitación enviada correctamente." }
```

**Response 400** si el email no tiene formato válido:
```json
{ "ok": false, "error": "EMAIL_INVALIDO" }
```

**Notas:**
- El email enviado debe incluir el `codigoInvitacion` del entrenador y un enlace a `/registro/atleta`.
- No requiere que el destinatario tenga cuenta previa.
- El backend no verifica si el email ya está registrado (solo envía el correo).

---

**`GET /api/v1/entrenador/check-codigo/:codigo`**

Verifica si un código de colegiado ya está registrado. Usado en el formulario de registro del entrenador para validación en tiempo real.

> Este endpoint **no requiere autenticación** (se llama antes del login).

```json
// Response 200
{ "ok": true, "data": { "existe": true } }
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
      "tienePlanActivo": true,
      "alergias": ["Frutos secos", "Marisco"],
      "intolerancias": ["Lactosa"]
    }
  ]
}
```

> `tienePlanActivo` es `true` si el atleta tiene al menos un plan de entrenamiento O nutrición activo creado por este entrenador.
>
> `alergias` e `intolerancias` son arrays de strings libres que el atleta declara en su perfil. Pueden ser arrays vacíos. El entrenador/nutricionista los ve en los módulos de entrenamiento y nutrición como banner de aviso al diseñar planes y dietas.

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
          "notas": "Toma el desayuno siempre antes de las 9h.",
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

> **Campo `notas` en cada comida:** texto libre opcional que el nutricionista escribe al crear el plan. El atleta lo ve al expandir la comida en su dashboard (marcado como "NOTA DE TU NUTRICIONISTA"). Puede ser `null` si el nutricionista no añadió nota.

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
      "notas": "Toma el desayuno siempre antes de las 9h.",
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

**`PUT /api/v1/nutricion/planes/:id/activar`**

Marca un plan de nutrición como activo para el atleta. Desactiva automáticamente cualquier otro plan activo del mismo atleta creado por este entrenador.

```json
// Request body: vacío

// Response 200
{ "ok": true }
```

**Response 403** si el plan no pertenece al entrenador autenticado:
```json
{ "ok": false, "error": "ACCESO_DENEGADO" }
```

---

**`POST /api/v1/entrenador/atletas/:atletaId/notas`**

El nutricionista escribe una nota de seguimiento para el atleta (consejo, observación, recordatorio). El atleta las lee en la sección DIETA de su dashboard bajo la etiqueta "NOTAS DE TU NUTRICIONISTA".

> Requiere `titulo_nutricion === true` y asignación activa de tipo `NUTRICION` con el atleta.

```json
// Request body
{ "texto": "Recuerda tomar el batido proteico dentro de los 30 minutos post-entreno." }

// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-nota",
    "texto": "Recuerda tomar el batido proteico dentro de los 30 minutos post-entreno.",
    "fecha": "2026-04-22"
  }
}
```

**Validaciones:**
- `texto`: no puede estar vacío, máximo 1000 caracteres.
- Guardar en tabla `notas_nutricionista` con `entrenador_id` y `atleta_id`.

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

**`PUT /api/v1/entrenamiento/rutinas/:id/activar`**

Marca una rutina como activa para el atleta. Desactiva automáticamente cualquier otra rutina activa del mismo atleta creada por este entrenador.

```json
// Request body: vacío

// Response 200
{ "ok": true }
```

**Response 403** si la rutina no pertenece al entrenador autenticado:
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
    "objetivo": "RENDIMIENTO",
    "fotoUrl": "https://cdn.grit.app/atletas/uuid/perfil.jpg"
  }
}
```

> `objetivo` puede ser `null` si el atleta contrató solo nutrición.
> `fotoUrl` es `null` si el atleta no ha subido foto de perfil.

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
        "nombre": "Pecho y Espalda",
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
        "nombre": "Piernas",
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

**Campos de cada sesión:**

| Campo | Tipo | Notas |
|---|---|---|
| `nombre` | `string` | Nombre libre que el entrenador asignó a la sesión (ej. "Piernas", "Pecho y Espalda", "Fuerza"). **No es un día de la semana** — el entrenador lo define libremente al crear la rutina |

**Campos de cada ejercicio:**

| Campo | Tipo | Notas |
|---|---|---|
| `nombre` | `string` | Nombre del ejercicio |
| `series` | `number` | Número de series |
| `reps` | `string` | Puede ser "8-10", "Máx", "Al fallo", "30s" |
| `descanso` | `string \| null` | Ej. "90s", "2 min". Opcional |
| `notas` | `string \| null` | Nota breve del entrenador sobre este ejercicio. El atleta la ve al pulsar el ejercicio en su dashboard |

> El campo `notas` de cada ejercicio es la nota que el entrenador dejó al diseñar la rutina. El frontend del atleta la muestra como panel expandible bajo cada ejercicio (no hay hilo de conversación por ejercicio en el frontend actual — ver nota en sección 3.14.6).

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
        "notas": "Toma el desayuno siempre antes de las 9h.",
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

**Notas sobre el campo `notas` de cada comida:**
- `notas` es `string | null`. El nutricionista puede añadir una nota por comida al crear el plan.
- El frontend del atleta la muestra al expandir la comida con la etiqueta "NOTA DE TU NUTRICIONISTA".

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

**`GET /api/v1/entrenador/atletas/:atletaId/peso/historial`**

Devuelve el historial de check-ins de peso de un atleta visto desde el entrenador. Ordenados por fecha ASC. El entrenador usa esta respuesta para renderizar la gráfica de evolución en el panel de seguimiento de su dashboard.

```json
{
  "ok": true,
  "data": [
    { "id": "uuid-w1", "fecha": "2026-02-03", "pesoKg": 85.2, "solicitadoPor": "ENTRENADOR" },
    { "id": "uuid-w2", "fecha": "2026-02-17", "pesoKg": 84.0, "solicitadoPor": "NUTRICIONISTA" }
  ]
}
```

> Solo devuelve registros del atleta indicado. El entrenador debe tener una asignación activa con ese atleta (validación en servidor).

---

**`GET /api/v1/entrenador/atletas/:atletaId/peso/pendiente`**

Indica si el atleta tiene una solicitud de check-in de peso **pendiente de responder** (creada por este entrenador y aún no registrada por el atleta). El entrenador usa esto para mostrar u ocultar el botón "SOLICITAR PESO" en el panel de seguimiento.

```json
// Response 200
{ "ok": true, "data": { "pendiente": true } }
```

> Internamente: buscar en `checkins_peso_solicitudes` por `atleta_id` y `estado = 'PENDIENTE'`. Devolver `pendiente: true` si existe al menos una.

---

#### 3.14.6 Hilo de Ejercicio

> **Estado frontend:** El hilo de conversación por ejercicio **no está activo en el dashboard del atleta** en la versión actual. Al pulsar un ejercicio, el atleta solo ve la nota del entrenador (`notas`). El chat se centraliza en el chat general (sección 3.14.7). Los endpoints de esta sección están diseñados para implementación futura o para que el entrenador los consulte desde su dashboard.

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

Chat de texto y multimedia entre el atleta y su entrenador/nutricionista. Hay un chat por cada relación de servicio activa:
- Si `servicio = ENTRENAMIENTO` → un único chat con el entrenador
- Si `servicio = NUTRICION` → un único chat con el nutricionista
- Si `servicio = AMBOS` → dos chats separados (uno con cada profesional)

El frontend identifica el chat con `tipo: 'entrenador' | 'nutricionista'`. El backend resuelve eso al entrenador real asignado a través de la tabla `asignaciones`.

El atleta puede adjuntar **fotos y vídeos** a sus mensajes (ej. cuando el entrenador lo solicita para revisar técnica o progreso). Los archivos se suben a S3 antes de enviar el mensaje.

---

**`GET /api/v1/atleta/chat/:tipo`**

`:tipo` es `entrenador` o `nutricionista`.

Devuelve el historial completo del chat, ordenado por `fecha ASC`. Cuando un mensaje tiene adjunto, incluye el objeto `adjunto` con una pre-signed URL de S3 (expiración 15 min).

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
        "autor": "Carlos López",
        "adjunto": null
      },
      {
        "id": "uuid-msg2",
        "texto": "Mira mi técnica de sentadilla",
        "fecha": "2026-04-21",
        "esAtleta": true,
        "autor": "Carlos Ruiz",
        "adjunto": {
          "url": "https://s3.amazonaws.com/grit-documentos/chat/uuid-media.mp4?X-Amz-Expires=900&...",
          "tipo": "video",
          "nombre": "sentadilla.mp4"
        }
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

**`POST /api/v1/atleta/chat/:tipo/archivo`**

El atleta sube una foto o vídeo antes de enviarlo como mensaje. Requiere S3.

Request: `multipart/form-data`

| Campo | Tipo | Requerido | Notas |
|---|---|---|---|
| `archivo` | `File` | ✅ | Imagen (JPG/PNG/WEBP) o vídeo (MP4/MOV/WEBM). Max 100 MB |

```json
// Response 201
{
  "ok": true,
  "data": {
    "url": "https://s3.amazonaws.com/grit-documentos/chat/uuid-media.jpg?X-Amz-Expires=900&...",
    "tipo": "foto",
    "nombre": "progreso.jpg"
  }
}
```

> El campo `tipo` se infiere del MIME type del archivo: `image/*` → `"foto"`, `video/*` → `"video"`.
> La URL devuelta es una pre-signed URL de S3 con expiración corta. El frontend la usa inmediatamente para mostrar la preview y luego la envía en el campo `adjunto` del mensaje.

---

**`POST /api/v1/atleta/chat/:tipo/mensaje`**

El atleta envía un mensaje al chat. Puede incluir texto, adjunto, o ambos. Al menos uno de los dos es obligatorio.

Request: `multipart/form-data`

| Campo | Tipo | Requerido | Notas |
|---|---|---|---|
| `texto` | `string` | Condicional | Obligatorio si no hay `archivo` |
| `archivo` | `File` | Condicional | Obligatorio si no hay `texto`. Imagen o vídeo, max 100 MB |

> El backend gestiona la subida a S3 directamente. El frontend puede enviar texto + archivo en una sola petición, o solo texto (sin archivo) manteniendo compatibilidad con el flujo de texto puro.

```json
// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-msg",
    "texto": "Mira mi técnica de sentadilla",
    "fecha": "2026-04-21",
    "esAtleta": true,
    "autor": "Carlos Ruiz",
    "adjunto": {
      "url": "https://s3.amazonaws.com/grit-documentos/chat/uuid-media.mp4?X-Amz-Expires=900&...",
      "tipo": "video",
      "nombre": "sentadilla.mp4"
    }
  }
}
```

> Si no hay archivo, `adjunto` es `null` en la respuesta.

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

**`DELETE /api/v1/atleta/cuenta`**

El atleta solicita la eliminación permanente de su cuenta. El frontend exige que el usuario escriba literalmente `"ELIMINAR"` antes de habilitar el botón.

```json
// Request body — vacío (la identidad se verifica por la cookie)
{}

// Response 200
{ "ok": true, "message": "Cuenta eliminada correctamente." }
```

**Lógica de borrado:**
- Marcar el usuario como `ELIMINADO` (soft delete: añadir columna `eliminado_en TIMESTAMP NULL` en `usuarios`) o borrado físico según política de datos.
- Eliminar o anonimizar: `atletas`, `asignaciones`, `checkins_peso`, `mensajes_chat`, `media_hilo`, `mensajes_hilo`.
- Invalidar la cookie `access_token` (responder con `Set-Cookie: access_token=; Max-Age=0`).
- No eliminar datos de planes/rutinas creados por el entrenador — esos pertenecen al entrenador.

---

**`POST /api/v1/atleta/foto`**

El atleta sube o reemplaza su foto de perfil. La imagen se almacena en S3.

```
// Request: multipart/form-data
foto: <archivo imagen>   // campo "foto", image/jpeg | image/png | image/webp, máx. 5 MB

// Response 200
{ "url": "https://cdn.grit.app/atletas/<uuid>/perfil.jpg" }
```

**Validaciones:**
- MIME type: `image/jpeg`, `image/png`, `image/webp` únicamente.
- Tamaño máximo: 5 MB.
- Sobrescribir el objeto S3 anterior si ya existía (`foto_url` en `atletas`).
- Actualizar columna `foto_url` en la tabla `atletas`.

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
    "sobreMi": "Especialista en fuerza e hipertrofia con 8 años de experiencia.",
    "anosExperiencia": 8,
    "masters": ["Máster en Alto Rendimiento Deportivo"]
  },
  {
    "id": "uuid-prof-2",
    "nombre": "María González",
    "titulacion": "Dietista-Nutricionista (Graduada en Nutrición Humana y Dietética)",
    "rol": "NUTRICIONISTA",
    "sobreMi": "Especializada en nutrición deportiva y pérdida de peso.",
    "anosExperiencia": 5,
    "masters": []
  }
]
```

**Campos del response:**

| Campo | Tipo | Notas |
|---|---|---|
| `titulacion` | `string` | Label legible de la titulación principal (no el enum) |
| `sobreMi` | `string \| null` | Texto de presentación del profesional |
| `anosExperiencia` | `number \| null` | Años de experiencia declarados en su perfil |
| `masters` | `string[]` | Posgrados o títulos adicionales. Array vacío si no tiene |

**Lógica de construcción:**
- Consultar `asignaciones` filtrando por `atleta_id` y `estado = ACTIVO`
- Para cada asignación, hacer JOIN con `entrenadores` → `usuarios` para obtener nombre, titulación, `sobre_mi`, `experiencia_anos` y `masters`
- Mapear `servicio = ENTRENAMIENTO` → `rol = ENTRENADOR`, `servicio = NUTRICION` → `rol = NUTRICIONISTA`
- Si el atleta tiene `servicio = AMBOS` con el mismo profesional, devolver dos entradas con el mismo `id` pero distinto `rol`

**Campos de `entrenadores` necesarios para esta respuesta:**
- `titulacion_entrenamiento` / `titulacion_nutricion` (ya existen; el backend construye el string legible)
- `sobre_mi` → devolver como `sobreMi` en esta respuesta (distinto nombre que en `GET /entrenador/perfil` donde se llama `descripcion`, pero ambos mapean a la misma columna)
- `experiencia_anos` (ya existe como `experiencia_anos`)
- `masters` — array de strings, nuevo campo (ver sección 4)

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
- El nutricionista puede responder desde el dashboard del entrenador usando `POST /api/v1/entrenador/atletas/:atletaId/nutricion/hilo/mensaje` (ver sección 3.18)

---

#### 3.14.11 Conectar con Entrenador por Código

El atleta puede vincularse a un entrenador desde la pestaña **MI PERFIL** de su dashboard, introduciendo el código de invitación del entrenador (formato `GRIT-XXXX-XXXX`).

**`POST /api/v1/atleta/conectar`**

```json
// Request body
{ "codigo": "GRIT-X7K2-9PQR" }

// Response 200
{ "ok": true, "message": "Vinculado correctamente con el entrenador." }
```

**Respuestas de error:**
```json
// 400 — código inválido o ya utilizado
{ "ok": false, "error": "CODIGO_INVALIDO", "message": "Código no válido o ya utilizado." }

// 409 — el atleta ya tiene un entrenador asignado para ese servicio
{ "ok": false, "error": "YA_VINCULADO", "message": "Ya tienes un profesional asignado para este servicio." }
```

**Lógica:**
- Buscar en `entrenadores` por `codigo_invitacion = codigo` y `estado = 'ACTIVO'`.
- Determinar el servicio a asignar según las titulaciones del entrenador y el `servicio` contratado por el atleta.
- Crear registro en `asignaciones` con `estado = 'ACTIVO'`.
- El código de invitación NO se invalida tras el primer uso — puede ser reutilizado por múltiples atletas.

---

### 3.17 Chat desde el Dashboard del Entrenador

> Requieren cookie `access_token` con `rol === 'ENTRENADOR'` y asignación activa con el atleta.

El entrenador/nutricionista puede leer y responder los chats de sus atletas desde su dashboard.

**`GET /api/v1/entrenador/atletas/:atletaId/chat`**

Devuelve el historial del chat entre el entrenador autenticado y el atleta indicado. El `servicio` se resuelve por la asignación activa. Los mensajes con adjunto incluyen la URL pre-signed de S3.

```json
{
  "ok": true,
  "data": {
    "atletaNombre": "Carlos Ruiz",
    "mensajes": [
      {
        "id": "uuid-msg",
        "texto": "¡Hola! ¿Cómo llevas la semana?",
        "fecha": "2026-04-10",
        "esAtleta": false,
        "autor": "Carlos López",
        "adjunto": null
      },
      {
        "id": "uuid-msg2",
        "texto": "Mira mi técnica de sentadilla",
        "fecha": "2026-04-21",
        "esAtleta": true,
        "autor": "Carlos Ruiz",
        "adjunto": {
          "url": "https://s3.amazonaws.com/grit-documentos/chat/uuid-media.mp4?X-Amz-Expires=900&...",
          "tipo": "video",
          "nombre": "sentadilla.mp4"
        }
      }
    ]
  }
}
```

---

**`POST /api/v1/entrenador/atletas/:atletaId/chat/mensaje`**

El entrenador envía un mensaje al chat con el atleta. Request: `multipart/form-data`.

| Campo | Tipo | Requerido | Notas |
|---|---|---|---|
| `texto` | `string` | Condicional | Obligatorio si no hay `archivo` |
| `archivo` | `File` | Condicional | Imagen o vídeo, max 100 MB |

```json
// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-msg",
    "texto": "Muy bien la sesión de hoy, sigue así.",
    "fecha": "2026-04-21",
    "esAtleta": false,
    "autor": "Carlos López",
    "adjunto": null
  }
}
```

---

**`POST /api/v1/entrenador/atletas/:atletaId/nutricion/hilo/mensaje`**

El nutricionista responde en el hilo de una comida concreta del atleta.

```json
// Request body
{
  "comidaNombre": "Desayuno",
  "texto": "Sí, puedes sustituir la leche por bebida de avena sin azúcares añadidos."
}

// Response 201
{
  "ok": true,
  "data": {
    "id": "uuid-msg",
    "texto": "Sí, puedes sustituir la leche por bebida de avena sin azúcares añadidos.",
    "fecha": "2026-04-20",
    "esAtleta": false,
    "autor": "María González"
  }
}
```

---

### 3.18 Historial de Ejercicios (Entrenador)

> Requieren cookie `access_token` con `rol === 'ENTRENADOR'` y `titulo_entrenamiento === true`.
>
> El frontend del entrenador guarda en `localStorage` los últimos ejercicios usados al crear rutinas (historial de búsqueda + biblioteca fija de ~45 ejercicios comunes). **Si se quiere persistencia entre dispositivos y sesiones**, implementar los siguientes endpoints.

**`GET /api/v1/entrenamiento/ejercicios-recientes`**

Devuelve los ejercicios usados más recientemente por el entrenador autenticado. Máximo 30 resultados, ordenados por `usado_en DESC`.

```json
{
  "ok": true,
  "data": ["Sentadilla", "Press de banca", "Remo con barra"]
}
```

---

**`POST /api/v1/entrenamiento/ejercicios-recientes`**

Registra el uso de un ejercicio. Si ya existe el nombre, actualiza `usado_en`. Si hay más de 30 registros para este entrenador, elimina el más antiguo.

```json
// Request body
{ "nombre": "Sentadilla" }

// Response 200
{ "ok": true }
```

> **Nota:** La biblioteca fija de ejercicios (pecho, espalda, piernas, etc.) **vive en el frontend** y no requiere endpoint. El backend solo persiste el historial de ejercicios usados por el entrenador. La combinación de historial (del backend) + biblioteca (del frontend) es lo que el entrenador ve en el desplegable de sugerencias al crear una rutina.

**BBDD — tabla `ejercicios_recientes`:**

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | |
| `nombre` | VARCHAR(255) | Nombre del ejercicio |
| `usado_en` | TIMESTAMP | Se actualiza cada vez que se usa |

**Constraint único:** `(entrenador_id, nombre)` — evita duplicados. El backend hace `UPSERT` actualizando `usado_en`.

---

### 3.19 Ajustes de Cuenta (Entrenador)

> Requieren cookie `access_token` válida con `rol === 'ENTRENADOR'`.

**`PUT /api/v1/entrenador/password`**

El entrenador cambia su contraseña. Se requiere la contraseña actual para verificar identidad.

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

**`DELETE /api/v1/entrenador/cuenta`**

El entrenador solicita la eliminación permanente de su cuenta. El frontend exige que el usuario escriba literalmente `"ELIMINAR"` antes de habilitar el botón.

```json
// Request body — vacío (la identidad se verifica por la cookie)
{}

// Response 200
{ "ok": true, "message": "Cuenta eliminada correctamente." }
```

**Lógica de borrado:**
- Soft delete recomendado: añadir `eliminado_en TIMESTAMP NULL` en `usuarios` y marcar la fecha.
- Cancelar todas las `asignaciones` activas (`estado → CANCELADO`). Los atletas vinculados quedan sin profesional asignado para ese servicio.
- No eliminar planes de nutrición ni rutinas ya creados — los atletas conservan acceso a sus planes activos hasta que expiren.
- Invalidar la cookie `access_token` (responder con `Set-Cookie: access_token=; Max-Age=0`).

---

**`POST /api/v1/entrenador/foto`**

El entrenador sube o reemplaza su foto de perfil. La imagen se almacena en S3.

```
// Request: multipart/form-data
foto: <archivo imagen>   // campo "foto", image/jpeg | image/png | image/webp, máx. 5 MB

// Response 200
{ "url": "https://cdn.grit.app/entrenadores/<uuid>/perfil.jpg" }
```

**Validaciones:**
- MIME type: `image/jpeg`, `image/png`, `image/webp` únicamente.
- Tamaño máximo: 5 MB.
- Sobrescribir el objeto S3 anterior si ya existía (`foto_url` en `entrenadores`).
- Actualizar columna `foto_url` en la tabla `entrenadores`.

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
| `titulo_entrenamiento` | BOOLEAN | Se fija a `true/false` al aprobar la cuenta (ver sección 3.9). `true` → acceso al módulo de entrenamiento |
| `titulo_nutricion` | BOOLEAN | Se fija a `true/false` al aprobar la cuenta (ver sección 3.9). `true` → acceso al módulo de nutrición |
| `experiencia_anos` | SMALLINT NULLABLE | |
| `sobre_mi` | TEXT NULLABLE | Texto libre de presentación. Se devuelve como `descripcion` en `GET /entrenador/perfil` y como `sobreMi` en `GET /atleta/profesionales` |
| `masters` | TEXT[] NULLABLE | Array de strings con posgrados o títulos adicionales. Devuelve `[]` si es NULL |
| `codigo_invitacion` | VARCHAR(20) UNIQUE NOT NULL | Código único generado al crear la cuenta. El atleta lo introduce al registrarse para vincularse automáticamente |
| `foto_url` | VARCHAR(500) NULLABLE | URL pública S3 de la foto de perfil. `NULL` si no ha subido foto |
| `solicitud_ampliacion_pendiente` | ENUM NULLABLE | `ENTRENAMIENTO`, `NUTRICION`. `NULL` si no hay solicitud activa. Se pone a `NULL` cuando el admin aprueba o rechaza |

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
| `alergias` | TEXT[] NULLABLE | Array de strings con alergias declaradas por el atleta (ej. `["Frutos secos", "Marisco"]`). Devuelve `[]` si es NULL |
| `intolerancias` | TEXT[] NULLABLE | Array de strings con intolerancias declaradas por el atleta (ej. `["Lactosa", "Gluten"]`). Devuelve `[]` si es NULL |
| `foto_url` | VARCHAR(500) NULLABLE | URL pública S3 de la foto de perfil. `NULL` si no ha subido foto |

> El entrenador/nutricionista puede ver `alergias` e `intolerancias` en la respuesta de `GET /api/v1/entrenador/atletas` para tenerlas en cuenta al diseñar planes. El frontend las muestra como banner de aviso en los módulos de entrenamiento y nutrición.

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
| `notas` | TEXT NULLABLE | Nota libre del nutricionista para esta comida. Visible al atleta al expandir la comida |
| `orden` | SMALLINT | Para mantener el orden de las comidas del día |

### Tabla `alimentos_en_comida`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `comida_id` | UUID FK → comidas | |
| `alimento_id` | UUID FK → alimentos | Referencia al alimento de la BD propia |
| `nombre` | VARCHAR(255) | Snapshot del nombre en el momento de guardar el plan |
| `marca` | VARCHAR(255) NULLABLE | Snapshot |
| `kcal_por_100g` | DECIMAL(7,2) | Snapshot — los macros que el nutricionista prescribió |
| `proteinas_por_100g` | DECIMAL(7,2) | Snapshot |
| `carbs_por_100g` | DECIMAL(7,2) | Snapshot |
| `grasas_por_100g` | DECIMAL(7,2) | Snapshot |
| `cantidad_g` | DECIMAL(7,2) | Cantidad en gramos para este plan |

> Los macros se guardan como snapshot para que el plan no cambie si en el futuro se corrige un alimento en la tabla `alimentos`. `alimento_id` permite trazabilidad pero no afecta a los cálculos del plan.

### Tabla `alimentos_recientes`

Historial de alimentos usados por el entrenador en cada tipo de comida. Máximo 8 registros por combinación `(usuario_id, nombre_comida)`.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `usuario_id` | UUID FK → usuarios | Entrenador que lo usó |
| `nombre_comida` | VARCHAR(100) | "Desayuno", "Almuerzo", etc. |
| `alimento_id` | UUID FK → alimentos | |
| `usado_en` | TIMESTAMP | Se actualiza cada vez que se usa |

> **Constraint único:** `(usuario_id, nombre_comida, alimento_id)` — evita duplicados. El backend debe hacer `UPSERT` actualizando `usado_en` si ya existe. Al devolver los recientes, hacer JOIN con `alimentos` para obtener nombre, marca y macros.

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
| `texto` | TEXT NULLABLE | Puede ser nulo si el mensaje solo contiene adjunto |
| `es_atleta` | BOOLEAN | `true` si lo envió el atleta |
| `autor` | VARCHAR(255) | Nombre del autor (snapshot) |
| `adjunto_url_s3` | TEXT NULLABLE | Clave S3 del archivo adjunto (foto/vídeo). `NULL` si no hay adjunto |
| `adjunto_tipo` | ENUM NULLABLE | `foto`, `video`. `NULL` si no hay adjunto |
| `adjunto_nombre` | VARCHAR(255) NULLABLE | Nombre original del archivo. `NULL` si no hay adjunto |
| `enviado_en` | TIMESTAMP | |
| `fecha` | DATE | Derivada de `enviado_en` |

> El `tipo` ('entrenador' o 'nutricionista') que envía el frontend se resuelve a un `asignacion_id` buscando en `asignaciones` por `(atleta_id, servicio)` donde `servicio = 'ENTRENAMIENTO'` para 'entrenador' y `servicio = 'NUTRICION'` para 'nutricionista'.
> `adjunto_url_s3` almacena la **clave** S3 (ej. `chat/uuid.mp4`), no la URL completa. Al devolver el mensaje al frontend, el backend genera una **pre-signed URL** temporal (15 min) a partir de la clave.

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
| `nombre` | VARCHAR(100) | Texto libre definido por el entrenador (ej. "Piernas", "Pecho y Espalda", "Fuerza"). **No es un día de la semana** — el entrenador lo nombra como quiera |
| `orden` | SMALLINT | |

> El campo `nombre` se devuelve tal cual en los endpoints de atleta (`GET /api/v1/atleta/entrenamiento/plan-activo`) y de entrenador (`GET /api/v1/entrenamiento/rutinas`). El frontend lo muestra como etiqueta del tab de cada sesión.

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
| `POST /entrenador/invitar` | Destinatario del email | "Te han invitado a unirte a GRIT" |
| `POST /entrenador/ampliar-formacion` | Entrenador | "Solicitud de ampliación recibida — revisaremos tu documentación en 48h" |
| Aprobación de ampliación (`POST /admin/ampliaciones/:id/aprobar`) | Entrenador | "¡Nuevo módulo activado en tu cuenta GRIT!" |
| Rechazo de ampliación (`POST /admin/ampliaciones/:id/rechazar`) | Entrenador | "Actualización sobre tu solicitud de ampliación en GRIT" |

> El email de invitación debe incluir: nombre del entrenador que invita, el código de invitación (para introducirlo en el formulario de registro) y un enlace directo a `/registro/atleta`.
> El email de aprobación de ampliación debe indicar qué módulo se ha activado (Entrenamiento o Nutrición) y que ya puede empezar a usarlo.
> El email de rechazo de ampliación debe incluir el motivo proporcionado por el admin para que el entrenador pueda corregir la documentación y reintentar.

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
   - Incluir desde el inicio: `foto_url` en `atletas` y `entrenadores`; `solicitud_ampliacion_pendiente` en `entrenadores`
3. **Healthcheck** (`GET /api/v1/health`)
4. **Registro de Atleta** + **Login** + **Refresh** + **Logout** + **Me**
   - El bloque de auth completo: cookies HttpOnly, JWT, redirección
   - Incluir `POST /api/v1/atleta/foto` en el mismo bloque — el frontend lo llama inmediatamente tras el registro si el atleta subió foto
5. **Registro de Entrenador** (multipart/form-data, upload a S3, estado `PENDIENTE_REVISION`)
   - Incluir `POST /api/v1/entrenador/foto` en el mismo bloque
6. **Endpoints de administración** (aprobar/rechazar, pre-signed URLs de S3)
7. **Sistema de emails** (registro, aprobación, rechazo)
8. **Middleware de protección por titulación** (secciones 3.8)
9. **Módulo Entrenador** — perfil, lista de atletas e invitación (sección 3.11)
   - El campo `solicitudAmpliacionPendiente` en `GET /perfil` debe devolverse desde el inicio
10. **API de Alimentos** — tabla y búsqueda (sección 11)
    - Migración: `alimentos` + índice `pg_trgm` sobre `nombre`
    - Seeder con los ~50 alimentos base
    - `GET /api/v1/alimentos?q=` y `POST /api/v1/alimentos`
11. **Módulo Nutrición** — planes (sección 3.12)
    - Migraciones: `planes_nutricion`, `comidas`, `alimentos_en_comida`
    - Requiere paso 10 (FK `alimento_id → alimentos`)
12. **Módulo Nutrición** — alimentos recientes (sección 3.16)
    - Migración: `alimentos_recientes`
13. **Módulo Entrenamiento** — rutinas (sección 3.13)
    - Migraciones: `rutinas`, `sesiones_rutina`, `ejercicios_en_sesion`
14. **Módulo Atleta — bloque 1:** perfil, plan activo entrenamiento, plan activo nutrición, notas nutricionista (secciones 3.14.1–3.14.3)
15. **Módulo Atleta — bloque 2:** check-in de peso (solicitud entrenador + registro atleta + historial) (sección 3.14.4–3.14.5)
    - Migraciones: `checkins_peso_solicitudes`, `checkins_peso`
16. **Módulo Atleta — bloque 3:** hilo de ejercicio con media S3 (sección 3.14.6)
    - Migraciones: `hilos_ejercicio`, `media_hilo`, `mensajes_hilo`
17. **Módulo Atleta — bloque 4:** chat general atleta ↔ entrenador/nutricionista con adjuntos S3 (sección 3.14.7)
    - Migración: `mensajes_chat` (con columnas `adjunto_url_s3`, `adjunto_tipo`, `adjunto_nombre`)
    - Requiere S3 configurado (mismo bucket que documentos de entrenador)
18. **Módulo Atleta — bloque 5:** ajustes de cuenta — contraseña + eliminación + foto + conectar con código (secciones 3.14.8 y 3.14.11)
19. **Módulo Atleta — bloque 6:** profesionales asignados + hilo de comida (secciones 3.14.9–3.14.10)
    - Migración: `mensajes_hilo_comida`
20. **Notas nutricionista** (escritura desde el dashboard del entrenador)
    - Migración: `notas_nutricionista`
21. **Ajustes de cuenta del entrenador** — contraseña + eliminación + foto (sección 3.19)
22. **Ampliación de formación** — endpoint unificado `POST /api/v1/entrenador/ampliar-formacion` (sección 3.7)
23. **Rate limiting + seguridad adicional**
23. **Tests de integración** para todos los endpoints

---

## 10. Notas de Integración Frontend ↔ Backend

- **Campo `email` en login y registro:** el frontend envía `email` (no `correo`) en todos los endpoints de auth. La BBDD puede almacenarlo como `correo` pero el campo JSON del body es `email`.
- **`withCredentials: true`:** todas las peticiones HTTP del frontend incluyen esta opción. El backend debe responder con `Access-Control-Allow-Credentials: true` y un `Origin` específico (no `*`) en la cabecera CORS.
- **Datos de ejercicios:** el frontend obtiene los ejercicios directamente del dataset externo `yuhonas/free-exercise-db` (GitHub raw) y de MyMemory para traducciones. No hay endpoint de ejercicios en GRIT. Los datos se guardan embebidos en `ejercicios_en_sesion` como snapshot.
- **Búsqueda de alimentos:** el frontend llama a `GET /api/v1/alimentos?q=<texto>` (sección 11). No hay dependencia de APIs externas. El backend debe tener la tabla `alimentos` con al menos el seeder mínimo antes de conectar el módulo de nutrición.
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
- **Cambio de contraseña del entrenador:** `PUT /api/v1/entrenador/password` — misma lógica que el atleta.
- **Eliminación de cuenta:** tanto `DELETE /api/v1/atleta/cuenta` como `DELETE /api/v1/entrenador/cuenta` requieren solo la cookie válida (el frontend ya exige escribir "ELIMINAR" como confirmación). Se recomienda soft delete para cumplir con RGPD. Ambos endpoints deben invalidar la cookie en la respuesta.
- **Conectar atleta con código:** `POST /api/v1/atleta/conectar` — el frontend llama a este endpoint desde la pestaña MI PERFIL del dashboard del atleta cuando el usuario introduce un código de invitación. El código no se consume (puede usarlo más de un atleta). Si ya existe una asignación activa para ese servicio, devolver `409 YA_VINCULADO`.
- **`cantidadG` en alimentos del plan nutricional:** es un número decimal (`DECIMAL(7,2)`) que representa gramos. El frontend siempre envía un número (por defecto 100). El backend lo almacena en `alimentos_en_comida.cantidad_g` y lo devuelve como número en la respuesta.
- **Código de invitación — generación:** el campo `codigo_invitacion` de `entrenadores` se genera automáticamente al crear la cuenta del entrenador (ej. `GRIT-` + 6 caracteres alfanuméricos aleatorios en mayúsculas). Debe ser único en la tabla. El atleta lo introduce en el formulario de registro (`POST /api/v1/auth/registro/atleta`); si el código es válido, el backend crea la asignación automáticamente tras la activación de la cuenta.
- **Alergias e intolerancias del atleta:** los campos `alergias` e `intolerancias` del atleta se pueden recoger en el formulario de registro (`POST /api/v1/auth/registro/atleta`) como arrays de strings opcionales. El backend los almacena y los devuelve al entrenador en `GET /api/v1/entrenador/atletas`. Si el atleta no los rellena, devolver `[]`.
- **Notas por comida en planes de nutrición:** el campo `notas` de cada comida (tabla `comidas`) es TEXT NULLABLE. Se persiste al crear el plan (`POST /api/v1/nutricion/planes`) y se devuelve tanto al entrenador (`GET /api/v1/nutricion/planes`) como al atleta (`GET /api/v1/atleta/nutricion/plan-activo`).
- **Sesión `nombre` en rutinas:** `sesiones_rutina.nombre` es texto libre definido por el entrenador. No hay validación de formato (no se restringe a días de la semana). El frontend del entrenador deja al entrenador escribir cualquier nombre ("Piernas", "Pecho y Espalda", "Full Body A"). El frontend del atleta muestra los tabs de sesión con este nombre tal cual.
- **Hilo de ejercicio — estado actual del frontend:** el frontend del atleta **no muestra el hilo de conversación por ejercicio**. Al pulsar un ejercicio, solo muestra el campo `notas` del ejercicio como panel expandible. Los endpoints de la sección 3.14.6 están diseñados para uso futuro. El backend puede implementarlos, pero el frontend no los consume actualmente.
- **Historial de ejercicios del entrenador:** actualmente se guarda en `localStorage`. Si se implementan los endpoints de la sección 3.17, el frontend debe migrar a consumirlos. La biblioteca fija de ~45 ejercicios vive solo en el frontend y no requiere endpoint.
- **Adjuntos en el chat general:** el frontend usa un flujo en dos pasos: (1) llama a `POST /atleta/chat/:tipo/archivo` para subir el archivo a S3 y obtener la URL preview; (2) al pulsar ENVIAR, llama a `POST /atleta/chat/:tipo/mensaje` con `multipart/form-data`. Si el mensaje es solo texto (sin archivo), el backend debe seguir aceptando `application/json` con `{ "texto": "..." }` para compatibilidad. Si tiene archivo, la petición es siempre `multipart/form-data`. El campo `adjunto_url_s3` en la tabla almacena la clave S3 (no la URL firmada); las pre-signed URLs se generan en cada `GET` del historial.
- **Hilo de comida — eliminado del frontend del atleta:** el frontend ya no consume los endpoints de hilo de comida (`GET/POST /atleta/nutricion/hilo`). El atleta solo ve el campo `notas` de cada comida (solo lectura, escrito por el nutricionista). El backend puede implementar estos endpoints para uso futuro desde el dashboard del entrenador, pero no son necesarios para el flujo actual del atleta.
- **Foto de perfil — flujo en dos pasos:** la foto es opcional en el registro. El formulario de registro (atleta y entrenador) no la incluye en la petición de registro; en su lugar, si el usuario subió una foto, el frontend realiza una segunda petición inmediatamente después del registro exitoso: `POST /api/v1/atleta/foto` o `POST /api/v1/entrenador/foto`. El backend debe estar listo para recibir esta petición justo tras la creación de la cuenta (la cookie de sesión ya estará activa). La URL devuelta se almacena en `foto_url` de la tabla correspondiente.
- **`descripcion` vs `sobre_mi`:** el frontend usa el nombre de campo `descripcion` en la interfaz `PerfilEntrenador`. El backend debe devolver este campo como `descripcion` en el JSON de `GET /api/v1/entrenador/perfil`, aunque la columna en base de datos se llame `sobre_mi`.
- **`solicitudAmpliacionPendiente` en perfil entrenador:** el campo `solicitudAmpliacionPendiente` de `GET /api/v1/entrenador/perfil` controla qué muestra el frontend en la sección "Ampliar formación" de MI PERFIL. Si es `null`, muestra el botón de solicitud. Si tiene valor (`'ENTRENAMIENTO'` o `'NUTRICION'`), muestra el banner "EN REVISIÓN". El frontend nunca muta este campo directamente — solo lo lee.
- **Recetas eliminadas:** la feature de recetas ha sido eliminada del frontend. No implementar ni las tablas `recetas` / `ingredientes_receta` ni los endpoints asociados.

---

## 11. API de Alimentos

El frontend busca alimentos a través del servicio `alimentos.service.ts`, que llama a la API propia de GRIT. Toda la búsqueda pasa por aquí — no hay dependencia de APIs externas.

> **Seguridad:** Requieren cookie `access_token` válida con `rol === 'ENTRENADOR'` y `titulo_nutricion === true`.

---

**`GET /api/v1/alimentos?q=<texto>`**

Busca alimentos por nombre o marca. Devuelve máximo 15 resultados ordenados por relevancia (coincidencia exacta primero, luego parcial).

```json
// Response 200
{
  "ok": true,
  "data": [
    {
      "codigo": "uuid-alimento",
      "nombre": "Pechuga de pollo",
      "marca": "",
      "kcalPor100g": 165,
      "proteinasPor100g": 31.0,
      "carbsPor100g": 0.0,
      "grasasPor100g": 3.6
    }
  ]
}
```

> El campo `codigo` corresponde a `alimentos.id` (UUID). El frontend lo usa como identificador al guardar un plan o registrar un alimento reciente.

> **Nombres de campos en camelCase:** el frontend consume los campos exactamente como aparecen aquí (`kcalPor100g`, `proteinasPor100g`, `carbsPor100g`, `grasasPor100g`). En Spring Boot, configurar el `ObjectMapper` para serializar en camelCase: `spring.jackson.property-naming-strategy=LOWER_CAMEL_CASE` en `application.properties`, o añadir `@JsonProperty("kcalPor100g")` en el DTO si se prefiere explícito.

Si `q` está vacío o tiene menos de 2 caracteres, devolver `data: []` sin error.

---

**`POST /api/v1/alimentos`**

El nutricionista añade un alimento que no existe en la base de datos. Solo disponible para entrenadores con `titulo_nutricion === true`.

```json
// Request body
{
  "nombre": "Tortilla de patata",
  "marca": "",
  "kcalPor100g": 185,
  "proteinasPor100g": 8.5,
  "carbsPor100g": 16.2,
  "grasasPor100g": 9.8
}
```

```json
// Response 201
{
  "ok": true,
  "data": {
    "codigo": "uuid-nuevo",
    "nombre": "Tortilla de patata",
    "marca": "",
    "kcalPor100g": 185,
    "proteinasPor100g": 8.5,
    "carbsPor100g": 16.2,
    "grasasPor100g": 9.8
  }
}
```

---

### BBDD — tabla `alimentos`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `nombre` | VARCHAR(255) | En español. Índice para búsqueda full-text |
| `marca` | VARCHAR(255) NULLABLE | Null si es alimento genérico (pollo, arroz…) |
| `kcal_por_100g` | DECIMAL(7,2) | |
| `proteinas_por_100g` | DECIMAL(7,2) | |
| `carbs_por_100g` | DECIMAL(7,2) | |
| `grasas_por_100g` | DECIMAL(7,2) | |
| `creado_por` | UUID FK → usuarios NULLABLE | Nutricionista que lo añadió. `null` si viene del seeder |
| `creado_en` | TIMESTAMP | |

> **Índice:** crear índice `GIN` o `pg_trgm` sobre `nombre` para que el `ILIKE '%q%'` sea eficiente cuando la tabla crezca.

### Seeder de alimentos

El backend debe incluir un seeder que cargue al menos los alimentos más habituales en nutrición deportiva para que la herramienta sea usable desde el primer día. Ejemplo mínimo (~50 alimentos):

| nombre | kcal | prot | carbs | grasa |
|---|---|---|---|---|
| Pechuga de pollo | 165 | 31.0 | 0.0 | 3.6 |
| Pavo pechuga | 135 | 29.0 | 0.0 | 1.7 |
| Salmón | 208 | 20.0 | 0.0 | 13.0 |
| Atún al natural | 116 | 25.5 | 0.0 | 0.9 |
| Huevo entero | 143 | 12.6 | 0.7 | 9.5 |
| Clara de huevo | 52 | 10.9 | 0.7 | 0.2 |
| Ternera magra | 158 | 26.0 | 0.0 | 5.5 |
| Merluza | 82 | 17.5 | 0.0 | 1.2 |
| Gambas | 85 | 18.0 | 0.0 | 1.0 |
| Arroz blanco cocido | 130 | 2.7 | 28.2 | 0.3 |
| Arroz integral cocido | 112 | 2.6 | 23.5 | 0.9 |
| Pasta cocida | 131 | 5.0 | 25.0 | 1.1 |
| Pan integral | 247 | 8.5 | 41.3 | 3.4 |
| Avena | 366 | 13.2 | 58.7 | 6.9 |
| Patata cocida | 86 | 2.0 | 20.1 | 0.1 |
| Boniato | 86 | 1.6 | 20.1 | 0.1 |
| Legumbres cocidas (garbanzos) | 164 | 8.9 | 27.4 | 2.6 |
| Lentejas cocidas | 116 | 9.0 | 20.1 | 0.4 |
| Leche entera | 61 | 3.2 | 4.8 | 3.3 |
| Leche desnatada | 35 | 3.4 | 5.0 | 0.1 |
| Yogur griego natural | 97 | 9.0 | 3.6 | 5.0 |
| Queso cottage | 98 | 11.1 | 3.4 | 4.3 |
| Queso fresco | 74 | 7.3 | 2.7 | 3.2 |
| Requesón | 74 | 10.0 | 4.0 | 1.7 |
| Plátano | 89 | 1.1 | 22.8 | 0.3 |
| Manzana | 52 | 0.3 | 13.8 | 0.2 |
| Naranja | 47 | 0.9 | 11.8 | 0.1 |
| Fresas | 32 | 0.7 | 7.7 | 0.3 |
| Arándanos | 57 | 0.7 | 14.5 | 0.3 |
| Brócoli | 34 | 2.8 | 6.6 | 0.4 |
| Espinacas | 23 | 2.9 | 3.6 | 0.4 |
| Lechuga | 15 | 1.4 | 2.9 | 0.2 |
| Tomate | 18 | 0.9 | 3.9 | 0.2 |
| Pepino | 16 | 0.7 | 3.6 | 0.1 |
| Zanahoria | 41 | 0.9 | 9.6 | 0.2 |
| Pimiento rojo | 31 | 1.0 | 6.0 | 0.3 |
| Aguacate | 160 | 2.0 | 8.5 | 14.7 |
| Aceite de oliva | 884 | 0.0 | 0.0 | 100.0 |
| Almendras | 579 | 21.2 | 21.6 | 49.9 |
| Nueces | 654 | 15.2 | 13.7 | 65.2 |
| Mantequilla de cacahuete | 588 | 25.1 | 20.0 | 50.4 |
| Proteína whey (polvo) | 370 | 75.0 | 8.0 | 4.0 |
| Leche de avena | 46 | 1.0 | 8.0 | 1.5 |
| Tortita de arroz | 387 | 8.0 | 80.0 | 3.0 |
| Pan de molde blanco | 265 | 8.0 | 49.0 | 3.2 |

> El seeder no debe ejecutarse si ya existen registros en la tabla (idempotente). Los nutricionistas pueden añadir los suyos propios desde el buscador usando `POST /api/v1/alimentos`.

---

