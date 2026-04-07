# Implementación de Registro y Login en GRIT Frontend

## Registro de Atleta
- **Endpoint**: `POST /api/v1/auth/registro/atleta`
- **Payload**: `RegistroAtletaPayload`
  - `nombre`: string
  - `email`: string
  - `password`: string
  - `fechaNac`: string
  - `genero`: string
  - `pesoKg`: number
  - `alturaCm`: number
  - `deporte`: string
  - `nivel`: string
  - `servicio`: ServicioAtleta ('ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS')
  - `objetivo`: string | null
- **Comportamiento**:
  - Envía datos JSON al backend.
  - Establece sesión con rol 'ATLETA', estado inicial, servicio seleccionado y nombre.
  - Redirige automáticamente a `/dashboard/atleta`.

## Registro de Entrenador
- **Endpoint**: `POST /api/v1/auth/registro/entrenador`
- **Payload**: `RegistroEntrenadorPayload`
  - `nombre`: string
  - `email`: string
  - `password`: string
  - `codigoProfesional`: string (opcional)
  - `titulacionEntrenamiento`: File (opcional)
  - `titulacionNutricion`: File (opcional)
  - `documentos`: File[]
- **Comportamiento**:
  - Envía datos como `FormData` para manejar archivos (titulaciones y documentos).
  - Estado inicial: `PENDIENTE_REVISION` (pendiente de aprobación por admin).
  - Determina `tituloEntrenamiento` y `tituloNutricion` basándose en la presencia de archivos de titulación.
  - Servicio: `null` (no aplica para entrenadores).
  - Redirección basada en títulos:
    - Ambos títulos: `/dashboard/entrenador/nutricion`
    - Solo nutrición: `/dashboard/entrenador/solo-nutricion`
    - Solo entrenamiento o ninguno: `/dashboard/entrenador`

## Login
- **Endpoint**: `POST /api/v1/auth/login`
- **Payload**: `{ email: string, password: string }`
- **Comportamiento**:
  - Autentica usuario y obtiene datos de sesión del backend.
  - Establece sesión con rol, estado, títulos, servicio y nombre.
  - Redirige según rol y estado:
    - Atleta: `/dashboard/atleta`
    - Entrenador: Según títulos (como arriba)
    - Pendiente: `/pendiente`
    - Rechazado: Muestra error
- **Manejo de Errores**:
  - 401: "Correo o contraseña incorrectos."
  - 403: "Tu cuenta ha sido rechazada. Contacta con soporte."
  - Otros: "Error de conexión. Inténtalo de nuevo."