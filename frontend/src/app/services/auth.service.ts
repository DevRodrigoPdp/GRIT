import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';

export type Rol = 'ATLETA' | 'ENTRENADOR';
export type EstadoCuenta = 'ACTIVO' | 'PENDIENTE_REVISION' | 'RECHAZADO';
export type ServicioAtleta = 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';

export interface LoginResponse {
  ok: boolean;
  data: {
    rol: Rol;
    estado: EstadoCuenta;
    tituloNutricion: boolean | null;
    servicio: ServicioAtleta | null;   // solo para atletas
    nombre: string;
  };
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API = '/api/v1/auth';

  readonly rol = signal<Rol | null>(null);
  readonly estado = signal<EstadoCuenta | null>(null);
  readonly tituloNutricion = signal<boolean | null>(null);
  readonly servicio = signal<ServicioAtleta | null>(null);
  readonly nombre = signal<string | null>(null);
  readonly loginError = signal<string | null>(null);
  readonly loading = signal(false);

  // ── Mock temporal hasta conectar backend ──────────────────
  mockRegistroAtleta(datos: { nombre: string; servicio: ServicioAtleta }) {
    this.rol.set('ATLETA');
    this.estado.set('ACTIVO');
    this.nombre.set(datos.nombre);
    this.servicio.set(datos.servicio);
    this.tituloNutricion.set(null);
    this.router.navigate(['/dashboard/atleta']);
  }
  // ─────────────────────────────────────────────────────────

  login(correo: string, password: string) {
    this.loading.set(true);
    this.loginError.set(null);

    return this.http
      .post<LoginResponse>(`${this.API}/login`, { correo, password }, { withCredentials: true })
      .pipe(
        tap({
          next: (res) => {
            this.rol.set(res.data.rol);
            this.estado.set(res.data.estado);
            this.tituloNutricion.set(res.data.tituloNutricion);
            this.servicio.set(res.data.servicio);
            this.nombre.set(res.data.nombre);
            this.loading.set(false);
            this.redirigir(res.data.rol, res.data.estado, res.data.tituloNutricion);
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

  logout() {
    this.http
      .post(`${this.API}/logout`, {}, { withCredentials: true })
      .subscribe(() => {
        this.rol.set(null);
        this.estado.set(null);
        this.tituloNutricion.set(null);
        this.servicio.set(null);
        this.nombre.set(null);
        this.router.navigate(['/']);
      });
  }

  private redirigir(rol: Rol, estado: EstadoCuenta, tituloNutricion: boolean | null) {
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
      this.router.navigate(
        tituloNutricion
          ? ['/dashboard/entrenador/nutricion']
          : ['/dashboard/entrenador']
      );
    }
  }
}
