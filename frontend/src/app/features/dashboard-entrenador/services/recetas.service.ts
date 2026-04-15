import { Injectable, signal } from '@angular/core';
import { AlimentoOFF } from './open-food-facts.service';
import { AlimentoEnPlan } from './nutricion.service';

// ── Tipos ─────────────────────────────────────────────────────────────────────

export interface Receta {
  id: string;
  nombre: string;
  /** Ingredientes con su cantidad en gramos */
  ingredientes: AlimentoEnPlan[];
  /** Peso final del plato (por defecto = suma de ingredientes) */
  gramosTotal: number;
}

// ── Servicio ──────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class RecetasService {
  private readonly STORAGE_KEY = 'grit_recetas';

  readonly recetas = signal<Receta[]>(
    JSON.parse(localStorage.getItem(this.STORAGE_KEY) ?? '[]')
  );

  guardar(datos: Omit<Receta, 'id'>): Receta {
    const nueva: Receta = { ...datos, id: crypto.randomUUID() };
    this.recetas.update(lista => {
      const actualizada = [...lista, nueva];
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(actualizada));
      return actualizada;
    });
    return nueva;
  }

  eliminar(id: string): void {
    this.recetas.update(lista => {
      const actualizada = lista.filter(r => r.id !== id);
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(actualizada));
      return actualizada;
    });
  }

  /**
   * Convierte una receta en AlimentoOFF para añadirla a una comida.
   * Los macros se calculan por 100g respecto al peso total del plato.
   */
  comoAlimento(receta: Receta): AlimentoOFF {
    let kcal = 0, prot = 0, carbs = 0, grasa = 0;
    for (const ing of receta.ingredientes) {
      const f = ing.cantidadG / 100;
      kcal  += ing.alimento.kcalPor100g        * f;
      prot  += ing.alimento.proteinasPor100g    * f;
      carbs += ing.alimento.carbsPor100g        * f;
      grasa += ing.alimento.grasasPor100g       * f;
    }
    const base = receta.gramosTotal || 1;
    return {
      codigo:           `receta-${receta.id}`,
      nombre:           receta.nombre,
      marca:            'Receta propia',
      kcalPor100g:      (kcal  / base) * 100,
      proteinasPor100g: (prot  / base) * 100,
      carbsPor100g:     (carbs / base) * 100,
      grasasPor100g:    (grasa / base) * 100,
    };
  }

  /** Suma los gramos de todos los ingredientes de una receta. */
  gramosIngredientes(ingredientes: AlimentoEnPlan[]): number {
    return ingredientes.reduce((s, i) => s + i.cantidadG, 0);
  }
}
