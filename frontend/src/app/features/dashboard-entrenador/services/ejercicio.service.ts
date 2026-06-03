import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, forkJoin } from 'rxjs';
import { map, switchMap, catchError, tap } from 'rxjs/operators';

// ── Tipos públicos ────────────────────────────────────────────────────────────

export interface EjercicioAPI {
  id:                   string;
  nombre:               string;       // traducido a ES
  categoria:            string;
  nivel:                string;       // Principiante / Intermedio / Avanzado
  equipamiento:         string;
  musculoPrincipal:     string;
  musculosSecundarios:  string[];
  instrucciones:        string[];     // EN (del dataset yuhonas)
  imagenes:             string[];     // URLs completas a GitHub raw
}

// ── Tipos internos (dataset yuhonas) ─────────────────────────────────────────

interface EjercicioRaw {
  id:               string;
  name:             string;
  force:            string | null;
  level:            'beginner' | 'intermediate' | 'expert';
  mechanic:         string | null;
  equipment:        string | null;
  primaryMuscles:   string[];
  secondaryMuscles: string[];
  instructions:     string[];
  category:         string;
  images:           string[];
}

interface MyMemoryResponse {
  responseStatus: number;
  responseData: { translatedText: string };
}

// ── URLs ──────────────────────────────────────────────────────────────────────

const DATASET_URL = 'https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json';
const IMG_BASE    = 'https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/';
const MYMEMORY    = 'https://api.mymemory.translated.net/get';

// ── Tablas de traducción ──────────────────────────────────────────────────────

const CAT_ES: Record<string, string> = {
  'strength':              'Fuerza',
  'stretching':            'Estiramientos',
  'cardio':                'Cardio',
  'olympic weightlifting': 'Halterofilia',
  'strongman':             'Fuerza total',
  'powerlifting':          'Powerlifting',
  'plyometrics':           'Pliometría',
  'crossfit':              'CrossFit',
};

const NIVEL_ES: Record<string, string> = {
  'beginner':    'Principiante',
  'intermediate': 'Intermedio',
  'expert':      'Avanzado',
};

const EQ_ES: Record<string, string> = {
  'barbell':       'Barra',
  'dumbbell':      'Mancuernas',
  'body only':     'Peso corporal',
  'machine':       'Máquina',
  'cable':         'Polea',
  'kettlebells':   'Kettlebell',
  'bands':         'Bandas',
  'medicine ball': 'Balón medicinal',
  'exercise ball': 'Fitball',
  'foam roll':     'Foam roller',
  'e-z curl bar':  'Barra EZ',
  'other':         'Otro',
};

const MUS_ES: Record<string, string> = {
  'abdominals':  'Abdominales',
  'abductors':   'Abductores',
  'adductors':   'Aductores',
  'biceps':      'Bíceps',
  'calves':      'Gemelos',
  'chest':       'Pecho',
  'forearms':    'Antebrazos',
  'glutes':      'Glúteos',
  'hamstrings':  'Isquiotibiales',
  'lats':        'Dorsal ancho',
  'lower back':  'Lumbar',
  'middle back': 'Espalda media',
  'traps':       'Trapecio',
  'triceps':     'Tríceps',
  'quadriceps':  'Cuádriceps',
  'shoulders':   'Hombros',
  'neck':        'Cuello',
};

