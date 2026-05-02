import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AlimentoOFF } from './alimentos.service';

// ── Tipos públicos ────────────────────────────────────────────────────────────

export interface AlimentoEnPlan {
  _id?:      string;   // AlimentoEnComida entity ID, presente al cargar desde backend
  alimento:  AlimentoOFF;
  cantidadG: number;
}

export interface Comida {
  nombre: string;
  alimentos: AlimentoEnPlan[];
  notas?: string;
}

export interface PlanNutricion {
  id: string;
  atletaId: string;
  nombre: string;
  descripcion: string;
  comidas: Comida[];
  creadoEn: Date;
  activo: boolean;
}

export interface MacrosTotales {
  kcal: number;
  prot: number;
  carbs: number;
  grasa: number;
}

interface ApiResponse<T> { ok: boolean; data: T; }

// ── Servicio ──────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class NutricionService {
  private http = inject(HttpClient);

  private readonly API = '/api/v1/nutricion';
  private readonly STORAGE_KEY = 'grit_ultimos_por_comida';

  /** Últimos alimentos por nombre de comida: { "Desayuno": [...], "Cena": [...] } */
  readonly ultimosPorComida = signal<Record<string, AlimentoOFF[]>>(
    JSON.parse(localStorage.getItem(this.STORAGE_KEY) ?? '{}')
  );

  ultimosDeComida(nombre: string): AlimentoOFF[] {
    return this.ultimosPorComida()[nombre] ?? [];
  }

  registrarUso(alimento: AlimentoOFF, comidaNombre: string): void {
    this.ultimosPorComida.update(mapa => {
      const existentes = mapa[comidaNombre] ?? [];
      const nueva = [
        alimento,
        ...existentes.filter(a => a.codigo !== alimento.codigo),
      ].slice(0, 8);
      const nuevoMapa = { ...mapa, [comidaNombre]: nueva };
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(nuevoMapa));
      return nuevoMapa;
    });
  }

  getPlanes(atletaId: string): Observable<PlanNutricion[]> {
    return this.http
      .get<ApiResponse<any[]>>(`${this.API}/planes`, { params: { atletaId }, withCredentials: true })
      .pipe(
        map(r => (r.data ?? []).map((p: any) => ({
          ...p,
          creadoEn: new Date(p.creadoEn),
          activo:   p.activo ?? false,
          comidas:  (p.comidas ?? []).map((c: any) => ({
            nombre:    c.nombre ?? '',
            notas:     c.notas  ?? '',
            alimentos: (c.alimentos ?? []).map((a: any) => ({
              _id:       a.id ?? undefined,
              cantidadG: Number(a.cantidadG ?? 0),
              alimento: {
                codigo:           a.codigoAlimento ?? a.codigo ?? '',
                nombre:           a.nombre         ?? '',
                marca:            a.marca          ?? '',
                kcalPor100g:      Number(a.kcalPor100g      ?? 0),
                proteinasPor100g: Number(a.proteinasPor100g ?? 0),
                carbsPor100g:     Number(a.carbsPor100g     ?? 0),
                grasasPor100g:    Number(a.grasasPor100g    ?? 0),
              },
            })),
          })),
        }))),
        catchError(() => of([])),
      );
  }

  crearPlan(
    atletaId: string,
    nombre: string,
    descripcion: string,
    comidas: Comida[],
    activo: boolean = false,
  ): Observable<PlanNutricion> {
    const macros = this.calcularMacros(comidas);
    const payload = {
      atletaId,
      nombre,
      descripcion,
      activo,
      kcalDiarias: Math.max(500, Math.round(macros.kcal)),
      comidas: comidas.map((c, ci) => ({
        nombre: c.nombre,
        orden:  ci,
        notas:  c.notas ?? '',
        alimentos: c.alimentos.map((a, ai) => ({
          codigoAlimento:   a.alimento.codigo,
          nombre:           a.alimento.nombre,
          marca:            a.alimento.marca ?? '',
          kcalPor100g:      a.alimento.kcalPor100g,
          proteinasPor100g: a.alimento.proteinasPor100g,
          carbsPor100g:     a.alimento.carbsPor100g,
          grasasPor100g:    a.alimento.grasasPor100g,
          cantidadG:        a.cantidadG,
          orden:            ai,
        })),
      })),
    };
    return this.http
      .post<ApiResponse<{ id: string; creadoEn: string }>>(`${this.API}/planes`, payload, { withCredentials: true })
      .pipe(map(r => ({
        id:          r.data.id,
        atletaId,
        nombre,
        descripcion,
        comidas,
        creadoEn:    new Date(r.data.creadoEn),
        activo,
      })));
  }

  actualizarPlan(
    planId: string,
    atletaId: string,
    nombre: string,
    descripcion: string,
    comidas: Comida[],
    activo: boolean,
  ): Observable<void> {
    const macros = this.calcularMacros(comidas);
    const payload = {
      atletaId,
      nombre,
      descripcion,
      activo,
      kcalDiarias: Math.max(500, Math.round(macros.kcal)),
      comidas: comidas.map((c, ci) => ({
        nombre:    c.nombre,
        orden:     ci,
        notas:     c.notas ?? '',
        alimentos: c.alimentos.map((a, ai) => ({
          orden: ai,
          id:               a._id ?? null,
          codigo:           a.alimento.codigo,
          nombre:           a.alimento.nombre,
          marca:            a.alimento.marca ?? '',
          kcalPor100g:      a.alimento.kcalPor100g,
          proteinasPor100g: a.alimento.proteinasPor100g,
          carbsPor100g:     a.alimento.carbsPor100g,
          grasasPor100g:    a.alimento.grasasPor100g,
          cantidadG:        a.cantidadG,
        })),
      })),
    };
    return this.http
      .put<ApiResponse<void>>(`${this.API}/planes/${planId}`, payload, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  eliminarPlan(_atletaId: string, planId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.API}/planes/${planId}`, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  activarPlan(_atletaId: string, planId: string): Observable<void> {
    return this.http
      .put<ApiResponse<void>>(`${this.API}/planes/${planId}/activar`, {}, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  calcularMacros(comidas: Comida[]): MacrosTotales {
    let kcal = 0, prot = 0, carbs = 0, grasa = 0;
    for (const comida of (comidas ?? [])) {
      for (const item of (comida.alimentos ?? [])) {
        const f = (item.cantidadG ?? 0) / 100;
        kcal  += (item.alimento?.kcalPor100g        ?? 0) * f;
        prot  += (item.alimento?.proteinasPor100g    ?? 0) * f;
        carbs += (item.alimento?.carbsPor100g        ?? 0) * f;
        grasa += (item.alimento?.grasasPor100g       ?? 0) * f;
      }
    }
    return { kcal, prot, carbs, grasa };
  }
}
