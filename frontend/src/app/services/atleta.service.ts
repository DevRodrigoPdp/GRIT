import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { AuthService, RegistroAtletaPayload } from './auth.service';

// ── Tipos de respuesta del backend ─────────────────────────────────────────

export interface PerfilAtleta {
  id: string;
  nombre: string;
  correo: string;
  fechaNac: string;
  genero: 'HOMBRE' | 'MUJER' | 'OTRO';
  peso: number;
  altura: number;
  deporte: string;
  nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | 'ELITE';
  servicio: 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';
  objetivo: 'RENDIMIENTO' | 'MASA_MUSCULAR' | 'PERDER_PESO' | 'SALUD' | 'RESISTENCIA' | null;
}

export interface EntrenadorAsignado {
  id: string;
  nombre: string;
  titulacion: string;
  servicio: 'ENTRENAMIENTO' | 'NUTRICION';
}

export interface PlanEntrenamiento {
  id: string;
  nombre: string;
  descripcion: string;
  semanas: number;
  sesiones: {
    dia: string;
    ejercicios: { nombre: string; series: number; reps: string }[];
  }[];
}

export interface PlanNutricion {
  id: string;
  nombre: string;
  descripcion: string;
  kcalDiarias: number;
  comidas: {
    nombre: string;
    alimentos: { nombre: string; cantidad: string }[];
  }[];
}

// ── Mocks ────────────────────────────────────────────────────────────────────

const MOCK_PERFIL: PerfilAtleta = {
  id: 'mock-uuid-atleta',
  nombre: 'Atleta Demo',
  correo: 'atleta@demo.com',
  fechaNac: '1998-05-14',
  genero: 'HOMBRE',
  peso: 80,
  altura: 180,
  deporte: 'Ciclismo',
  nivel: 'AVANZADO',
  servicio: 'AMBOS',
  objetivo: 'RENDIMIENTO',
};

const MOCK_ENTRENADOR: EntrenadorAsignado = {
  id: 'mock-uuid-entrenador',
  nombre: 'Sin entrenador asignado',
  titulacion: '',
  servicio: 'ENTRENAMIENTO',
};

const MOCK_PLAN_ENTRENAMIENTO: PlanEntrenamiento = {
  id: 'mock-plan-1',
  nombre: 'Plan base — pendiente de asignación',
  descripcion: 'Tu entrenador aún no ha creado tu plan.',
  semanas: 0,
  sesiones: [],
};

const MOCK_PLAN_NUTRICION: PlanNutricion = {
  id: 'mock-nutri-1',
  nombre: 'Plan nutricional — pendiente de asignación',
  descripcion: 'Tu dietista aún no ha creado tu plan.',
  kcalDiarias: 0,
  comidas: [],
};

// ── Servicio ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class AtletaService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private readonly API = '/api/v1/atleta';

  /**
   * Devuelve el perfil completo del atleta autenticado.
   * TODO: descomentar llamada real cuando haya backend.
   */
  getPerfil(): Observable<PerfilAtleta> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<PerfilAtleta>(`${this.API}/perfil`, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of({
      ...MOCK_PERFIL,
      nombre: this.auth.nombre() ?? MOCK_PERFIL.nombre,
      servicio: (this.auth.servicio() as PerfilAtleta['servicio']) ?? MOCK_PERFIL.servicio,
    });
  }

  /**
   * Devuelve el entrenador asignado para el servicio indicado.
   * servicio: 'ENTRENAMIENTO' | 'NUTRICION'
   * TODO: descomentar llamada real cuando haya backend.
   */
  getEntrenadorAsignado(servicio: 'ENTRENAMIENTO' | 'NUTRICION'): Observable<EntrenadorAsignado | null> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<EntrenadorAsignado | null>(
    //   `${this.API}/entrenador?servicio=${servicio}`,
    //   { withCredentials: true }
    // );
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(null); // sin entrenador asignado por defecto
  }

  /**
   * Devuelve el plan de entrenamiento activo del atleta.
   * Solo llamar si servicio incluye ENTRENAMIENTO.
   * TODO: descomentar llamada real cuando haya backend.
   */
  getPlanEntrenamiento(): Observable<PlanEntrenamiento | null> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<PlanEntrenamiento | null>(
    //   `${this.API}/entrenamiento/plan-activo`,
    //   { withCredentials: true }
    // );
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(null); // sin plan asignado por defecto
  }

  /**
   * Devuelve el plan de nutrición activo del atleta.
   * Solo llamar si servicio incluye NUTRICION.
   * TODO: descomentar llamada real cuando haya backend.
   */
  getPlanNutricion(): Observable<PlanNutricion | null> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<PlanNutricion | null>(
    //   `${this.API}/nutricion/plan-activo`,
    //   { withCredentials: true }
    // );
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(null); // sin plan asignado por defecto
  }
}