// ── Servicio ──────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class EjercicioService {
  private http = inject(HttpClient);

  /** Dataset cargado en memoria tras la primera búsqueda. */
  private datasetCache: EjercicioRaw[] | null = null;

  /** Caché de resultados de búsqueda para no repetir traducciones. */
  private searchCache = new Map<string, EjercicioAPI[]>();

  /**
   * Traduce el término ES → EN, filtra el dataset y traduce los nombres de vuelta a ES.
   * Tras la primera carga, las búsquedas son instantáneas (sin red).
   */
  buscarEjercicios(query: string): Observable<EjercicioAPI[]> {
    const cacheKey = query.toLowerCase().trim();
    if (this.searchCache.has(cacheKey)) return of(this.searchCache.get(cacheKey)!);

    return this.loadDataset().pipe(
      switchMap(dataset =>
        this.traducir(query, 'es|en').pipe(
          map(termEN => this.filtrar(dataset, termEN))
        )
      ),
      switchMap(ejercicios => this.traducirNombres(ejercicios)),
      map(ejercicios => {
        this.searchCache.set(cacheKey, ejercicios);
        return ejercicios;
      }),
      catchError(() => of([]))
    );
  }

  // ── Privados ──────────────────────────────────────────────────────────────

  /**
   * Carga el dataset completo desde GitHub raw.
   * TODO: en producción el backend debería servir este dataset como proxy.
   */
  private loadDataset(): Observable<EjercicioRaw[]> {
    if (this.datasetCache) return of(this.datasetCache);
    return this.http.get<EjercicioRaw[]>(DATASET_URL).pipe(
      tap(data => { this.datasetCache = data; }),
      catchError(() => of([]))
    );
  }

  private filtrar(dataset: EjercicioRaw[], term: string): EjercicioAPI[] {
    const q = term.toLowerCase();
    return dataset
      .filter(e => e.name.toLowerCase().includes(q))
      .slice(0, 15)
      .map(e => this.normalizar(e));
  }

  private normalizar(raw: EjercicioRaw): EjercicioAPI {
    return {
      id:                   raw.id,
      nombre:               raw.name,
      categoria:            CAT_ES[raw.category]    ?? raw.category,
      nivel:                NIVEL_ES[raw.level]      ?? raw.level,
      equipamiento:         raw.equipment
        ? (EQ_ES[raw.equipment] ?? raw.equipment)
        : 'Sin equipo',
      musculoPrincipal:     raw.primaryMuscles[0]
        ? (MUS_ES[raw.primaryMuscles[0]] ?? raw.primaryMuscles[0])
        : '—',
      musculosSecundarios:  raw.secondaryMuscles.map(m => MUS_ES[m] ?? m),
      instrucciones:        raw.instructions,
      imagenes:             raw.images.map(img => `${IMG_BASE}${img}`),
    };
  }

  /**
   * Traduce las instrucciones de un ejercicio EN → ES en lotes de 2 pasos.
   * Cada paso suele tener ~150-250 chars; con 2 por lote nos mantenemos bajo 500.
   */
  traducirInstrucciones(instrucciones: string[]): Observable<string[]> {
    if (instrucciones.length === 0) return of([]);

    const lotes: string[][] = [];
    for (let i = 0; i < instrucciones.length; i += 2) {
      lotes.push(instrucciones.slice(i, i + 2));
    }

    const lotes$ = lotes.map(lote =>
      this.traducir(lote.join('\n'), 'en|es').pipe(
        map(t => t.split('\n').map(s => s.trim()).filter(Boolean)),
        catchError(() => of(lote))
      )
    );

    return forkJoin(lotes$).pipe(map(g => g.flat()));
  }

  private traducirNombres(ejercicios: EjercicioAPI[]): Observable<EjercicioAPI[]> {
    if (ejercicios.length === 0) return of([]);
    const lotes: EjercicioAPI[][] = [];
    for (let i = 0; i < ejercicios.length; i += 5) lotes.push(ejercicios.slice(i, i + 5));

    const lotes$ = lotes.map(lote =>
      this.traducir(lote.map(e => e.nombre).join('\n'), 'en|es').pipe(
        map(t => {
          const nombres = t.split('\n');
          return lote.map((e, i) => ({ ...e, nombre: nombres[i]?.trim() || e.nombre }));
        }),
        catchError(() => of(lote))
      )
    );
    return forkJoin(lotes$).pipe(map(g => g.flat()));
  }

  private traducir(texto: string, langpair: string): Observable<string> {
    const params = new HttpParams().set('q', texto).set('langpair', langpair);
    return this.http.get<MyMemoryResponse>(MYMEMORY, { params }).pipe(
      map(res => res.responseStatus === 200 ? res.responseData.translatedText : texto),
      catchError(() => of(texto))
    );
  }
}
