import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface EjercicioManual {
  id:     string;
  nombre: string;
  series: number;
  reps:   string;
  notas:  string;
}

export type TipoDescanso = 'completo' | 'activo' | 'movilidad' | 'stretching';

export const TIPOS_DESCANSO: { value: TipoDescanso; label: string; desc: string }[] = [
  { value: 'completo',   label: 'DESCANSO COMPLETO', desc: 'Sin actividad física'         },
  { value: 'activo',     label: 'DESCANSO ACTIVO',   desc: 'Paseo, natación suave...'      },
  { value: 'movilidad',  label: 'MOVILIDAD',          desc: 'Trabajo articular y de rango' },
  { value: 'stretching', label: 'STRETCHING',         desc: 'Estiramientos y recuperación' },
];

export interface Sesion {
  id:            string;
  nombre:        string;
  tipo:          'entrenamiento' | 'descanso';
  tipoDescanso?: TipoDescanso;
  ejercicios:    EjercicioManual[];
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

const STORAGE_KEY_HISTORIAL = 'grit_ejercicios_historial';

const BIBLIOTECA: string[] = [
  // Pecho
  'Press de banca', 'Press inclinado', 'Press declinado', 'Aperturas con mancuernas', 'Fondos en paralelas',
  // Espalda
  'Dominadas', 'Jalón al pecho', 'Remo con barra', 'Remo en polea baja', 'Remo con mancuerna', 'Pull-over',
  // Hombros
  'Press militar', 'Press Arnold', 'Elevaciones laterales', 'Elevaciones frontales', 'Pájaros', 'Face pull',
  // Bíceps
  'Curl de bíceps', 'Curl martillo', 'Curl concentrado', 'Curl en polea baja',
  // Tríceps
  'Extensión de tríceps en polea', 'Press francés', 'Patada de tríceps',
  // Pierna
  'Sentadilla', 'Sentadilla búlgara', 'Prensa 45°', 'Extensión de cuádriceps', 'Peso muerto', 'Peso muerto rumano',
  'Hip thrust', 'Curl femoral', 'Zancadas', 'Elevación de talones',
  // Core
  'Plancha', 'Crunch', 'Rueda abdominal', 'Elevación de piernas', 'Russian twist',
  // Funcional / cardio
  'Burpees', 'Saltos a cajón', 'Kettlebell swing', 'Mountain climbers', 'Sprints',
];

interface ApiResponse<T> { ok: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class EntrenamientoService {
  private http = inject(HttpClient);

  private readonly API = '/api/v1/entrenamiento';

  readonly historial = signal<string[]>([]);

  constructor() {
    this.cargarHistorial();
  }

  sugerencias(query: string): string[] {
    const q = query.trim().toLowerCase();
    const historial = this.historial();
    if (!q) return historial.slice(0, 8);
    const coincide = (n: string) => n.toLowerCase().includes(q);
    const deHistorial = historial.filter(coincide);
    const deBiblioteca = BIBLIOTECA.filter(n => coincide(n) && !deHistorial.includes(n));
    return [...deHistorial, ...deBiblioteca].slice(0, 8);
  }

  registrarUsoEjercicio(nombre: string): void {
    const n = nombre.trim();
    if (!n) return;
    this.historial.update(h => [n, ...h.filter(x => x !== n)].slice(0, 30));
    localStorage.setItem(STORAGE_KEY_HISTORIAL, JSON.stringify(this.historial()));
  }

  private cargarHistorial(): void {
    try {
      const raw = JSON.parse(localStorage.getItem(STORAGE_KEY_HISTORIAL) ?? '[]') as string[];
      this.historial.set(raw);
    } catch { /* storage corrupto */ }
  }

  getRutinas(atletaId: string): Observable<Rutina[]> {
    return this.http
      .get<ApiResponse<Rutina[]>>(`${this.API}/rutinas`, { params: { atletaId } })
      .pipe(map(r => r.data.map(x => ({ ...x, creadoEn: new Date(x.creadoEn) }))));
  }

  crearRutina(atletaId: string, nombre: string, descripcion: string, sesiones: Sesion[]): Observable<Rutina> {
    return this.http
      .post<ApiResponse<{ id: string; creadoEn: string }>>(`${this.API}/rutinas`, { atletaId, nombre, descripcion, sesiones })
      .pipe(map(r => ({
        id:          r.data.id,
        atletaId,
        nombre,
        descripcion,
        sesiones,
        creadoEn:    new Date(r.data.creadoEn),
        activa:      false,
      })));
  }

  eliminarRutina(_atletaId: string, rutinaId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.API}/rutinas/${rutinaId}`)
      .pipe(map(() => undefined));
  }

  activarRutina(_atletaId: string, rutinaId: string): Observable<void> {
    return this.http
      .put<ApiResponse<void>>(`${this.API}/rutinas/${rutinaId}/activar`, {})
      .pipe(map(() => undefined));
  }

  totalEjercicios(rutina: Rutina): number {
    return rutina.sesiones.reduce((s, ses) => s + ses.ejercicios.length, 0);
  }
}
