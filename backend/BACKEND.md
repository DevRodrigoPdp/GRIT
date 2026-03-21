# GRIT — Especificación Backend

> **Rama de trabajo:** `frontend` (en desarrollo) · **Stack frontend:** Angular 21 + Tailwind CSS
> **Este documento** describe todos los contratos de API, modelos de datos y requisitos que el equipo de backend debe implementar para dar soporte a la plataforma GRIT.
> **Última actualización:** 21 de marzo de 2026

---

## 1. Contexto del Producto

GRIT es una plataforma de rendimiento deportivo de alto nivel con dos tipos de usuario y **tres dashboards diferenciados**:

| Rol | Subtipo | Dashboard | Descripción |
|---|---|---|---|
| **Entrenador** | Con título de nutrición | `/dashboard/entrenador/nutricion` | Acceso completo: entrenamiento + planes nutricionales |
| **Entrenador** | Sin título de nutrición | `/dashboard/entrenador` | Solo entrenamiento. Sin acceso a módulos de nutrición |
| **Atleta** | — | `/dashboard/atleta` | Métricas, planes y seguimiento personal |

> **Principio clave — No intrusión laboral:** Un entrenador sin titulación en nutrición **no puede** crear, editar ni visualizar planes nutricionales dentro de la plataforma. Esta restricción se aplica tanto en frontend (rutas protegidas) como en backend (validación en cada endpoint de nutrición).

---

## 2. Stack Recomendado

No hay restricción técnica, pero se recomienda:

- **Lenguaje:** Node.js (TypeScript) / Python / Go
- **Framework:** Express / Fastify / NestJS / FastAPI
- **Base de datos:** PostgreSQL (ver carpeta `/bbdd`)
- **Almacenamiento de archivos:** S3-compatible (AWS S3, MinIO, Cloudflare R2)
- **Autenticación:** JWT (access token + refresh token)
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
| `correo` | `string` | ✅ | Formato email válido, único en BBDD |
| `codigoColegiado` | `string` | ✅ | Alfanumérico, 4–20 caracteres, único en BBDD |
| `titulacion` | `enum` | ✅ | Ver valores válidos abajo |
| `tituloNutricion` | `boolean` | ✅ | `true` / `false` |
| `documentos` | `File[]` | ✅ | 1–10 archivos, formatos: PDF/JPG/JPEG/PNG, max 10 MB cada uno |

**Valores válidos para `titulacion`:**
```
TAFAD | GRADO_CAFYD | MASTER_RENDIMIENTO | MASTER_ENTRENAMIENTO | CICLO_FP | OTRO
```

**Respuesta 201 (éxito):**
```json
{
  "ok": true,
  "message": "Solicitud recibida. Revisaremos tus credenciales en un plazo máximo de 48h y te notificaremos por correo.",
  "data": {
    "id": "uuid-del-entrenador",
    "estado": "PENDIENTE_REVISION"
  }
}
```

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

Recibe el formulario como `application/json`. No hay archivos.

**Campos del body:**

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `nombre` | `string` | ✅ | Min 3 caracteres |
| `correo` | `string` | ✅ | Formato email válido, único en BBDD |
| `fechaNac` | `string` (ISO 8601) | ✅ | Formato `YYYY-MM-DD`, mayor de 14 años |
| `genero` | `enum` | ✅ | `hombre` \| `mujer` \| `otro` |
| `peso` | `number` | ✅ | Entre 30 y 300 (kg) |
| `altura` | `number` | ✅ | Entre 100 y 250 (cm) |
| `deporte` | `string` | ✅ | Min 3 caracteres |
| `nivel` | `enum` | ✅ | Ver valores válidos abajo |
| `objetivo` | `enum` | ✅ | Ver valores válidos abajo |

**Valores válidos para `nivel`:**
```
PRINCIPIANTE | INTERMEDIO | AVANZADO | ELITE
```

**Valores válidos para `objetivo`:**
```
PERDER_PESO | GANAR_MASA | RENDIMIENTO | RESISTENCIA | SALUD | REHABILITACION
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

```json
// Request body
{
  "correo": "usuario@ejemplo.com",
  "password": "contraseña"
}

