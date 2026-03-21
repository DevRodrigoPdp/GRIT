# GRIT — Especificación Backend

> **Rama de trabajo:** `frontend` (en desarrollo) · **Stack frontend:** Angular 21 + Tailwind CSS
> **Este documento** describe todos los contratos de API, modelos de datos y requisitos que el equipo de backend debe implementar para dar soporte a la plataforma GRIT.

---

## 1. Contexto del Producto

GRIT es una plataforma de rendimiento deportivo de alto nivel con dos tipos de usuario:

| Rol | Descripción |
|---|---|
| **Entrenador** | Profesional con credenciales verificables (colegiado, titulaciones, nutrición). Su cuenta requiere revisión manual antes de activarse. |
| **Atleta** | Usuario que se registra con datos personales y físicos. Su cuenta se activa inmediatamente. |

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
    "estado": "ACTIVO" | "PENDIENTE_REVISION" | "RECHAZADO"
  }
}
```

**Cookies que debe setear el servidor:**
```
Set-Cookie: access_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900
Set-Cookie: refresh_token=<jwt>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=604800
```

> **Nota:** Si el entrenador está en `PENDIENTE_REVISION`, el frontend mostrará una pantalla de espera. El backend debe indicarlo en el response body del login.

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

### 3.5 Healthcheck

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
2. **Migraciones de BBDD** (`/bbdd` — schema SQL inicial)
3. **Healthcheck** (`GET /api/v1/health`)
4. **Registro de Atleta** (el más simple, sin archivos ni revisión manual)
5. **Login + JWT + Refresh**
6. **Registro de Entrenador** (con upload a S3 y flujo de revisión)
7. **Sistema de emails**
8. **Rate limiting + seguridad**
9. **Tests de integración** para todos los endpoints

---

## 10. Preguntas Abiertas para el Equipo

- [ ] ¿Quién gestiona la **revisión manual** de los entrenadores? ¿Panel de admin o proceso manual?
- [ ] ¿El entrenador debe crear su **contraseña** durante el registro o se le envía por email tras la aprobación?
- [ ] ¿Necesitamos **OAuth** (Google, Apple) en la primera versión?
- [ ] ¿Cuál es el **dominio de producción** definitivo?

---

*Documento generado el 21 de marzo de 2026 — actualizar conforme evolucione el producto.*
