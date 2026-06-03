# Contrato API - Dashboard Atleta

Este documento define el contrato entre el frontend Angular y el backend para todas las funcionalidades del dashboard del atleta.

## Base URL
```
/api/v1/atleta
```

## Endpoints Implementados

### 1. Obtener Perfil del Atleta
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
    fechaNac: string;
    genero: 'HOMBRE' | 'MUJER' | 'OTRO';
    peso: number;
    altura: number;
    deporte: string;
    nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | 'ELITE';
    servicio: 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';
    objetivo: 'RENDIMIENTO' | 'MASA_MUSCULAR' | 'PERDER_PESO' | 'SALUD' | 'RESISTENCIA' | null;
    alergias: string[];
    lesiones: string[];
    fotoUrl: string | null;
  }
}
```

---

### 2. Obtener Plan Activo de Entrenamiento
**Método:** `GET`  
**Endpoint:** `/entrenamiento/plan-activo`  
**Autenticación:** ✅ Required

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    nombre: string;
    descripcion: string;
    semanas?: number;
    semanaActual?: number;
    sesiones: {
      nombre: string;
      ejercicios: {
        nombre: string;
        series: number;
        reps: string;
        descanso?: string;
        notas?: string;
      }[];
    }[];
    creadoEn?: string;
    atletaId?: string;
  } | null;
}
```

---

### 3. Obtener Profesionales Asignados
**Método:** `GET`  
**Endpoint:** `/profesionales`  
**Autenticación:** ✅ Required

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    nombre: string;
    titulacion: string;
    rol: 'ENTRENADOR' | 'NUTRICIONISTA';
    descripcion?: string;
    anosExperiencia?: number | null;
    sobreMi?: string | null;
    masters?: string[];
  }[] | null;
}
```

---

### 4. Conectar con Profesional (Código de invitación)
**Método:** `POST`  
**Endpoint:** `/conectar`  
**Autenticación:** ✅ Required

**Request Body:**
```typescript
{
  codigo: string; // Patrón: "GRIT-[A-Z0-9]{4}-[A-Z0-9]{4}"
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

### 5. Obtener Solicitud Pendiente de Check-in de Peso
**Método:** `GET`  
**Endpoint:** `/peso/solicitud-pendiente`  
**Autenticación:** ✅ Required

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    fecha: string; // ISO date: "2026-04-16"
    solicitadoPor: string; // Nombre del entrenador
  } | null;
}
```

---

### 6. Registrar Peso
**Método:** `POST`  
**Endpoint:** `/peso`  
**Autenticación:** ✅ Required

**Request Body:**
```typescript
{
  solicitudId: string;
  pesoKg: number;
}
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    fecha: string; // ISO date
    pesoKg: number;
  };
}
```
**Status:** `201 Created`

---

### 7. Obtener Historial de Pesos
**Método:** `GET`  
**Endpoint:** `/peso/historial`  
**Autenticación:** ✅ Required

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    fecha: string; // ISO date
    pesoKg: number;
  }[];
}
```

---

### 8. Obtener Notas del Nutricionista
**Método:** `GET`  
**Endpoint:** `/nutricion/notas`  
**Autenticación:** ✅ Required

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    texto: string;
    fecha: string; // ISO date
  }[];
}
```

---

### 9. Obtener Plan Nutricional Activo
**Método:** `GET`  
**Endpoint:** `/nutricion/plan-activo`  
**Autenticación:** ✅ Required

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    nombre: string;
    descripcion: string;
    kcalDiarias: number;
    proteinas?: number;
    carbos?: number;
    grasas?: number;
    comidas: {
      nombre: string;
      alimentos: {
        nombre: string;
        cantidad: string;
        kcal?: number;
        proteinas?: number;
        carbos?: number;
        grasas?: number;
      }[];
      notas?: string;
    }[];
  } | null;
}
```

---

### 10. Cambiar Contraseña
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

### 11. Eliminar Cuenta
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

### 12. Subir Foto de Perfil
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

### 13. Obtener Hilo de Ejercicio (Notas del entrenador + Mensajes)
**Método:** `GET`  
**Endpoint:** `/entrenamiento/hilo?dia={sesionDia}&ejercicio={ejercicioNombre}`  
**Autenticación:** ✅ Required

