import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';

export type Rol = 'ATLETA' | 'ENTRENADOR';
export type EstadoCuenta = 'ACTIVO' | 'PENDIENTE_REVISION' | 'RECHAZADO';

export interface LoginResponse {
  ok: boolean;
  data: {
    rol: Rol;
    estado: EstadoCuenta;
  };
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API = '/api/v1/auth';

  // Estado reactivo accesible desde cualquier componente
  readonly rol = signal<Rol | null>(null);
  readonly estado = signal<EstadoCuenta | null>(null);
  readonly loginError = signal<string | null>(null);
  readonly loading = signal(false);

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
            this.loading.set(false);
            this.redirigir(res.data.rol, res.data.estado);
          },
          error: (err) => {
            this.loading.set(false);
            if (err.status === 401) {
              this.loginError.set('Correo o contraseña incorrectos.');
            } else {
              this.loginError.set('Error de conexión. Inténtalo de nuevo.');
            }
          },
        })
      );
  }

  logout() {
    this.http
      .post('/api/v1/auth/logout', {}, { withCredentials: true })
      .subscribe(() => {
        this.rol.set(null);
        this.estado.set(null);
        this.router.navigate(['/']);
      });
  }

  private redirigir(rol: Rol, estado: EstadoCuenta) {
    if (rol === 'ATLETA' && estado === 'ACTIVO') {
      this.router.navigate(['/dashboard/atleta']);
    } else if (rol === 'ENTRENADOR' && estado === 'ACTIVO') {
      this.router.navigate(['/dashboard/entrenador']);
    } else if (estado === 'PENDIENTE_REVISION') {
      this.router.navigate(['/pendiente']);
    } else {
      this.router.navigate(['/']);
    }
  }
}
