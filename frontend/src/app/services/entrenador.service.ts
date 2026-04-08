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
  /** Servicios contratados por el atleta con este entrenador */
  servicio: 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';
  tienePlanActivo: boolean;
}

// ── Mocks ────────────────────────────────────────────────────────────────────

const MOCK_PERFIL: PerfilEntrenador = {
  id: 'mock-uuid-entrenador',
  nombre: 'Entrenador Demo',
  correo: 'entrenador@demo.com',
  titulacionEntrenamiento: 'GRADO_CAFYD',
  titulacionNutricion: 'GRADO_NUTRICION_DIETETICA',
  experienciaAnos: 5,
  descripcion: 'Especialista en rendimiento deportivo.',
  estado: 'ACTIVO',
};

const MOCK_ATLETAS: AtletaAsignado[] = [
  { id: 'atleta-1', nombre: 'Carlos Ruiz',     deporte: 'Fútbol',    nivel: 'AVANZADO',    servicio: 'AMBOS',          tienePlanActivo: true  },
  { id: 'atleta-2', nombre: 'Laura Sánchez',   deporte: 'CrossFit',  nivel: 'INTERMEDIO',  servicio: 'ENTRENAMIENTO',  tienePlanActivo: true  },
  { id: 'atleta-3', nombre: 'Marcos Ibáñez',   deporte: 'Natación',  nivel: 'ELITE',       servicio: 'NUTRICION',      tienePlanActivo: false },
  { id: 'atleta-4', nombre: 'Sara Molina',     deporte: 'Atletismo', nivel: 'PRINCIPIANTE',servicio: 'AMBOS',          tienePlanActivo: false },
];

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
  getMisAtletas(): Observable<AtletaAsignado[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<AtletaAsignado[]>(`${this.API}/atletas`, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(MOCK_ATLETAS);
  }
}
