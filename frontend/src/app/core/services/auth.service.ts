import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { setLoggingOut } from '../interceptors/auth.interceptor';

// ── Tipos compartidos ────────────────────────────────────────────────────────

export type Rol = 'ATLETA' | 'ENTRENADOR' | 'ADMIN';
export type EstadoCuenta = 'ACTIVO' | 'PENDIENTE_REVISION' | 'RECHAZADO' | 'BLOQUEADO';
export type EstadoRevision = 'PENDIENTE_REVISION' | 'APROBADO' | 'RECHAZADO';
export type ServicioAtleta = 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';

// ── Payloads de registro ─────────────────────────────────────────────────────

export interface RegistroAtletaPayload {
  nombre: string;
  email: string;
  password: string;
  fechaNac: string;
  genero: string;
  pesoKg: number;
  alturaCm: number;
  deporte: string;
  nivel: string;
  servicio: ServicioAtleta;
  objetivo: string | null;
  codigoInvitacion: string | null;
  tipoProfesionalCodigo: string | null;
  restriccionesDieteticas: string;
  restriccionesFisicas: string;
  fotoPerfil: File | null;
}

export interface RegistroEntrenadorPayload {
  nombre: string;
  email: string;
  password: string;
  codigoProfesional?: string | null;
  titulacionEntrenamiento?: string | null;
  titulacionNutricion?: string | null;
  anosExperiencia?: number | null;
  sobreMi?: string | null;
  masters?: string[];
  fotoPerfil: File | null;
  certificaciones: File[];
}

// ── Respuestas del backend ───────────────────────────────────────────────────

export interface LoginResponse {
  ok: boolean;
  data: {
    rol: Rol;
    estado: EstadoCuenta;
    estadoRevision: EstadoRevision | null;
    tituloEntrenamiento: boolean | null;
    tituloNutricion: boolean | null;
    servicio: ServicioAtleta | null;
    nombre: string;
  };
}

export interface RegistroResponse {
  ok: true;
  message: string;
  data: {
    id: string;
    rol: Rol;
    estado: EstadoCuenta;
  };
}

export interface MeResponse {
  ok: boolean;
  data: {
    id: string;
    nombre: string;
    rol: Rol;
    estado: EstadoCuenta;
    estadoRevision: EstadoRevision | null;
    servicio: ServicioAtleta | null;
    tituloEntrenamiento: boolean | null;
    tituloNutricion: boolean | null;
    titulacionEntrenamiento: string | null;
    titulacionNutricion: string | null;
  };
}

