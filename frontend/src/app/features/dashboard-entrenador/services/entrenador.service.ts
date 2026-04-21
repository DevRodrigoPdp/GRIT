import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

// ── Tipos de respuesta del backend ─────────────────────────────────────────

export interface PerfilEntrenador {
  id: string;
  nombre: string;
  correo: string;
  titulacionEntrenamiento: 'GRADO_CAFYD' | 'TSAF_TSEAS' | 'CERT_AFDA0210' | null;
  titulacionNutricion: 'GRADO_NUTRICION_DIETETICA' | 'TSD' | null;
  experienciaAnos: number;
  descripcion: string;
  masters: string[];
  estado: 'ACTIVO' | 'PENDIENTE_REVISION' | 'RECHAZADO';
  solicitudAmpliacionPendiente?: 'ENTRENAMIENTO' | 'NUTRICION' | null;
  codigoInvitacion: string;
}

export type TitulacionEntrenamiento = 'GRADO_CAFYD' | 'TSAF_TSEAS' | 'CERT_AFDA0210';
export type TitulacionNutricion     = 'GRADO_NUTRICION_DIETETICA' | 'TSD';

export interface AtletaAsignado {
  id: string;
  nombre: string;
  deporte: string;
  nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | 'ELITE';
  /** Servicios contratados por el atleta con este entrenador */
  servicio: 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';
  tienePlanActivo: boolean;
  alergias?: string[];
  intolerancias?: string[];
}

// ── Mocks ────────────────────────────────────────────────────────────────────

// ── Cambia este mock según el perfil que quieras probar ──────────────────────

const MOCK_PERFIL_AMBOS: PerfilEntrenador = {
  id: 'mock-uuid-entrenador',
  nombre: 'Entrenador Demo',
  correo: 'entrenador@demo.com',
  titulacionEntrenamiento: 'GRADO_CAFYD',
  titulacionNutricion: 'GRADO_NUTRICION_DIETETICA',
  experienciaAnos: 5,
  descripcion: 'Especialista en rendimiento deportivo.',
  masters: ['Máster en Alto Rendimiento Deportivo', 'Máster en Nutrición Deportiva'],
  estado: 'ACTIVO',
  codigoInvitacion: 'GRIT-X7K2-9PQR',
};

const MOCK_PERFIL_NUTRICION: PerfilEntrenador = {
  id: 'mock-uuid-nutricionista',
  nombre: 'Nutricionista Demo',
  correo: 'nutricionista@demo.com',
  titulacionEntrenamiento: null,
  titulacionNutricion: 'GRADO_NUTRICION_DIETETICA',
  experienciaAnos: 7,
  descripcion: 'Dietista-nutricionista especializada en deporte de resistencia.',
  masters: ['Máster en Nutrición Deportiva y Rendimiento'],
  estado: 'ACTIVO',
  codigoInvitacion: 'GRIT-N3TR-5KWZ',
};

// ← Cambia aquí para probar distintos perfiles
const MOCK_PERFIL = MOCK_PERFIL_NUTRICION;

const MOCK_ATLETAS: AtletaAsignado[] = [
  { id: 'atleta-1', nombre: 'Carlos Ruiz',   deporte: 'Fútbol',    nivel: 'AVANZADO',     servicio: 'AMBOS',         tienePlanActivo: true,  alergias: ['Frutos secos', 'Marisco'], intolerancias: ['Lactosa'] },
  { id: 'atleta-2', nombre: 'Laura Sánchez', deporte: 'CrossFit',  nivel: 'INTERMEDIO',   servicio: 'ENTRENAMIENTO', tienePlanActivo: true  },
  { id: 'atleta-3', nombre: 'Marcos Ibáñez', deporte: 'Natación',  nivel: 'ELITE',        servicio: 'NUTRICION',     tienePlanActivo: false, intolerancias: ['Gluten'] },
  { id: 'atleta-4', nombre: 'Sara Molina',   deporte: 'Atletismo', nivel: 'PRINCIPIANTE', servicio: 'AMBOS',         tienePlanActivo: false },
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
   * Verifica si un código de colegiado ya existe en la base de datos.
   * TODO: descomentar llamada real cuando haya backend.
   */
  checkCodigoColegiadoExists(codigo: string): Observable<boolean> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<boolean>(`${this.API}/check-codigo/${codigo}`, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(codigo === 'MAD-12345' || codigo === 'AND-00123');
  }

  getMisAtletas(): Observable<AtletaAsignado[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<AtletaAsignado[]>(`${this.API}/atletas`, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(MOCK_ATLETAS);
  }

  invitarAtleta(email: string): Observable<void> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.post<void>(`${this.API}/invitar`, { email }, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    console.log('[mock] Invitación enviada a', email);
    return of(undefined);
  }

  solicitarAmpliacionFormacion(modulo: 'ENTRENAMIENTO' | 'NUTRICION', titulacion: string, documentos: File[]): Observable<void> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // const fd = new FormData();
    // fd.append('modulo', modulo);
    // fd.append('titulacion', titulacion);
    // documentos.forEach(f => fd.append('documentos', f));
    // return this.http.post<void>(`${this.API}/ampliar-formacion`, fd, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(undefined);
  }
}
