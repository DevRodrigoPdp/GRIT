import { Component, input, computed, signal, effect } from '@angular/core';
import { PlanNutricion, NotaNutricionista } from '../../services/atleta.service';

interface ItemCompra { nombre: string; }

@Component({
  selector: 'app-vista-dieta',
  standalone: true,
  imports: [],
  templateUrl: './vista-dieta.html',
})
export class VistaDietaComponent {
  readonly plan  = input<PlanNutricion | null>(null);
  readonly notas = input<NotaNutricionista[]>([]);

  private readonly COMPRA_KEY  = 'grit_compra_checked';
  private readonly COMPRA_PLAN = 'grit_compra_plan_id';
  readonly planExpandido = signal(false);

  readonly itemsCompraChecked  = signal<string[]>(
    JSON.parse(sessionStorage.getItem('grit_compra_checked') ?? '[]')
  );

  readonly listaCompra = computed<ItemCompra[]>(() => {
    const plan = this.plan();
    if (!plan) return [];
    const vistos = new Set<string>();
    const items: ItemCompra[] = [];
    for (const comida of plan.comidas)
      for (const alimento of comida.alimentos)
        if (!vistos.has(alimento.nombre)) { vistos.add(alimento.nombre); items.push({ nombre: alimento.nombre }); }
    return items;
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
