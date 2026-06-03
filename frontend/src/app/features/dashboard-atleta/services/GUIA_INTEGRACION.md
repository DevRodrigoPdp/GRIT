# Guía de Integración - AtletaService con Backend

## 📋 Resumen de Cambios

Se ha actualizado completamente el servicio `AtletaService` para implementar un contrato real con el backend. Todos los endpoints están activados y listos para consumir las APIs del backend Spring Boot.

## ✅ Cambios Realizados

### 1. **Estructuras de Datos Actualizadas**

- ✅ Agregada interfaz `ApiResponseDTO<T>` para envolver todas las respuestas del backend
- ✅ Actualizado campo `correo` → `email` en `PerfilAtleta`
- ✅ Todos los tipos son ahora compatibles con los DTOs del backend Java

### 2. **Métodos HTTP Activados**

Todos estos métodos ahora realizan **llamadas HTTP reales**:

| Método | Endpoint | Tipo HTTP |
|--------|----------|-----------|
| `getPerfil()` | `/perfil` | GET |
| `getProfesionalesAsignados()` | `/profesionales` | GET |
| `getPlanEntrenamiento()` | `/entrenamiento/plan-activo` | GET |
| `getPlanNutricion()` | `/nutricion/plan-activo` | GET |
| `getSolicitudCheckIn()` | `/peso/solicitud-pendiente` | GET |
| `getHistorialPesos()` | `/peso/historial` | GET |
| `registrarPeso()` | `/peso` | POST |
| `getNotasNutricionista()` | `/nutricion/notas` | GET |
| `subirFotoPerfil()` | `/foto` | POST (multipart) |
| `cambiarPassword()` | `/password` | PUT |
| `eliminarCuenta()` | `/cuenta` | DELETE |
| `getHiloEjercicio()` | `/entrenamiento/hilo` | GET |
| `enviarMensajeHilo()` | `/entrenamiento/hilo/mensaje` | POST |
| `getChat()` | `/chat/{tipo}` | GET |
| `enviarMensajeChat()` | `/chat/{tipo}/mensaje` | POST (multipart) |
| `subirArchivoChat()` | `/chat/{tipo}/archivo` | POST (multipart) |
| `getHiloComida()` | `/nutricion/hilo` | GET |
| `enviarMensajeHiloComida()` | `/nutricion/hilo/mensaje` | POST |
| `subirMediaHilo()` | `/entrenamiento/hilo/media` | POST (multipart) |
| `conectarConEntrenador()` | `/conectar` | POST ✨ **Nuevo** |

### 3. **Mocks Removidos**

Se han eliminado todas las constantes MOCK:
- ❌ `MOCK_PROFESIONALES`
- ❌ `MOCK_PERFIL`
- ❌ `MOCK_PLAN_ENTRENAMIENTO`
- ❌ `MOCK_PLAN_NUTRICION`
- ❌ `MOCK_SOLICITUD_CHECKIN`
- ❌ `MOCK_HISTORIAL_PESOS`
- ❌ `MOCK_NOTAS_NUTRICIONISTA`
- ❌ `MOCK_HILOS` y `MOCK_HILOS_COMIDA`
- ❌ `MOCK_CHAT_ENTRENADOR` y `MOCK_CHAT_NUTRICIONISTA`

### 4. **Nuevo Método Agregado**

```typescript
conectarConEntrenador(codigo: string): Observable<void> {
  return this.http.post<ApiResponseDTO<void>>(`${this.API}/conectar`, { codigo }, { withCredentials: true })
    .pipe(map(() => undefined));
}
```

Este método conecta al atleta con un profesional usando un código de invitación.

## 🔧 Uso en el Componente

### Ejemplo: Obtener perfil del atleta

```typescript
// En dashboard-atleta.ts - ya implementado en ngOnInit()
this.atleta.getPerfil().subscribe(p => {
  this.perfilAtleta.set(p);
  console.log('Perfil cargado:', p);
});
```

### Ejemplo: Conectar con entrenador (TODO)

En `dashboard-atleta.ts`, el método `conectarConCodigo()` debe ser actualizado:

**Antes (sin conexión real):**
```typescript
conectarConCodigo(): void {
  // ... validaciones ...
  this.enviandoCodigo.set(true);
  this.codigoError.set('');
  this.codigoExito.set(false);
  
  // TODO: conectar con POST /api/v1/atleta/conectar { codigo }
  setTimeout(() => {
    this.enviandoCodigo.set(false);
    this.codigoError.set('Código no válido o ya utilizado.');
  }, 800);
}
```

**Después (con conexión real):**
```typescript
conectarConCodigo(): void {
  const codigo = this.codigoEntrenador().trim().toUpperCase();
  if (!codigo) return;
  
  this.enviandoCodigo.set(true);
  this.codigoError.set('');
  this.codigoExito.set(false);
  
  this.atleta.conectarConEntrenador(codigo).subscribe({
    next: () => {
      this.codigoExito.set(true);
      this.codigoEntrenador.set('');
      this.enviandoCodigo.set(false);
      // Recargar profesionales
      this.atleta.getProfesionalesAsignados().subscribe(p => {
        this.profesionales.set(p);
      });
    },
    error: (err) => {
      this.codigoError.set('Código no válido o ya utilizado.');
      this.enviandoCodigo.set(false);
    }
  });
}
```

