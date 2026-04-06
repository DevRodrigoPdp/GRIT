import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';

// ── Tipos compartidos ────────────────────────────────────────────────────────

export type Rol            = 'ATLETA' | 'ENTRENADOR';
export type EstadoCuenta   = 'ACTIVO' | 'PENDIENTE_REVISION' | 'RECHAZADO';
export type ServicioAtleta = 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';

// ── Payloads de registro ─────────────────────────────────────────────────────

export interface RegistroAtletaPayload {
  nombre:   string;
  email:    string;
  password: string;
  fechaNac: string;
  genero:   string;
  pesoKg:   number;
  alturaCm: number;
  deporte:  string;
  nivel:    string;
  servicio: ServicioAtleta;
  objetivo: string | null;
}

export interface RegistroEntrenadorPayload {
  username:   string;
  email:    string;
  password: string;
}

// ── Respuestas del backend ───────────────────────────────────────────────────

export interface LoginResponse {
  ok: boolean;
  data: {
    rol:                  Rol;
    estado:               EstadoCuenta;
    tituloEntrenamiento:  boolean | null;
    tituloNutricion:      boolean | null;
    servicio:             ServicioAtleta | null;
    nombre:               string;
  };
}

export interface RegistroResponse {
  ok:       true;
  message:  string;
  data: {
    id:    string;
    rol:   Rol;
    estado: EstadoCuenta;
  };
}

export interface MeResponse {
  ok: boolean;
  data: {
    id:                      string;
    nombre:                  string;
    rol:                     Rol;
    estado:                  EstadoCuenta;
    servicio:                ServicioAtleta | null;
    tituloEntrenamiento:     boolean | null;
    tituloNutricion:         boolean | null;
    titulacionEntrenamiento: string | null;
    titulacionNutricion:     string | null;
  };
}