// Response 200 — los tokens van en cookies, no en el body
{
  "ok": true,
  "data": {
    "rol": "ENTRENADOR" | "ATLETA",
    "estado": "ACTIVO" | "PENDIENTE_REVISION" | "RECHAZADO",
    "tituloNutricion": true | false | null
  }
}
```

> `tituloNutricion` solo es relevante cuando `rol === "ENTRENADOR"`. Para atletas devolver `null`.

**Cookies que debe setear el servidor:**
```
Set-Cookie: access_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900
Set-Cookie: refresh_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=604800
```

**Lógica de redirección que aplica el frontend según la respuesta:**

| `rol` | `estado` | `tituloNutricion` | Redirección |
|---|---|---|---|
| `ATLETA` | `ACTIVO` | `null` | `/dashboard/atleta` |
| `ENTRENADOR` | `ACTIVO` | `true` | `/dashboard/entrenador/nutricion` |
| `ENTRENADOR` | `ACTIVO` | `false` | `/dashboard/entrenador` |
| `ENTRENADOR` | `PENDIENTE_REVISION` | cualquiera | `/pendiente` |
| cualquiera | `RECHAZADO` | cualquiera | `/login` con mensaje de error |

> **Nota:** Si el entrenador está en `PENDIENTE_REVISION`, el frontend mostrará una pantalla de espera informando que la solicitud está siendo revisada (plazo máximo 48h).

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

### 3.6 Solicitud de Ampliación de Permisos de Nutrición *(implementación futura)*

Un entrenador que en el registro indicó **no tener** título de nutrición podrá solicitarlo más adelante aportando nueva documentación.

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

**Flujo completo:**
1. Entrenador hace clic en "Ampliar acceso a Nutrición" desde su dashboard
2. Sube la documentación acreditativa
3. Estado pasa a `PENDIENTE_REVISION_NUTRICION` — sigue accediendo a su dashboard de entrenamiento con normalidad
4. Admin revisa y aprueba/rechaza
5. Si se aprueba: `titulo_nutricion` pasa a `true` en BBDD y se envía email de confirmación
6. En el siguiente login (o con un endpoint de `/api/v1/auth/me`), el frontend recibe `tituloNutricion: true` y redirige al dashboard completo

---

### 3.7 Protección de endpoints de Nutrición

Todos los endpoints bajo `/api/v1/nutricion/**` deben validar en servidor que el usuario autenticado es un entrenador con `titulo_nutricion = true`.

**Middleware a aplicar en esas rutas:**
1. Verificar cookie `access_token` válida
2. Verificar que `rol === 'ENTRENADOR'`
3. Verificar que `titulo_nutricion === true` en BBDD

**Respuesta si no tiene titulación de nutrición (`403`):**
```json
{
  "ok": false,
  "error": "ACCESO_DENEGADO_SIN_TITULACION_NUTRICION",
  "message": "No tienes autorización para acceder a los módulos de nutrición. Se requiere titulación acreditada."
}
```

> Esta validación **siempre ocurre en servidor**, independientemente de lo que muestre el frontend. El frontend oculta las rutas, pero el backend las bloquea.

---

### 3.8 Healthcheck

**`GET /api/v1/health`**


```json
{ "status": "ok", "timestamp": "2026-03-21T12:00:00Z" }
```

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
| `codigo_colegiado` | VARCHAR(20) UNIQUE | |
| `titulacion` | ENUM | |
| `titulo_nutricion` | BOOLEAN | |

### Tabla `documentos_entrenador`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | |
| `nombre_archivo` | VARCHAR(255) | Nombre original del archivo |
| `url_s3` | TEXT | URL privada del archivo en S3 |
| `tipo_mime` | VARCHAR(50) | `application/pdf`, `image/jpeg`, `image/png` |
| `tamanyo_bytes` | INTEGER | |
| `uploaded_at` | TIMESTAMP | |

### Tabla `atletas`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK FK → usuarios | |
| `fecha_nac` | DATE | |
| `genero` | ENUM | `hombre`, `mujer`, `otro` |
| `peso_kg` | DECIMAL(5,2) | |
| `altura_cm` | SMALLINT | |
| `deporte` | VARCHAR(100) | |
| `nivel` | ENUM | `PRINCIPIANTE`, `INTERMEDIO`, `AVANZADO`, `ELITE` |
| `objetivo` | ENUM | `PERDER_PESO`, `GANAR_MASA`, `RENDIMIENTO`, `RESISTENCIA`, `SALUD`, `REHABILITACION` |

---

## 5. Seguridad

- Todas las rutas bajo `/api/v1/` deben servirse **únicamente por HTTPS** en producción.
- Los endpoints de registro y login deben tener **rate limiting**: máx. 10 peticiones por IP cada 15 minutos.
- Los archivos subidos deben validarse en servidor (no solo por extensión): verificar el **magic number / MIME type real** del binario.
- Las URLs de S3 deben ser **privadas con firma temporal** (pre-signed URLs), nunca públicas.
- Contraseñas: **bcrypt** con cost factor mínimo 12. No almacenar en texto plano jamás.
- **Tokens via cookies HttpOnly** — los JWTs nunca se exponen al JavaScript del frontend (previene XSS y robo de tokens).
  - `access_token`: `HttpOnly; Secure; SameSite=Strict; Max-Age=900` (15 min)
  - `refresh_token`: `HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=604800` (7 días, solo enviado a la ruta de refresh)
- **Logout:** endpoint `POST /api/v1/auth/logout` que sobreescribe ambas cookies con `Max-Age=0` para borrarlas.

---

## 6. CORS

El frontend corre en `http://localhost:4200` en desarrollo. Configurar CORS para:

```
Allowed Origins (dev):   http://localhost:4200
Allowed Origins (prod):  https://grit.app (pendiente definir)
Allowed Methods:         GET, POST, PUT, PATCH, DELETE, OPTIONS
Allowed Headers:         Content-Type, Authorization
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
PORT=3000
NODE_ENV=development

# Base de datos
DATABASE_URL=postgresql://user:pass@localhost:5432/grit

# JWT
JWT_SECRET=cambiar-en-produccion
JWT_EXPIRES_IN=15m
JWT_REFRESH_SECRET=cambiar-en-produccion
JWT_REFRESH_EXPIRES_IN=7d

# S3
S3_BUCKET=grit-documentos
S3_REGION=eu-west-1
S3_ACCESS_KEY_ID=...
S3_SECRET_ACCESS_KEY=...

# Email
EMAIL_FROM=noreply@grit.app
SENDGRID_API_KEY=...  # o SMTP_HOST, SMTP_PORT, etc.
```

---

## 9. Orden de Implementación Sugerido

1. **Setup del proyecto** (estructura de carpetas, linting, Docker Compose con PostgreSQL)
2. **Migraciones de BBDD** (`/bbdd` — schema SQL inicial con campo `titulo_nutricion`)
3. **Healthcheck** (`GET /api/v1/health`)
4. **Registro de Atleta** (el más simple, sin archivos ni revisión manual)
5. **Login + cookies HttpOnly + Refresh + Logout**
6. **Registro de Entrenador** (con upload a S3 y flujo de revisión)
   - Guardar `titulo_nutricion` correctamente en BBDD
   - La respuesta del login debe devolver `tituloNutricion` para que el frontend redirija al dashboard correcto
7. **Middleware de protección para rutas de nutrición** (`titulo_nutricion === true`)
8. **Sistema de emails**
9. **Rate limiting + seguridad**
10. **Tests de integración** para todos los endpoints, incluyendo el caso de acceso denegado a nutrición

---

## 10. Preguntas Abiertas para el Equipo

- [ ] ¿Quién gestiona la **revisión manual** de los entrenadores? ¿Panel de admin o proceso manual?
- [ ] ¿El entrenador debe crear su **contraseña** durante el registro o se le envía por email tras la aprobación?
- [ ] ¿Necesitamos **OAuth** (Google, Apple) en la primera versión?
- [ ] ¿Cuál es el **dominio de producción** definitivo?
- [ ] ¿Los endpoints de nutrición (`/api/v1/nutricion/**`) se desarrollan en esta fase o en una siguiente iteración?
- [ ] ¿Se necesita endpoint `GET /api/v1/auth/me` para que el frontend pueda recuperar el perfil sin hacer login de nuevo (útil tras actualización de `titulo_nutricion`)?

---

*Última actualización: 21 de marzo de 2026*
