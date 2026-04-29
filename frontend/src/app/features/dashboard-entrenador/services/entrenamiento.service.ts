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

// Fallback local — descomentar si la API no está disponible
// const BIBLIOTECA: string[] = [
//   'Press de banca', 'Press inclinado', 'Press declinado', 'Aperturas con mancuernas', 'Fondos en paralelas',
//   'Dominadas', 'Jalón al pecho', 'Remo con barra', 'Remo en polea baja', 'Remo con mancuerna', 'Pull-over',
//   'Press militar', 'Press Arnold', 'Elevaciones laterales', 'Elevaciones frontales', 'Pájaros', 'Face pull',
//   'Curl de bíceps', 'Curl martillo', 'Curl concentrado', 'Curl en polea baja',
//   'Extensión de tríceps en polea', 'Press francés', 'Patada de tríceps',
//   'Sentadilla', 'Sentadilla búlgara', 'Prensa 45°', 'Extensión de cuádriceps', 'Peso muerto', 'Peso muerto rumano',
//   'Hip thrust', 'Curl femoral', 'Zancadas', 'Elevación de talones',
//   'Plancha', 'Crunch', 'Rueda abdominal', 'Elevación de piernas', 'Russian twist',
//   'Burpees', 'Saltos a cajón', 'Kettlebell swing', 'Mountain climbers', 'Sprints',
// ];

export interface EjercicioSugerencia {
  id:              string;
  nombre:          string;
  dificultad:      string;
  grupoMuscular:   string;
  equipoNecesario: string;
}

interface ApiResponse<T> { ok: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class EntrenamientoService {
  private http = inject(HttpClient);

  private readonly API    = '/api/v1/entrenamiento';
  private readonly API_EJ = '/api/v1/ejercicios';

  readonly historial        = signal<string[]>([]);
  readonly sugerenciasCache = signal<EjercicioSugerencia[]>([]);

  constructor() {
    this.cargarHistorial();
  }

  buscarEjercicios(query: string): void {
    this.http
      .get<ApiResponse<EjercicioSugerencia[]>>(`${this.API_EJ}/search`, { params: { q: query.trim() } })
      .subscribe(r => this.sugerenciasCache.set(r.data ?? []));

    // ── Fallback local (descomentar si la API no está disponible) ────────────
    // const q = query.trim().toLowerCase();
    // const historial = this.historial();
    // if (!q) { this.sugerenciasCache.set(historial.slice(0, 8).map(nombre => ({ id: '', nombre, dificultad: '', grupoMuscular: '', equipoNecesario: '' }))); return; }
    // const coincide = (n: string) => n.toLowerCase().includes(q);
    // const deHistorial = historial.filter(coincide);
    // const deBiblioteca = BIBLIOTECA.filter(n => coincide(n) && !deHistorial.includes(n));
    // this.sugerenciasCache.set([...deHistorial, ...deBiblioteca].slice(0, 8).map(nombre => ({ id: '', nombre, dificultad: '', grupoMuscular: '', equipoNecesario: '' })));
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
      .get<ApiResponse<any[]>>(`${this.API}/rutinas`, { params: { atletaId } })
      .pipe(map(r => r.data.map((x: any) => ({
        ...x,
        creadoEn: new Date(x.creadoEn),
        sesiones: (x.sesiones ?? []).map((s: any) => ({
          ...s,
          ejercicios: (s.ejercicios ?? []).map((e: any) => ({
            id:     e.ejercicio?.id     ?? crypto.randomUUID(),
            nombre: e.ejercicio?.nombre ?? '',
            series: e.series,
            reps:   e.reps,
            notas:  e.notas ?? '',
          })),
        })),
      }))));
  }

  crearRutina(atletaId: string, nombre: string, descripcion: string, sesiones: Sesion[]): Observable<Rutina> {
    const payload = {
      atletaId,
      nombre,
      descripcion,
      sesiones: sesiones.map(s => ({
        id:     s.id,
        nombre: s.nombre,
        ejercicios: s.ejercicios.map(e => ({
          ejercicio: { id: e.id, nombre: e.nombre },
          series: e.series,
          reps:   e.reps,
          notas:  e.notas,
        })),
      })),
    };
    return this.http
      .post<ApiResponse<{ id: string; creadoEn: string }>>(`${this.API}/rutinas`, payload)
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
