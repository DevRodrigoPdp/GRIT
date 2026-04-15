import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, forkJoin } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

// ── Tipos públicos ────────────────────────────────────────────────────────────

export interface AlimentoOFF {
  codigo: string;
  nombre: string;
  marca: string;
  kcalPor100g: number;
  proteinasPor100g: number;
  carbsPor100g: number;
  grasasPor100g: number;
}

// ── Tipos internos USDA ───────────────────────────────────────────────────────

interface USDANutrient {
  nutrientId: number;
  nutrientName: string;
  unitName: string;
  value: number;
}

interface USDAFood {
  fdcId: number;
  description: string;
  brandOwner?: string;
  foodNutrients: USDANutrient[];
}

interface USDASearchResponse {
  foods: USDAFood[];
}

interface MyMemoryResponse {
  responseStatus: number;
  responseData: { translatedText: string };
}

// ── Nutrient IDs de USDA ──────────────────────────────────────────────────────

const NUTRIENT = {
  KCAL:  1008,
  PROT:  1003,
  CARBS: 1005,
  FAT:   1004,
} as const;

// ── Servicio ──────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class OpenFoodFactsService {
  private http = inject(HttpClient);

  private readonly USDA      = 'https://api.nal.usda.gov/fdc/v1';
  private readonly MYMEMORY  = 'https://api.mymemory.translated.net/get';
  private readonly API_KEY   = environment.usdaApiKey;

  /**
   * Traduce el término ES → EN, busca en USDA y traduce los nombres de vuelta a ES.
   * Si cualquier traducción falla, usa el texto original.
   */
  buscarPorNombre(query: string): Observable<AlimentoOFF[]> {
    return this.traducir(query, 'es|en').pipe(
      switchMap(termEnIngles => this.buscarEnUSDA(termEnIngles)),
      switchMap(alimentos   => this.traducirNombres(alimentos)),
      catchError(() => of([]))
    );
  }

  /**
   * Busca por código de barras (UPC) en USDA.
   * USDA contiene productos de marca con UPC; devuelve null si no lo encuentra.
   */
  buscarPorCodigoBarras(barcode: string): Observable<AlimentoOFF | null> {
    const params = new HttpParams()
      .set('query', barcode)
      .set('api_key', this.API_KEY)
      .set('pageSize', '1');

    return this.http
      .get<USDASearchResponse>(`${this.USDA}/foods/search`, { params })
      .pipe(
        map(res => {
          const food = res.foods?.[0];
          return food ? this.normalizar(food) : null;
        }),
        catchError(() => of(null))
      );
  }

  // ── Privados ──────────────────────────────────────────────────────────────

  /**
   * Traduce los nombres de los alimentos de EN → ES en lotes de 5
   * para minimizar llamadas a la API (3 requests para 15 resultados).
   */
  private traducirNombres(alimentos: AlimentoOFF[]): Observable<AlimentoOFF[]> {
    if (alimentos.length === 0) return of([]);

    // Partir en lotes de 5 (≈300 chars por lote, bajo el límite de 500 de MyMemory)
    const lotes: AlimentoOFF[][] = [];
    for (let i = 0; i < alimentos.length; i += 5) {
      lotes.push(alimentos.slice(i, i + 5));
    }

    const lotes$ = lotes.map(lote =>
      this.traducir(lote.map(a => a.nombre).join('\n'), 'en|es').pipe(
        map(traduccion => {
          const nombres = traduccion.split('\n');
          return lote.map((a, i) => ({ ...a, nombre: nombres[i]?.trim() || a.nombre }));
        }),
        catchError(() => of(lote)) // si falla este lote, devuelve nombres en inglés
      )
    );

    return forkJoin(lotes$).pipe(map(grupos => grupos.flat()));
  }

  private traducir(texto: string, langpair: string): Observable<string> {
    const params = new HttpParams()
      .set('q', texto)
      .set('langpair', langpair);

    return this.http
      .get<MyMemoryResponse>(this.MYMEMORY, { params })
      .pipe(
        map(res =>
          res.responseStatus === 200
            ? res.responseData.translatedText
            : texto
        ),
        catchError(() => of(texto))
      );
  }

  private buscarEnUSDA(query: string): Observable<AlimentoOFF[]> {
    const params = new HttpParams()
      .set('query', query)
      .set('api_key', this.API_KEY)
      .set('pageSize', '15')
      .set('dataType', 'Foundation,SR Legacy,Branded');

    return this.http
      .get<USDASearchResponse>(`${this.USDA}/foods/search`, { params })
      .pipe(
        map(res =>
          (res.foods ?? [])
            .map(f => this.normalizar(f))
            .filter(a => a.kcalPor100g > 0)
        ),
        catchError(() => of([]))
      );
  }

  private normalizar(food: USDAFood): AlimentoOFF {
    const get = (id: number) =>
      food.foodNutrients.find(n => n.nutrientId === id)?.value ?? 0;

    return {
      codigo:           String(food.fdcId),
      nombre:           food.description,
      marca:            food.brandOwner ?? '',
      kcalPor100g:      get(NUTRIENT.KCAL),
      proteinasPor100g: get(NUTRIENT.PROT),
      carbsPor100g:     get(NUTRIENT.CARBS),
      grasasPor100g:    get(NUTRIENT.FAT),
    };
  }
}
