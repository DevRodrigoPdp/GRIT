import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { AuthService } from './auth.service';

// ── Tipos de respuesta del backend ─────────────────────────────────────────

export interface PerfilEntrenador {
  id: string;
  nombre: string;
  correo: string;
  titulacionEntrenamiento: 'GRADO_CAFYD' | 'TSAF_TSEAS' | 'CERT_AFDA0210' | null;
  titulacionNutricion: 'GRADO_NUTRICION_DIETETICA' | 'TSD' | null;
  experienciaAnos: number;
  descripcion: string;
  estado: 'ACTIVO' | 'PENDIENTE_REVISION' | 'RECHAZADO';
}

export interface AtletaAsignado {
  id: string;
  nombre: string;
  deporte: string;
  nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | 'ELITE';
  servicio: 'ENTRENAMIENTO' | 'NUTRICION'; // el servicio que cubre este entrenador
  tienePlanActivo: boolean;
}

// ── Mocks ────────────────────────────────────────────────────────────────────

const MOCK_PERFIL: PerfilEntrenador = {
  id: 'mock-uuid-entrenador',
  nombre: 'Entrenador Demo',
  correo: 'entrenador@demo.com',
  titulacionEntrenamiento: 'GRADO_CAFYD',
  titulacionNutricion: null,
  experienciaAnos: 5,
  descripcion: 'Especialista en rendimiento deportivo.',
  estado: 'ACTIVO',
};

// ── Servicio ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class EntrenadorService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private readonly API = '/api/v1/entrenador';

  /**
   * Devuelve el perfil completo del entrenador autenticado.
   * TODO: descomentar llamada real cuando haya backend.
   */
  getPerfil(): Observable<PerfilEntrenador> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<PerfilEntrenador>(`${this.API}/perfil`, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of({
      ...MOCK_PERFIL,
      nombre: this.auth.nombre() ?? MOCK_PERFIL.nombre,
    });
  }

  /**
   * Devuelve la lista de atletas asignados al entrenador.
   * Filtra por servicio: 'ENTRENAMIENTO' | 'NUTRICION' | undefined (todos)
   * TODO: descomentar llamada real cuando haya backend.
   */
  getMisAtletas(servicio?: 'ENTRENAMIENTO' | 'NUTRICION'): Observable<AtletaAsignado[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // const params = servicio ? `?servicio=${servicio}` : '';
    // return this.http.get<AtletaAsignado[]>(
    //   `${this.API}/atletas${params}`,
    //   { withCredentials: true }
    // );
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of([]); // sin atletas asignados por defecto
  }
}