## 📝 Notas Técnicas

### Autenticación
- ✅ Todos los endpoints tienen `{ withCredentials: true }`
- ✅ Las cookies de sesión se envían automáticamente
- ✅ El backend debe validar el usuario autenticado

### Extracción de Datos
- ✅ Se usa `.pipe(map(response => response.data))` para extraer los datos
- ✅ Los nulls se manejan con `|| null` o `|| []` según corresponda
- ✅ Las respuestas siguen siempre la estructura `ApiResponseDTO<T>`

### Manejo de Errores
- ✅ El componente implementa `subscribe({ next: ..., error: ... })`
- ✅ Se pueden agregar interceptores para manejo global de errores
- ✅ Los errores HTTP se devuelven al componente con el estado original

### Uploads (Multipart)
- ✅ Foto de perfil: `subirFotoPerfil(archivo: File)`
- ✅ Chat adjuntos: `subirArchivoChat(tipo: 'entrenador'|'nutricionista', archivo: File)`
- ✅ Media hilos: `subirMediaHilo(sesionDia: string, ejercicioNombre: string, archivo: File)`

## 🚀 Próximos Pasos

1. **Asegurar que el Backend implementa todos los endpoints**
   - Verificar que todos los controladores de `AtletaController` estén implementados
   - Asegurar que los servicios (AtletaService, VinculacionService, PesoService, etc.) existan

2. **Configurar CORS en el Backend**
   ```java
   @Bean
   public CorsConfigurationSource corsConfigurationSource() {
       CorsConfiguration configuration = new CorsConfiguration();
       configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
       configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
       configuration.setAllowCredentials(true);
       configuration.setAllowedHeaders(Arrays.asList("*"));
       // ...
   }
   ```

3. **Actualizar el método `conectarConCodigo()` en el componente**
   - Implementar la llamada real a `conectarConEntrenador()`
   - Recargar la lista de profesionales después de una conexión exitosa

4. **Probar todos los endpoints**
   - Usar Postman o Thunder Client
   - Verificar que las respuestas coinciden con la estructura esperada
   - Validar los códigos HTTP (201 para POST, 200 para GET, etc.)

5. **Agregar Interceptores HTTP (Opcional)**
   ```typescript
   // Para auto-refresh de tokens, logging, etc.
   @Injectable()
   export class AtletaInterceptor implements HttpInterceptor {
     intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
       // Agregar lógica aquí
       return next.handle(req);
     }
   }
   ```

## 📚 Archivos Modificados

- `frontend/src/app/features/dashboard-atleta/services/atleta.service.ts` - ✅ Actualizado
- `frontend/src/app/features/dashboard-atleta/services/CONTRATO_API.md` - ✨ Nuevo
- `frontend/src/app/features/dashboard-atleta/services/GUIA_INTEGRACION.md` - ✨ Este archivo

## 🎯 Checklist de Validación

Antes de publicar a producción:

- [ ] Todos los endpoints del backend están implementados
- [ ] Las respuestas HTTP coinciden con `ApiResponseDTO<T>`
- [ ] Los campos en los DTOs coinciden con las interfaces TypeScript
- [ ] La autenticación funciona (cookies/tokens se envían correctamente)
- [ ] Los uploads de archivos funcionan (foto, chat, media)
- [ ] Los errores HTTP se manejan correctamente
- [ ] Se validó con Postman primero
- [ ] Se probó end-to-end en el navegador
- [ ] CORS está configurado correctamente en el backend
- [ ] Las cookies SameSite están configuradas correctamente

## ❓ Preguntas Frecuentes

**P: ¿Por qué se removieron los mocks?**
A: Porque ahora el servicio realiza llamadas HTTP reales al backend. Si necesitas mocks para desarrollo sin backend, usa un `HttpTestingController` en tus tests.

**P: ¿Cómo puedo interceder las llamadas HTTP?**
A: Crea un `HttpInterceptor` e inyéctalo en la configuración del módulo HTTP.

**P: ¿Qué pasa si el backend aún no está listo?**
A: Puedes:
1. Usar [json-server](https://github.com/typicode/json-server) para un backend mock
2. Crear un servicio decorador que devuelva mocks según una variable de entorno
3. Usar [ng-mock-server](https://www.npmjs.com/package/ng-mock-server)

**P: ¿Cómo manejo errores de red?**
A: El componente ya tiene `subscribe({ error: ... })`. Puedes agregar un interceptor HTTP para manejo global:

```typescript
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError(error => {
        console.error('Error HTTP:', error);
        // Mostrar snackbar, redirigir a login, etc.
        return throwError(() => error);
      })
    );
  }
}
```

