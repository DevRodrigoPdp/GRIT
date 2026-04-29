import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AlimentoOFF } from './alimentos.service';

// ── Tipos públicos ────────────────────────────────────────────────────────────

export interface AlimentoEnPlan {
  alimento: AlimentoOFF;
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
      .pipe(map(r => (r.data ?? []).map((p: any) => ({
        ...p,
        creadoEn: new Date(p.creadoEn),
        activo: p.activo ?? false,
      }))));
  }

  crearPlan(
    atletaId: string,
    nombre: string,
    descripcion: string,
    comidas: Comida[]
  ): Observable<PlanNutricion> {
    return this.http
      .post<ApiResponse<{ id: string; creadoEn: string }>>(`${this.API}/planes`, {
        atletaId, nombre, descripcion, comidas,
      }, { withCredentials: true })
      .pipe(map(r => ({
        id:          r.data.id,
        atletaId,
        nombre,
        descripcion,
        comidas,
        creadoEn:    new Date(r.data.creadoEn),
        activo:      false,
      })));
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
