# GRIT — Especificación Backend

> **Rama de trabajo:** `frontend` (en desarrollo) · **Stack frontend:** Angular 21 + Tailwind CSS
> **Este documento** describe todos los contratos de API, modelos de datos y requisitos que el equipo de backend debe implementar para dar soporte a la plataforma GRIT.
> **Última actualización:** 28 de marzo de 2026

---

## 1. Contexto del Producto

GRIT es una plataforma de rendimiento deportivo de alto nivel con dos tipos de usuario y **cuatro dashboards diferenciados**:

| Rol | Titulaciones | Dashboard | Descripción |
|---|---|---|---|
| **Entrenador** | Entrenamiento + Nutrición | `/dashboard/entrenador/nutricion` | Acceso completo: entrenamiento + planes nutricionales |
| **Entrenador** | Solo Entrenamiento | `/dashboard/entrenador` | Solo entrenamiento. Sin acceso a módulos de nutrición |
| **Entrenador** | Solo Nutrición | `/dashboard/entrenador/solo-nutricion` | Solo nutrición. Sin acceso a módulos de entrenamiento |
| **Atleta** | — | `/dashboard/atleta` | Métricas, planes y seguimiento personal |

El dashboard al que se redirige a un entrenador (tras login o registro aprobado) se determina combinando `tituloEntrenamiento` y `tituloNutricion`:

| `tituloEntrenamiento` | `tituloNutricion` | Redirección |
|---|---|---|
| `true` | `true` | `/dashboard/entrenador/nutricion` |
| `true` | `false` | `/dashboard/entrenador` |
| `false` | `true` | `/dashboard/entrenador/solo-nutricion` |

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
| `correo` | `string` | ✅ | Formato email válido, único en BBDD |
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
| `correo` | `string` | ✅ | Formato email válido, único en BBDD |
| `password` | `string` | ✅ | Min 8 caracteres |
| `fechaNac` | `string` (ISO 8601) | ✅ | Formato `YYYY-MM-DD`, mayor de 14 años |
| `genero` | `enum` | ✅ | `hombre` \| `mujer` \| `otro` |
| `peso` | `number` | ✅ | Entre 30 y 300 (kg) |
| `altura` | `number` | ✅ | Entre 100 y 250 (cm) |
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

**Valores válidos para `servicio`:**
```
ENTRENAMIENTO | NUTRICION | AMBOS
```

**Validación cruzada obligatoria en servidor:**
```
si servicio == NUTRICION  → objetivo debe ser null o ausente
si servicio == ENTRENAMIENTO o AMBOS → objetivo es obligatorio (400 si falta)
```

**Ejemplo body — solo nutrición:**
```json
{
  "nombre": "Laura Sánchez",
  "correo": "laura@email.com",
  "password": "MiPassword123",
  "fechaNac": "1998-05-14",
  "genero": "mujer",
  "peso": 65,
  "altura": 168,
  "deporte": "Crossfit",
  "nivel": "INTERMEDIO",
  "servicio": "NUTRICION",
  "objetivo": null
}
```