// ── Servicio ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http   = inject(HttpClient);
  private router = inject(Router);

  private readonly API = 'http://localhost:8080/api/v1/auth';

  // Signals de sesión (única fuente de verdad en el frontend)
  readonly rol                 = signal<Rol | null>(null);
  readonly estado              = signal<EstadoCuenta | null>(null);
  readonly tituloEntrenamiento = signal<boolean | null>(null);
  readonly tituloNutricion     = signal<boolean | null>(null);
  readonly servicio            = signal<ServicioAtleta | null>(null);
  readonly nombre              = signal<string | null>(null);
  readonly loginError          = signal<string | null>(null);
  readonly loading             = signal(false);

  // ── Registro atleta ──────────────────────────────────────────────────────

  /**
   * Registra un nuevo atleta.
   * Normaliza los valores del formulario (lowercase → UPPERCASE) antes de enviar.
   * TODO: descomentar llamada real y eliminar bloque mock cuando haya backend.
   */
  registroAtleta(payload: RegistroAtletaPayload): Observable<RegistroResponse> {
    return this.http
      .post<RegistroResponse>(`${this.API}/registro/atleta`, payload, { withCredentials: true })
      .pipe(
        tap({
          next: (res) => {
            const data = res.data;
            this.setSession(
              data.rol,
              data.estado,
              null,
              null,
              payload.servicio as ServicioAtleta,
              payload.nombre
            );
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
   * TODO: descomentar llamada real y eliminar bloque mock cuando haya backend.
   */
  // registroEntrenador(payload: RegistroEntrenadorPayload): void {
  //   this.loading.set(true);
  //   const formData = new FormData();
  //   formData.append('nombre', payload.nombre);
  //   formData.append('correo', payload.correo);
  //   if (payload.codigoProfesional) {
  //     formData.append('codigoProfesional', payload.codigoProfesional);
  //   }
  //   if (payload.titulacionEntrenamiento) {
  //     formData.append('titulacionEntrenamiento', payload.titulacionEntrenamiento);
  //   }
  //   if (payload.titulacionNutricion) {
  //     formData.append('titulacionNutricion', payload.titulacionNutricion);
  //   }
  //   payload.documentos.forEach((file, index) => {
  //     formData.append('documentos', file);
  //   });

  //   this.http
  //     .post<RegistroResponse>(`${this.API}/register`, formData, { withCredentials: true })
  //     .pipe(
  //       tap({
  //         next: (res) => {
  //           this.loading.set(false);
  //           this.setSession(res.data.rol, res.data.estado, res.data.tituloEntrenamiento, res.data.tituloNutricion, res.data.servicio, res.data.nombre);
  //           this.redirigir(res.data.rol, res.data.estado, res.data.tituloEntrenamiento, res.data.tituloNutricion);
  //         },
  //         error: () => this.loading.set(false),
  //       })
  //     )
  //     .subscribe();
  // }

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
            this.setSession(res.data.rol, res.data.estado, res.data.tituloEntrenamiento, res.data.tituloNutricion, res.data.servicio, res.data.nombre);
            this.redirigir(res.data.rol, res.data.estado, res.data.tituloEntrenamiento, res.data.tituloNutricion);
          },
          error: (err) => {
            this.loading.set(false);
            if (err.status === 401) {
              this.loginError.set('Correo o contraseña incorrectos.');
            } else if (err.status === 403) {
              this.loginError.set('Tu cuenta ha sido rechazada. Contacta con soporte.');
            } else {
              this.loginError.set('Error de conexión. Inténtalo de nuevo.');
            }
          },
        })
      );
  }

  // ── Me (restaurar sesión tras recarga) ───────────────────────────────────

  /**
   * Llama a GET /api/v1/auth/me usando la cookie HttpOnly existente.
   * Usar en ngOnInit del dashboard para restaurar la sesión tras F5.
   * TODO: descomentar llamada real y eliminar bloque mock cuando haya backend.
   */
  me(): Observable<MeResponse | null> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http
    //   .get<MeResponse>(`${this.API}/me`, { withCredentials: true })
    //   .pipe(
    //     tap((res) => {
    //       this.setSession(res.data.rol, res.data.estado, res.data.tituloEntrenamiento, res.data.tituloNutricion, res.data.servicio, res.data.nombre);
    //     })
    //   );
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(null); // los signals ya tienen datos del registro/login
  }

  // ── Logout ───────────────────────────────────────────────────────────────

  logout() {
    // ── REAL ──────────────────────────────────────────────────────────────
    // this.http
    //   .post(`${this.API}/logout`, {}, { withCredentials: true })
    //   .subscribe(() => this.clearSession());
    // ── MOCK ──────────────────────────────────────────────────────────────
    this.clearSession();
    this.router.navigate(['/']);
  }

  // ── Helpers privados ─────────────────────────────────────────────────────

  private setSession(
    rol: Rol,
    estado: EstadoCuenta,
    tituloEntrenamiento: boolean | null,
    tituloNutricion: boolean | null,
    servicio: ServicioAtleta | null,
    nombre: string
  ) {
    this.rol.set(rol);
    this.estado.set(estado);
    this.tituloEntrenamiento.set(tituloEntrenamiento);
    this.tituloNutricion.set(tituloNutricion);
    this.servicio.set(servicio);
    this.nombre.set(nombre);
  }

  private clearSession() {
    this.rol.set(null);
    this.estado.set(null);
    this.tituloEntrenamiento.set(null);
    this.tituloNutricion.set(null);
    this.servicio.set(null);
    this.nombre.set(null);
  }

  private redirigir(
    rol: Rol,
    estado: EstadoCuenta,
    tituloEntrenamiento: boolean | null,
    tituloNutricion: boolean | null
  ) {
    if (estado === 'PENDIENTE_REVISION') {
      this.router.navigate(['/pendiente']);
      return;
    }
    if (estado === 'RECHAZADO') {
      this.loginError.set('Tu cuenta ha sido rechazada. Contacta con soporte.');
      return;
    }
    if (rol === 'ATLETA') {
      this.router.navigate(['/dashboard/atleta']);
      return;
    }
    if (rol === 'ENTRENADOR') {
      if (tituloEntrenamiento && tituloNutricion) {
        this.router.navigate(['/dashboard/entrenador/nutricion']);
      } else if (tituloNutricion) {
        this.router.navigate(['/dashboard/entrenador/solo-nutricion']);
      } else {
        this.router.navigate(['/dashboard/entrenador']);
      }
    }
  }
}
