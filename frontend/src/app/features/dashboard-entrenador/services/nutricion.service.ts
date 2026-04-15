import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AlimentoOFF } from './open-food-facts.service';

// ── Tipos públicos ────────────────────────────────────────────────────────────

export interface AlimentoEnPlan {
  alimento: AlimentoOFF;
  cantidadG: number;
}

export interface Comida {
  nombre: string;
  alimentos: AlimentoEnPlan[];
}

export interface PlanNutricion {
  id: string;
  atletaId: string;
  nombre: string;
  descripcion: string;
  comidas: Comida[];
  creadoEn: Date;
}

export interface MacrosTotales {
  kcal: number;
  prot: number;
  carbs: number;
  grasa: number;
}

// ── Servicio ──────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class NutricionService {
  /** Almacén en memoria: atletaId → planes */
  private planes = new Map<string, PlanNutricion[]>();

  private readonly STORAGE_KEY = 'grit_ultimos_por_comida';

  /** Últimos alimentos por nombre de comida: { "Desayuno": [...], "Cena": [...] } */
  readonly ultimosPorComida = signal<Record<string, AlimentoOFF[]>>(
    JSON.parse(localStorage.getItem(this.STORAGE_KEY) ?? '{}')
  );

  /** Devuelve los últimos alimentos usados en una comida concreta. */
  ultimosDeComida(nombre: string): AlimentoOFF[] {
    return this.ultimosPorComida()[nombre] ?? [];
  }

  /** Registra un alimento como reciente para la comida indicada. */
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

  /**
   * Devuelve los planes de un atleta.
   * TODO: reemplazar por GET /api/v1/nutricion/planes?atletaId=
   */
  getPlanes(atletaId: string): Observable<PlanNutricion[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<PlanNutricion[]>(`/api/v1/nutricion/planes`, {
    //   params: { atletaId }, withCredentials: true
    // });
    // ── MOCK ──────────────────────────────────────────────────────────────
    return of(this.planes.get(atletaId) ?? []);
  }

  /**
   * Crea un plan de nutrición para un atleta.
   * TODO: reemplazar por POST /api/v1/nutricion/planes
   */
  crearPlan(
    atletaId: string,
    nombre: string,
    descripcion: string,
    comidas: Comida[]
  ): Observable<PlanNutricion> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.post<PlanNutricion>(`/api/v1/nutricion/planes`, {
    //   atletaId, nombre, descripcion, comidas
    // }, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    const plan: PlanNutricion = {
      id: crypto.randomUUID(),
      atletaId,
      nombre,
      descripcion,
      comidas,
      creadoEn: new Date(),
    };
    const existentes = this.planes.get(atletaId) ?? [];
    this.planes.set(atletaId, [...existentes, plan]);
    return of(plan);
  }

  /**
   * Elimina un plan por id.
   * TODO: reemplazar por DELETE /api/v1/nutricion/planes/:id
   */
  eliminarPlan(atletaId: string, planId: string): Observable<void> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.delete<void>(`/api/v1/nutricion/planes/${planId}`, { withCredentials: true });
    // ── MOCK ──────────────────────────────────────────────────────────────
    const existentes = this.planes.get(atletaId) ?? [];
    this.planes.set(atletaId, existentes.filter(p => p.id !== planId));
    return of(undefined);
  }

  /** Calcula los macros totales de una lista de comidas. */
  calcularMacros(comidas: Comida[]): MacrosTotales {
    let kcal = 0, prot = 0, carbs = 0, grasa = 0;
    for (const comida of comidas) {
      for (const item of comida.alimentos) {
        const f = item.cantidadG / 100;
        kcal  += item.alimento.kcalPor100g        * f;
        prot  += item.alimento.proteinasPor100g    * f;
        carbs += item.alimento.carbsPor100g        * f;
        grasa += item.alimento.grasasPor100g       * f;
      }
    }
    return { kcal, prot, carbs, grasa };
  }
}
