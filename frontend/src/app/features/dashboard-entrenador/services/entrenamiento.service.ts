import { Injectable, signal, inject, DestroyRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, Subject, debounceTime, switchMap, distinctUntilChanged, catchError, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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
interface PagedResponse<T> { content: T[]; page: { size: number; number: number; totalElements: number; totalPages: number; }; }

@Injectable({ providedIn: 'root' })
export class EntrenamientoService {
  private http = inject(HttpClient);

  private readonly API    = '/api/v1/entrenamiento';
  private readonly API_EJ = '/api/v1/ejercicios';

  readonly historial           = signal<string[]>([]);
  readonly sugerenciasCache    = signal<EjercicioSugerencia[]>([]);
  readonly cargandoSugerencias = signal(false);

  private readonly busqueda$ = new Subject<string>();
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    this.cargarHistorial();
    this.busqueda$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => {
        if (q.length < 2) { this.sugerenciasCache.set([]); return of(null); }
        this.cargandoSugerencias.set(true);
        return this.http
          .get<PagedResponse<EjercicioSugerencia>>(`${this.API_EJ}/search`, { params: { q, page: 0, size: 15}, withCredentials: true })
          .pipe(catchError(() => of(null)));
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(r => {
      this.cargandoSugerencias.set(false);
      if (r) this.sugerenciasCache.set(r.content ?? []);
    });
  }

  buscarEjercicios(query: string): void {
    this.busqueda$.next(query.trim());
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
            id:     e.ejercicio?.id ?? e.ejercicioId ?? e.id,
            nombre: e.ejercicio?.nombre ?? e.ejercicioNombre ?? e.nombre ?? '',
            series: e.series ?? 0,
            reps:   String(e.reps ?? ''),
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
      sesiones: sesiones.map((s, si) => ({
        nombre: s.nombre,
        orden:  si,
        ejercicios: s.ejercicios.map((e, ei) => ({
          ejercicioId: e.id,
          nombre:      e.nombre,
          orden:       ei,
          series:      e.series,
          reps:        e.reps,
          notas:       e.notas,
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

  actualizarRutina(rutinaId: string, nombre: string, descripcion: string, sesiones: Sesion[]): Observable<Rutina> {
    const payload = {
      nombre,
      descripcion,
      sesiones: sesiones.map((s, si) => ({
        nombre: s.nombre,
        orden:  si,
        ejercicios: s.ejercicios.map((e, ei) => ({
          ejercicioId: e.id,
          nombre:      e.nombre,
          orden:       ei,
          series:      e.series,
          reps:        e.reps,
          notas:       e.notas,
        })),
      })),
    };
    return this.http
      .put<ApiResponse<{ id: string; atletaId: string; creadoEn: string; activa: boolean }>>(`${this.API}/rutinas/${rutinaId}`, payload)
      .pipe(map(r => ({
        id:          r.data.id,
        atletaId:    r.data.atletaId,
        nombre,
        descripcion,
        sesiones,
        creadoEn:    new Date(r.data.creadoEn),
        activa:      r.data.activa,
      })));
  }

  eliminarRutina(_atletaId: string, rutinaId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.API}/rutinas/${rutinaId}`)
      .pipe(map(() => undefined));
  }

  activarRutina(_atletaId: string, rutinaId: string): Observable<void> {
    return this.http
      .patch<ApiResponse<void>>(`${this.API}/rutinas/${rutinaId}/activar`, {})
      .pipe(map(() => undefined));
  }

  desactivarRutina(_atletaId: string, rutinaId: string): Observable<void> {
    return this.http
      .patch<ApiResponse<void>>(`${this.API}/rutinas/${rutinaId}/desactivar`, {})
      .pipe(map(() => undefined));
  }

  totalEjercicios(rutina: Rutina): number {
    return rutina.sesiones.reduce((s, ses) => s + ses.ejercicios.length, 0);
  }
}
