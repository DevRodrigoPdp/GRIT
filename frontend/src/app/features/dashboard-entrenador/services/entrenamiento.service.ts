import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { EjercicioAPI } from './ejercicio.service';

// ── Tipos públicos ────────────────────────────────────────────────────────────

export interface EjercicioEnSesion {
  ejercicio: EjercicioAPI;
  series:    number;
  reps:      string;       // "8-12", "10", "Al fallo", "30 seg"
  notas:     string;
}

export interface Sesion {
  id:         string;
  nombre:     string;
  ejercicios: EjercicioEnSesion[];
}

export interface Rutina {
  id:          string;
  atletaId:    string;
  nombre:      string;
  descripcion: string;
  sesiones:    Sesion[];
  creadoEn:    Date;
  activa:      boolean;
}

// ── Servicio ──────────────────────────────────────────────────────────────────

const STORAGE_KEY_RUTINAS   = 'grit_rutinas';
const STORAGE_KEY_RECIENTES = 'grit_ultimos_ejercicios';

@Injectable({ providedIn: 'root' })
export class EntrenamientoService {
  private rutinasMap = new Map<string, Rutina[]>();

  /** Últimos ejercicios por nombre de sesión: { "Piernas": [...], "Pecho": [...] } */
  readonly ultimosPorSesion = signal<Record<string, EjercicioAPI[]>>(
    JSON.parse(localStorage.getItem(STORAGE_KEY_RECIENTES) ?? '{}')
  );

  constructor() {
    this.cargarRutinas();
  }

  /** Devuelve los últimos ejercicios usados en una sesión concreta. */
  ultimosDeSesion(nombre: string): EjercicioAPI[] {
    return this.ultimosPorSesion()[nombre] ?? [];
  }

  /** Registra un ejercicio como reciente para la sesión indicada. */
  registrarUso(ejercicio: EjercicioAPI, sesionNombre: string): void {
    this.ultimosPorSesion.update(mapa => {
      const existentes = mapa[sesionNombre] ?? [];
      const nueva = [
        ejercicio,
        ...existentes.filter(e => e.id !== ejercicio.id),
      ].slice(0, 6);
      const nuevoMapa = { ...mapa, [sesionNombre]: nueva };
      localStorage.setItem(STORAGE_KEY_RECIENTES, JSON.stringify(nuevoMapa));
      return nuevoMapa;
    });
  }

  /**
   * Devuelve las rutinas de un atleta.
   * TODO: reemplazar por GET /api/v1/entrenamiento/rutinas?atletaId=
   */
  getRutinas(atletaId: string): Observable<Rutina[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<Rutina[]>(`/api/v1/entrenamiento/rutinas`, {
    //   params: { atletaId }, withCredentials: true
    // });
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(this.rutinasMap.get(atletaId) ?? []);
  }

  /**
   * Crea una rutina de entrenamiento para un atleta.
   * TODO: reemplazar por POST /api/v1/entrenamiento/rutinas
   */
  crearRutina(
    atletaId:    string,
    nombre:      string,
    descripcion: string,
    sesiones:    Sesion[]
  ): Observable<Rutina> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.post<Rutina>(`/api/v1/entrenamiento/rutinas`, {
    //   atletaId, nombre, descripcion, sesiones
    // }, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    const rutina: Rutina = {
      id:          crypto.randomUUID(),
      atletaId,
      nombre,
      descripcion,
      sesiones,
      creadoEn:    new Date(),
      activa:      false,
    };
    const existentes = this.rutinasMap.get(atletaId) ?? [];
    this.rutinasMap.set(atletaId, [...existentes, rutina]);
    this.persistirRutinas();
    return of(rutina);
  }

  /**
   * Elimina una rutina por id.
   * TODO: reemplazar por DELETE /api/v1/entrenamiento/rutinas/:id
   */
  eliminarRutina(atletaId: string, rutinaId: string): Observable<void> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.delete<void>(`/api/v1/entrenamiento/rutinas/${rutinaId}`, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    const existentes = this.rutinasMap.get(atletaId) ?? [];
    this.rutinasMap.set(atletaId, existentes.filter(r => r.id !== rutinaId));
    this.persistirRutinas();
    return of(undefined);
  }

  /**
   * Marca una rutina como activa (desactiva el resto del atleta).
   * TODO: reemplazar por PUT /api/v1/entrenamiento/rutinas/:id/activar
   */
  activarRutina(atletaId: string, rutinaId: string): Observable<void> {
    // return this.http.put<void>(`/api/v1/entrenamiento/rutinas/${rutinaId}/activar`, {}, { withCredentials: true });
    const existentes = this.rutinasMap.get(atletaId) ?? [];
    this.rutinasMap.set(atletaId, existentes.map(r => ({ ...r, activa: r.id === rutinaId })));
    this.persistirRutinas();
    return of(undefined);
  }

  /** Total de ejercicios de una rutina (sumando todas las sesiones). */
  totalEjercicios(rutina: Rutina): number {
    return rutina.sesiones.reduce((s, ses) => s + ses.ejercicios.length, 0);
  }

  // ── Persistencia ─────────────────────────────────────────────────────────

  private cargarRutinas(): void {
    try {
      const raw = JSON.parse(localStorage.getItem(STORAGE_KEY_RUTINAS) ?? '[]') as Rutina[];
      for (const rutina of raw) {
        const lista = this.rutinasMap.get(rutina.atletaId) ?? [];
        lista.push(rutina);
        this.rutinasMap.set(rutina.atletaId, lista);
      }
    } catch {
      // Storage corrupto: empieza limpio
    }
  }

  private persistirRutinas(): void {
    const todas: Rutina[] = [];
    this.rutinasMap.forEach(lista => todas.push(...lista));
    localStorage.setItem(STORAGE_KEY_RUTINAS, JSON.stringify(todas));
  }
}
