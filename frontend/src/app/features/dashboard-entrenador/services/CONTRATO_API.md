# Contrato API - Dashboard Entrenador

Este documento define el contrato entre el frontend Angular y el backend para todas las funcionalidades del dashboard del entrenador.

## Base URL
```
/api/v1/entrenador
```

## Endpoints Implementados

### 1. Obtener Perfil del Entrenador
**Método:** `GET`  
**Endpoint:** `/perfil`  
**Autenticación:** ✅ Required (`withCredentials: true`)

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    nombre: string;
    email: string;
    titulacionEntrenamiento: 'GRADO_CAFYD' | 'TSAF_TSEAS' | 'CERT_AFDA0210' | null;
    titulacionNutricion: 'GRADO_NUTRICION_DIETETICA' | 'TSD' | null;
    experienciaAnos: number;
    descripcion: string;
    masters: string[];
    estado: 'ACTIVO' | 'PENDIENTE_REVISION' | 'RECHAZADO';
    solicitudAmpliacionPendiente?: 'ENTRENAMIENTO' | 'NUTRICION' | null;
    codigoInvitacion: string;
    fotoUrl: string | null;
  }
}
```

---

### 2. Obtener Mis Atletas
**Método:** `GET`  
**Endpoint:** `/atletas`  
**Autenticación:** ✅ Required

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    nombre: string;
    deporte: string;
    nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | 'ELITE';
    servicio: 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';
    tienePlanActivo: boolean;
    alergias?: string[];
    intolerancias?: string[];
  }[] | null;
}
```

---

### 3. Solicitar Check-in de Peso para Atleta
**Método:** `POST`  
**Endpoint:** `/atletas/{atletaId}/peso/solicitar`  
**Autenticación:** ✅ Required

**Path Parameters:**
- `atletaId` (UUID): ID del atleta

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: null;
}
```
**Status:** `201 Created`

---

### 4. Verificar si hay Solicitud de Peso Pendiente
**Método:** `GET`  
**Endpoint:** `/atletas/{atletaId}/peso/pendiente`  
**Autenticación:** ✅ Required

**Path Parameters:**
- `atletaId` (UUID): ID del atleta

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    pendiente: boolean;
  };
}
```

---

### 5. Obtener Historial de Pesos del Atleta
**Método:** `GET`  
**Endpoint:** `/atletas/{atletaId}/peso/historial`  
**Autenticación:** ✅ Required

**Path Parameters:**
- `atletaId` (UUID): ID del atleta

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    fecha: string; // ISO date
    pesoKg: number;
  }[] | null;
}
```

---

### 6. Cambiar Contraseña
**Método:** `PUT`  
**Endpoint:** `/password`  
**Autenticación:** ✅ Required

**Request Body:**
```typescript
{
  actual: string;
  nueva: string;
}
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: null;
}
```

---

### 7. Eliminar Cuenta
**Método:** `DELETE`  
**Endpoint:** `/cuenta`  
**Autenticación:** ✅ Required

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: null;
}
```

**Headers esperados:**
- `Set-Cookie`: `access_token=; httpOnly; secure; path=/; maxAge=0;`

---

### 8. Subir Foto de Perfil
**Método:** `POST`  
**Endpoint:** `/foto`  
**Autenticación:** ✅ Required
**Content-Type:** `multipart/form-data`

**Request Body:**
```
FormData:
  - foto: File (image/*)
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    url: string; // URL de la imagen subida
  };
}
```

---

### 9. Invitar Atleta
**Método:** `POST`  
**Endpoint:** `/invitar`  
**Autenticación:** ✅ Required

**Request Body:**
```typescript
{
  email: string;
}
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: null;
}
```

---

### 10. Verificar si Código de Colegiado Existe
**Método:** `GET`  
**Endpoint:** `/check-codigo/{codigo}`  
**Autenticación:** ✅ Required

**Path Parameters:**
- `codigo` (string): Código del colegio profesional

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: boolean; // true si existe, false si no
}
```

---

### 11. Solicitar Ampliación de Formación
**Método:** `POST`  
**Endpoint:** `/ampliar-formacion`  
**Autenticación:** ✅ Required
**Content-Type:** `multipart/form-data`

**Request Body:**
```
FormData:
  - modulo: 'ENTRENAMIENTO' | 'NUTRICION'
  - titulacion: string (ej: 'GRADO_NUTRICION_DIETETICA')
  - documentos: File[] (múltiples archivos PDF)
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: null;
}
```

---

## Notas Importantes

1. **Autenticación:** Todos los endpoints requieren cookies de sesión (`withCredentials: true`)
2. **Estructura de Respuesta:** Todos los endpoints devuelven `ApiResponseDTO<T>` con campos:
   - `ok: boolean` - Indica éxito o error
   - `message: string` - Mensaje descriptivo
   - `data: T | null` - Los datos solicitados
3. **Manejo de Errores:** El frontend implementa `map(response => response.data)` para extraer directamente los datos
4. **Null Handling:** Para endpoints opcionales, devuelve `null` en lugar de error HTTP si no existen
5. **Archivos:** Para uploads, usar `FormData` con `withCredentials: true`

---

## Servicios Auxiliares del Backend Requeridos

Los siguientes servicios Java deben estar implementados:
- `EntrenadorService` - Lógica de perfil y datos del entrenador
- `PesoService` - Gestión de solicitudes y registro de pesos
- Otros servicios específicos del dominio