**Ejemplo body — entrenamiento o ambos:**
```json
{
  "nombre": "Carlos Ruiz",
  "correo": "carlos@email.com",
  "password": "MiPassword123",
  "fechaNac": "1995-03-20",
  "genero": "hombre",
  "peso": 80,
  "altura": 180,
  "deporte": "Ciclismo",
  "nivel": "AVANZADO",
  "servicio": "AMBOS",
  "objetivo": "RENDIMIENTO"
}
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

### 3.6 Solicitud de Ampliación de Permisos *(implementación futura)*

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

**Flujo:**
1. Entrenador hace clic en "¿Tienes título de nutrición?" desde su dashboard de entrenamiento
2. Sube la documentación acreditativa
3. Estado pasa a `PENDIENTE_REVISION_NUTRICION` — sigue accediendo a su dashboard con normalidad
4. Admin aprueba → `titulo_nutricion = true` en BBDD + email de confirmación
5. En el siguiente login el frontend recibe `tituloNutricion: true` y redirige a `/dashboard/entrenador/nutricion`

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

**Flujo:**
1. Entrenador hace clic en "¿Tienes título de entrenamiento?" desde su dashboard de nutrición
2. Sube la documentación acreditativa
3. Estado pasa a `PENDIENTE_REVISION_ENTRENAMIENTO` — sigue accediendo a su dashboard con normalidad
4. Admin aprueba → `titulo_entrenamiento = true` en BBDD + email de confirmación
5. En el siguiente login el frontend recibe `tituloEntrenamiento: true` y redirige a `/dashboard/entrenador/nutricion`

---

### 3.7 Protección de endpoints por titulación

#### Endpoints de Nutrición

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

#### Endpoints de Entrenamiento

Todos los endpoints bajo `/api/v1/entrenamiento/**` deben validar en servidor que el usuario autenticado es un entrenador con `titulo_entrenamiento = true`.

**Middleware a aplicar en esas rutas:**
1. Verificar cookie `access_token` válida
2. Verificar que `rol === 'ENTRENADOR'`
3. Verificar que `titulo_entrenamiento === true` en BBDD

**Respuesta si no tiene titulación de entrenamiento (`403`):**
```json
{
  "ok": false,
  "error": "ACCESO_DENEGADO_SIN_TITULACION_ENTRENAMIENTO",
  "message": "No tienes autorización para acceder a los módulos de entrenamiento. Se requiere titulación acreditada."
}
```

> Esta validación **siempre ocurre en servidor**, independientemente de lo que muestre el frontend. El frontend oculta las rutas, pero el backend las bloquea.

---

### 3.8 Endpoints de Administración — Revisión de Credenciales

> **Seguridad:** Todos estos endpoints requieren cookie `access_token` válida con `rol === 'ADMIN'`.
> El panel de administración **no existe en el frontend** — la gestión se hace íntegramente desde el backend (interfaz propia, herramienta interna o cliente de API). Esto es una decisión de seguridad deliberada: la lógica de validación no se expone en el navegador del cliente.

---

**`GET /api/v1/admin/entrenadores?status=pending`**

Devuelve la lista de entrenadores cuya documentación está pendiente de revisión.

```json
// Response 200
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

> `url_firmada` es una pre-signed URL de S3 con expiración corta (ej. 15 min). El admin la usa para abrir el PDF directamente. Nunca se devuelve la `url_s3` privada.

---

**`POST /api/v1/admin/entrenadores/:id/aprobar`**

Aprueba la documentación de un entrenador.

**Acciones que debe realizar el backend:**
1. Cambiar `usuarios.estado` → `ACTIVO`
2. Cambiar `documentos_entrenador.status` → `verified` y setear `reviewed_at`
3. Enviar email de aprobación al entrenador (ver sección 7)

```json
// Response 200
{ "ok": true, "message": "Entrenador aprobado correctamente." }
```

---

**`POST /api/v1/admin/entrenadores/:id/rechazar`**

Rechaza la documentación de un entrenador con un motivo.

**Body:**
```json
{ "motivo": "El PDF es ilegible. Por favor, sube una versión de mayor calidad." }
```

**Acciones que debe realizar el backend:**
1. Cambiar `usuarios.estado` → `RECHAZADO`
2. Cambiar `documentos_entrenador.status` → `rejected` y setear `reviewed_at`
3. Guardar el motivo en `documentos_entrenador.rejection_reason`
4. Enviar email de rechazo al entrenador incluyendo el motivo (ver sección 7)

```json
// Response 200
{ "ok": true, "message": "Solicitud rechazada. El entrenador ha sido notificado." }
```

> El frontend de entrenador en `/pendiente` mostrará el estado `RECHAZADO` cuando el entrenador haga login. El `rejection_reason` se puede devolver en `GET /api/v1/auth/me` para mostrárselo al entrenador si se desea.

---

### 3.9 Healthcheck

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
| `codigo_profesional` | VARCHAR(20) UNIQUE NULLABLE | Nº de colegiado (titulación universitaria) o nº de registro/asociado (FP/certificado). Ver nota en sección 3.1 |
| `titulacion_entrenamiento` | ENUM NULLABLE | `GRADO_CAFYD`, `TSAF_TSEAS`, `CERT_AFDA0210`. `null` si es puramente nutricionista |
| `titulacion_nutricion` | ENUM NULLABLE | `GRADO_NUTRICION_DIETETICA`, `TSD`. `null` si no tiene titulación en nutrición |
| `titulo_entrenamiento` | BOOLEAN | Derivado: `titulacion_entrenamiento IS NOT NULL` |
| `titulo_nutricion` | BOOLEAN | Derivado: `titulacion_nutricion IS NOT NULL` |

> **Restricción:** `titulo_entrenamiento` y `titulo_nutricion` no pueden ser ambos `false` simultáneamente — el backend debe rechazar el registro con 400 si se da ese caso.

### Tabla `documentos_entrenador`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `entrenador_id` | UUID FK → entrenadores | |
| `nombre_archivo` | VARCHAR(255) | Nombre original del archivo |
| `url_s3` | TEXT | URL privada del archivo en S3 (nunca pública) |
| `tipo_mime` | VARCHAR(50) | `application/pdf`, `image/jpeg`, `image/png` |
| `tamanyo_bytes` | INTEGER | |
| `status` | ENUM | `pending`, `verified`, `rejected` — estado de revisión del documento |
| `rejection_reason` | TEXT NULLABLE | Motivo de rechazo redactado por el admin. `null` si no ha sido rechazado |
| `uploaded_at` | TIMESTAMP | Fecha de subida del documento (auditoría) |
| `reviewed_at` | TIMESTAMP NULLABLE | Fecha en que el admin tomó la decisión |

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
| `servicio` | ENUM | `ENTRENAMIENTO`, `NUTRICION`, `AMBOS` — **obligatorio** |
| `objetivo` | ENUM NULLABLE | `RENDIMIENTO`, `MASA_MUSCULAR`, `PERDER_PESO`, `SALUD`, `RESISTENCIA` — **null si servicio = NUTRICION** |

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
   - Guardar `titulo_entrenamiento` y `titulo_nutricion` correctamente en BBDD
   - Rechazar con 400 si ambas titulaciones son `null`
   - El login debe devolver `tituloEntrenamiento` y `tituloNutricion` para que el frontend redirija al dashboard correcto (4 casos posibles — ver sección 1)
7. **Endpoints de administración** (sección 3.8) — aprobar/rechazar entrenadores
   - Proteger con rol `ADMIN` en el JWT
   - Generar pre-signed URLs de S3 para visualizar los PDFs
   - Actualizar `documentos_entrenador.status`, `rejection_reason` y `reviewed_at`
8. **Middleware de protección por titulación**
   - Rutas de nutrición: requieren `titulo_nutricion === true`
   - Rutas de entrenamiento: requieren `titulo_entrenamiento === true`
8. **Sistema de emails**
9. **Rate limiting + seguridad**
10. **Tests de integración** para todos los endpoints, incluyendo el caso de acceso denegado a nutrición

---

## 10. Preguntas Abiertas para el Equipo

- [x] ¿Quién gestiona la **revisión manual** de los entrenadores? → **Backend exclusivamente** (endpoints `/api/v1/admin/**` protegidos por rol `ADMIN`). No hay panel de admin en el frontend por razones de seguridad.
- [ ] ¿El entrenador debe crear su **contraseña** durante el registro o se le envía por email tras la aprobación?
- [ ] ¿Necesitamos **OAuth** (Google, Apple) en la primera versión?
- [ ] ¿Cuál es el **dominio de producción** definitivo?
- [ ] ¿Los endpoints de nutrición (`/api/v1/nutricion/**`) se desarrollan en esta fase o en una siguiente iteración?
- [ ] ¿Se necesita endpoint `GET /api/v1/auth/me` para que el frontend pueda recuperar el perfil sin hacer login de nuevo (útil tras actualización de `titulo_nutricion`)?

---

*Última actualización: 31 de marzo de 2026 — añadida sección 3.8 (endpoints admin), campos de auditoría en `documentos_entrenador` y decisión de arquitectura: gestión de credenciales solo en backend*