// ── Servicio ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API = '/api/v1/auth';

  // Signals de sesión (única fuente de verdad en el frontend)
  readonly rol = signal<Rol | null>(null);
  readonly estado = signal<EstadoCuenta | null>(null);
  readonly estadoRevision = signal<EstadoRevision | null>(null);
  readonly tituloEntrenamiento = signal<boolean | null>(null);
  readonly tituloNutricion = signal<boolean | null>(null);
  readonly servicio = signal<ServicioAtleta | null>(null);
  readonly nombre = signal<string | null>(null);
  readonly loginError = signal<string | null>(null);
  readonly loading = signal(false);

  readonly sessionInitialized = signal(false);
  // true solo cuando /me confirmó la sesión (no mero localStorage)
  readonly sessionVerified = signal(false);

  constructor() {
    const saved = localStorage.getItem('grit_session');
    if (saved) {
      try {
        const data = JSON.parse(saved);
        this.setSession(data.rol, data.estado, data.estadoRevision ?? null, data.tituloEntrenamiento, data.tituloNutricion, data.servicio, data.nombre);
      } catch {
        localStorage.removeItem('grit_session');
      }
    }
  }

  // ── Registro atleta ──────────────────────────────────────────────────────

  /**
   * Registra un nuevo atleta.
   * Normaliza los valores del formulario (lowercase → UPPERCASE) antes de enviar.
   */
  registroAtleta(payload: RegistroAtletaPayload): Observable<RegistroResponse> {
    const formData = new FormData();

    const dto = {
      nombre: payload.nombre,
      email: payload.email,
      password: payload.password,
      fechaNac: payload.fechaNac,
      genero: payload.genero,
      pesoKg: payload.pesoKg,
      alturaCm: payload.alturaCm,
      deporte: payload.deporte,
      nivel: payload.nivel,
      servicio: payload.servicio,
      objetivo: payload.objetivo,
      codigoInvitacion: payload.codigoInvitacion,
      tipoProfesionalCodigo: payload.tipoProfesionalCodigo,
      restriccionesDieteticas: payload.restriccionesDieteticas,
      restriccionesFisicas: payload.restriccionesFisicas,
    };

    formData.append('datos', new Blob([JSON.stringify(dto)], { type: 'application/json' }));

    if (payload.fotoPerfil) {
      formData.append('fotoPerfil', payload.fotoPerfil);
    }

    return this.http
      .post<RegistroResponse>(`${this.API}/registro/atleta`, formData, { withCredentials: true })
      .pipe(
        tap({
          next: (res) => {
            const data = res.data;
            this.setSession(data.rol, data.estado, null, null, null, payload.servicio, payload.nombre);
            this.sessionVerified.set(true);
            this.redirigir(data.rol, data.estado, null, null);
          },
        })
      );
  }

  // ── Registro entrenador ──────────────────────────────────────────────────

  /**
   * Registra un nuevo entrenador.
   * El estado inicial es PENDIENTE_REVISION hasta que un admin lo apruebe.
   * Las titulaciones determinan a qué dashboard irá una vez aprobado.
   *   - Solo entrenamiento → /dashboard/entrenador
   *   - Solo nutrición     → /dashboard/entrenador/solo-nutricion
   *   - Ambas              → /dashboard/entrenador/nutricion
   */
  registroEntrenador(payload: RegistroEntrenadorPayload): Observable<RegistroResponse> {
    this.loading.set(true);
    const formData = new FormData();

    // ── Construir DTO para la parte "datos" ─────────────────────────────────
    const dto: any = {
      nombre: payload.nombre,
      email: payload.email,
      password: payload.password,
      codigoProfesional: payload.codigoProfesional || null,
      experienciaAnos: payload.anosExperiencia || null,
      masters: payload.masters || null,
      descripcion: payload.sobreMi || null,
    };

    if (payload.titulacionEntrenamiento) {
      dto.titulacionEntrenamiento = payload.titulacionEntrenamiento;
    }
    if (payload.titulacionNutricion) {
      dto.titulacionNutricion = payload.titulacionNutricion;
    }

    // Enviar DTO como JSON en la parte "datos"
    formData.append('datos', new Blob([JSON.stringify(dto)], { type: 'application/json' }));

    // Enviar foto en la parte "fotoPerfil"
    if (payload.fotoPerfil) {
      formData.append('fotoPerfil', payload.fotoPerfil);
    }

    // Enviar certificaciones en la parte "certificaciones"
    payload.certificaciones.forEach((file) => {
      formData.append('certificaciones', file);
    });

    return this.http
      .post<RegistroResponse>(`${this.API}/registro/entrenador`, formData, { withCredentials: true })
      .pipe(
        tap({
          next: (res) => {
            this.loading.set(false);
            const tituloEntrenamiento = !!payload.titulacionEntrenamiento;
            const tituloNutricion = !!payload.titulacionNutricion;
            this.setSession(res.data.rol, res.data.estado, null, tituloEntrenamiento, tituloNutricion, null, payload.nombre);
            this.sessionVerified.set(true);
            this.redirigir(res.data.rol, res.data.estado, tituloEntrenamiento, tituloNutricion);
          },
          error: () => this.loading.set(false),
        })
      );
  }

  // ── Login ────────────────────────────────────────────────────────────────

  login(correo: string, password: string) {
    this.loading.set(true);
    this.loginError.set(null);

    return this.http
      .post<LoginResponse>(`${this.API}/login`, { email: correo, password }, { withCredentials: true })
      .pipe(
        tap({
          next: (res) => {
            this.loading.set(false);
            const estadosValidos: EstadoCuenta[] = ['ACTIVO', 'PENDIENTE_REVISION', 'RECHAZADO'];
            const estado = res?.data?.estado;
            const rol = res?.data?.rol;

            // Estado válido → flujo normal
            if (rol && estadosValidos.includes(estado)) {
              const rev = res.data.estadoRevision ?? null;
              this.setSession(rol, estado, rev, res.data.tituloEntrenamiento, res.data.tituloNutricion, res.data.servicio, res.data.nombre);
              this.sessionVerified.set(true);
              this.redirigir(rol, estado, res.data.tituloEntrenamiento, res.data.tituloNutricion);
              return;
            }

            // Backend devolvió datos incompletos/incorrectos → confirmar con /me
            this.me().subscribe(() => {
              if (!this.rol()) {
                this.loginError.set('error_servidor');
                return;
              }
              if (this.estado() === 'PENDIENTE_REVISION') {
                this.loginError.set('cuenta_pendiente');
                return;
              }
              this.redirigir(this.rol()!, this.estado()!, this.tituloEntrenamiento(), this.tituloNutricion());
            });
          },
          error: (err) => {
            this.loading.set(false);
            const msg: string = (err.error?.message ?? err.error?.error ?? '').toLowerCase();
            if (err.status === 401) {
              if (msg.includes('inactivo') || msg.includes('bloqueado')) {
                this.loginError.set('usuario_bloqueado');
              } else {
                this.loginError.set('credenciales_invalidas');
              }
            } else if (err.status === 403) {
              if (msg.includes('rechazado')) {
                this.loginError.set('cuenta_rechazada');
              } else {
                this.loginError.set('usuario_bloqueado');
              }
            } else {
              this.loginError.set('error_servidor');
            }
          },
        })
      );
  }

  // ── Inicialización de sesión ────────────────────────────────────────────

  /**
   * Restaura la sesión desde:
   * 1. Cookie HttpOnly (preferido)
   * 2. localStorage (fallback)
   * Se ejecuta automáticamente vía APP_INITIALIZER.
   */
  initSession(): Observable<void> {
    return this.http
      .get<MeResponse>(`${this.API}/me`, { withCredentials: true })
      .pipe(
        tap((res) => {
          this.setSession(res.data.rol, res.data.estado, res.data.estadoRevision ?? null,
            res.data.tituloEntrenamiento, res.data.tituloNutricion,
            res.data.servicio, res.data.nombre);
          this.sessionVerified.set(true);
          this.sessionInitialized.set(true);
        }),
        map(() => void 0),
        catchError(() => {
          this.clearSession();
          this.sessionInitialized.set(true);
          return of(void 0);
        })
      );
  }


  // ── Me (restaurar sesión tras recarga) ───────────────────────────────────

  /**
   * Llama a GET /api/v1/auth/me usando la cookie HttpOnly existente.
   * Solo se usa desde componentes (no redirige, deja que el guard decida).
   */
  me(): Observable<MeResponse | null> {
    return this.http
      .get<MeResponse>(`${this.API}/me`, { withCredentials: true })
      .pipe(
        tap((res) => {
          this.setSession(res.data.rol, res.data.estado, res.data.estadoRevision ?? null,
            res.data.tituloEntrenamiento, res.data.tituloNutricion,
            res.data.servicio, res.data.nombre);
          this.sessionVerified.set(true);
        }),
        catchError(() => {
          this.clearSession();
          return of(null);
        })
      );
  }

  // ── Logout ───────────────────────────────────────────────────────────────

  logout() {
    setLoggingOut(true);
    this.http
      .post(`${this.API}/logout`, {}, { withCredentials: true })
      .pipe(catchError(() => EMPTY))
      .subscribe(() => {
        setLoggingOut(false);
        this.clearSession();
        this.router.navigate(['/']);
      });
  }

  // ── Helpers privados ─────────────────────────────────────────────────────

  private setSession(
    rol: Rol,
    estado: EstadoCuenta,
    estadoRevision: EstadoRevision | null,
    tituloEntrenamiento: boolean | null,
    tituloNutricion: boolean | null,
    servicio: ServicioAtleta | null,
    nombre: string
  ) {
    this.rol.set(rol);
    this.estado.set(estado);
    this.estadoRevision.set(estadoRevision);
    this.tituloEntrenamiento.set(tituloEntrenamiento);
    this.tituloNutricion.set(tituloNutricion);
    this.servicio.set(servicio);
    this.nombre.set(nombre);
    this.guardarSesionLocal();
  }

  private clearSession() {
    this.rol.set(null);
    this.estado.set(null);
    this.estadoRevision.set(null);
    this.tituloEntrenamiento.set(null);
    this.tituloNutricion.set(null);
    this.servicio.set(null);
    this.nombre.set(null);
    this.sessionVerified.set(false);
    localStorage.removeItem('grit_session');
  }

  private guardarSesionLocal() {
    localStorage.setItem('grit_session', JSON.stringify({
      rol: this.rol(),
      estado: this.estado(),
      estadoRevision: this.estadoRevision(),
      tituloEntrenamiento: this.tituloEntrenamiento(),
      tituloNutricion: this.tituloNutricion(),
      servicio: this.servicio(),
      nombre: this.nombre(),
    }));
  }

  private redirigir(
    rol: Rol,
    estado: EstadoCuenta,
    _tituloEntrenamiento: boolean | null,
    _tituloNutricion: boolean | null
  ) {
    if (rol === 'ATLETA') {
      this.router.navigate(['/dashboard/atleta']);
      return;
    }
    if (rol === 'ENTRENADOR') {
      const rev = this.estadoRevision();
      // estadoRevision es la fuente de verdad; si no viene, fallback a estado
      const pendiente = rev !== null ? rev !== 'APROBADO' : estado !== 'ACTIVO';
      this.router.navigate(pendiente ? ['/pendiente'] : ['/dashboard/entrenador']);
      return;
    }
    if (rol === 'ADMIN') {
      this.router.navigate(['/admin']);
    }
  }
}
