import { Component, input, computed, signal, effect } from '@angular/core';
import { PlanNutricion, NotaNutricionista } from '../../services/atleta.service';

interface ItemCompra { nombre: string; cantidad: string; }

@Component({
  selector: 'app-vista-dieta',
  standalone: true,
  imports: [],
  templateUrl: './vista-dieta.html',
})
export class VistaDietaComponent {
  readonly plan  = input<PlanNutricion | null>(null);
  readonly notas = input<NotaNutricionista[]>([]);

  readonly comidaActiva = signal<string | null>(null);

  private readonly COMPRA_KEY  = 'grit_compra_checked';
  private readonly COMPRA_PLAN = 'grit_compra_plan_id';
  readonly itemsCompraChecked  = signal<string[]>(
    JSON.parse(sessionStorage.getItem('grit_compra_checked') ?? '[]')
  );

  readonly listaCompra = computed<ItemCompra[]>(() => {
    const plan = this.plan();
    if (!plan) return [];
    const mapa = new Map<string, string>();
    for (const comida of plan.comidas)
      for (const alimento of comida.alimentos)
        if (!mapa.has(alimento.nombre)) mapa.set(alimento.nombre, alimento.cantidad);
    return Array.from(mapa.entries()).map(([nombre, cantidad]) => ({ nombre, cantidad }));
  });

  constructor() {
    effect(() => {
      const plan = this.plan();
      if (plan?.id && plan.id !== sessionStorage.getItem(this.COMPRA_PLAN)) {
        sessionStorage.setItem(this.COMPRA_PLAN, plan.id);
        sessionStorage.setItem(this.COMPRA_KEY, '[]');
        this.itemsCompraChecked.set([]);
      }
    });
  }

  abrirComida(nombre: string): void {
    this.comidaActiva.set(this.comidaActiva() === nombre ? null : nombre);
  }

  toggleItemCompra(nombre: string): void {
    this.itemsCompraChecked.update(items => {
      const next = items.includes(nombre) ? items.filter(i => i !== nombre) : [...items, nombre];
      sessionStorage.setItem(this.COMPRA_KEY, JSON.stringify(next));
      return next;
    });
  }

  totalKcalComida(alimentos: PlanNutricion['comidas'][0]['alimentos']): number {
    return alimentos.reduce((sum, a) => sum + (a.kcal ?? 0), 0);
  }

  formatFecha(fecha: string): string {
    const [year, month, day] = fecha.split('-');
    const meses = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic'];
    return `${parseInt(day)} ${meses[parseInt(month) - 1]} ${year}`;
  }
}