**Query Parameters:**
- `dia` (string): Nombre de la sesión (e.g., "Lunes")
- `ejercicio` (string URL-encoded): Nombre del ejercicio

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    sesionDia: string;
    ejercicioNombre: string;
    notaEntrenador?: string;
    media: {
      id: string;
      tipo: 'foto' | 'video';
      url: string;
      fecha: string; // ISO date
    }[];
    mensajes: {
      id: string;
      texto: string;
      fecha: string; // ISO date
      esAtleta: boolean;
      autor: string;
    }[];
  };
}
```

---

### 14. Enviar Mensaje en Hilo de Ejercicio
**Método:** `POST`  
**Endpoint:** `/entrenamiento/hilo/mensaje`  
**Autenticación:** ✅ Required

**Request Body:**
```typescript
{
  sesionDia: string;
  ejercicioNombre: string;
  texto: string;
}
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    texto: string;
    fecha: string; // ISO date
    esAtleta: boolean;
    autor: string;
  };
}
```

---

### 15. Subir Media (Foto/Video) en Hilo de Ejercicio
**Método:** `POST`  
**Endpoint:** `/entrenamiento/hilo/media`  
**Autenticación:** ✅ Required
**Content-Type:** `multipart/form-data`

**Request Body:**
```
FormData:
  - archivo: File (image/* | video/*)
  - sesionDia: string
  - ejercicioNombre: string
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    tipo: 'foto' | 'video';
    url: string; // URL en S3 o servidor
    fecha: string; // ISO date
  };
}
```

---

### 16. Obtener Chat con Entrenador/Nutricionista
**Método:** `GET`  
**Endpoint:** `/chat/{tipo}`  
**Autenticación:** ✅ Required

**Path Parameters:**
- `tipo`: `'entrenador'` | `'nutricionista'`

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    tipo: 'entrenador' | 'nutricionista';
    interlocutor: string; // Nombre del profesional
    mensajes: {
      id: string;
      texto: string;
      fecha: string; // ISO date
      esAtleta: boolean;
      autor: string;
      adjunto?: {
        url: string;
        tipo: 'foto' | 'video';
        nombre: string;
      };
    }[];
  };
}
```

---

### 17. Enviar Mensaje en Chat
**Método:** `POST`  
**Endpoint:** `/chat/{tipo}/mensaje`  
**Autenticación:** ✅ Required
**Content-Type:** `multipart/form-data` (si incluye adjunto)

**Path Parameters:**
- `tipo`: `'entrenador'` | `'nutricionista'`

**Request Body:**
```
FormData:
  - texto: string (opcional)
  - archivo: File (opcional, image/* | video/*)
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    texto: string;
    fecha: string; // ISO date
    esAtleta: boolean;
    autor: string;
    adjunto?: {
      url: string;
      tipo: 'foto' | 'video';
      nombre: string;
    };
  };
}
```

---

### 18. Subir Archivo en Chat
**Método:** `POST`  
**Endpoint:** `/chat/{tipo}/archivo`  
**Autenticación:** ✅ Required
**Content-Type:** `multipart/form-data`

**Path Parameters:**
- `tipo`: `'entrenador'` | `'nutricionista'`

**Request Body:**
```
FormData:
  - archivo: File (image/* | video/*)
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    url: string;
    tipo: 'foto' | 'video';
    nombre: string;
  };
}
```

---

### 19. Obtener Hilo de Comida
**Método:** `GET`  
**Endpoint:** `/nutricion/hilo?comida={comidaNombre}`  
**Autenticación:** ✅ Required

**Query Parameters:**
- `comida` (string URL-encoded): Nombre de la comida (e.g., "Desayuno")

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    comidaNombre: string;
    mensajes: {
      id: string;
      texto: string;
      fecha: string; // ISO date
      esAtleta: boolean;
      autor: string;
    }[];
  };
}
```

---

### 20. Enviar Mensaje en Hilo de Comida
**Método:** `POST`  
**Endpoint:** `/nutricion/hilo/mensaje`  
**Autenticación:** ✅ Required

**Request Body:**
```typescript
{
  comidaNombre: string;
  texto: string;
}
```

**Response:**
```typescript
{
  ok: boolean;
  message: string;
  data: {
    id: string;
    texto: string;
    fecha: string; // ISO date
    esAtleta: boolean;
    autor: string;
  };
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
4. **Null Handling:** Para endpoints opcionales (planes, solicitudes, etc.), devuelve `null` en lugar de error HTTP si no existen
5. **Archivos:** Para uploads, usar `FormData` con `withCredentials: true`

---

## Servicios Auxiliares del Backend Requeridos

Los siguientes servicios Java deben estar implementados:
- `AtletaService` - Lógica de perfil y datos del atleta
- `VinculacionService` - Manejo de códigos de conexión
- `PesoService` - Gestión de solicitudes y registro de pesos
- `ChatService` - Gestión de mensajes (entrenador/nutricionista)
- `EntrenamientoService` - Planes y hilos de ejercicios
- `NutricionService` - Planes nutricionales y hilos de comidas

