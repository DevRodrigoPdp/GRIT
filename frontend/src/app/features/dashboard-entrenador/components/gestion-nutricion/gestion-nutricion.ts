import {
  Component, inject, signal, computed, input, OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, DatePipe } from '@angular/common';

import { NutricionService, PlanNutricion, Comida, AlimentoEnPlan } from '../../services/nutricion.service';
import { AlimentoOFF } from '../../services/alimentos.service';
import { BuscadorAlimentoComponent } from '../buscador-alimento/buscador-alimento';

type Vista = 'lista' | 'crear' | 'detalle';

@Component({
  selector: 'app-gestion-nutricion',
  standalone: true,
  imports: [FormsModule, DecimalPipe, DatePipe, BuscadorAlimentoComponent],
  templateUrl: './gestion-nutricion.html',
})
export class GestionNutricionComponent implements OnInit {
  readonly nutricion = inject(NutricionService);

  readonly atletaId      = input<string | null>(null);
  readonly alergias      = input<string[]>([]);
  readonly intolerancias = input<string[]>([]);

  // ── Estado principal ──────────────────────────────────────────────────────
  vista        = signal<Vista>('lista');
  planes       = signal<PlanNutricion[]>([]);
  guardando      = signal(false);
  errorGuardando = signal('');
  planDetalle    = signal<PlanNutricion | null>(null);

  // ── Formulario plan ───────────────────────────────────────────────────────
  nombrePlan      = signal('');
  descripcionPlan = signal('');
  planActivo      = signal(false);
  planEditandoId  = signal<string | null>(null);
  comidas         = signal<Comida[]>([{ nombre: 'Comida 1', alimentos: [] }]);

  /** Índice de la comida con el buscador de alimentos abierto */
  buscadorEnComida = signal<number | null>(null);

  totales = computed(() => this.nutricion.calcularMacros(this.comidas()));

  ngOnInit() {
    const id = this.atletaId();
    if (id) this.nutricion.getPlanes(id).subscribe(p => this.planes.set(p));
  }

  // ── Gestión de comidas ────────────────────────────────────────────────────

  abrirDetalle(plan: PlanNutricion) {
    this.planDetalle.set(plan);
    this.vista.set('detalle');
  }

  iniciarCreacion() {
    this.nombrePlan.set('');
    this.descripcionPlan.set('');
    this.planActivo.set(false);
    this.planEditandoId.set(null);
    this.comidas.set([{ nombre: 'Comida 1', alimentos: [] }]);
    this.vista.set('crear');
  }

  iniciarEdicion(plan: PlanNutricion) {
    this.nombrePlan.set(plan.nombre);
    this.descripcionPlan.set(plan.descripcion ?? '');
    this.planActivo.set(plan.activo);
    this.planEditandoId.set(plan.id);
    this.comidas.set(plan.comidas.length > 0 ? plan.comidas : [{ nombre: 'Comida 1', alimentos: [] }]);
    this.vista.set('crear');
  }

  agregarComida() {
    this.comidas.update(l => [...l, { nombre: `Comida ${l.length + 1}`, alimentos: [] }]);
  }

  eliminarComida(idx: number) {
    this.comidas.update(l => l.filter((_, i) => i !== idx));
  }

  actualizarNombreComida(idx: number, nombre: string) {
    this.comidas.update(l => {
      const n = [...l];
      n[idx] = { ...n[idx], nombre };
      return n;
    });
  }

  actualizarNotasComida(idx: number, notas: string) {
    this.comidas.update(l => {
      const n = [...l];
      n[idx] = { ...n[idx], notas };
      return n;
    });
  }

  // ── Gestión de alimentos ──────────────────────────────────────────────────

  agregarAlimento(comidaIdx: number, item: AlimentoEnPlan) {
    const comidaNombre = this.comidas()[comidaIdx]?.nombre ?? '';
    this.comidas.update(l => {
      const n = [...l];
      n[comidaIdx] = { ...n[comidaIdx], alimentos: [...n[comidaIdx].alimentos, item] };
      return n;
    });
    this.nutricion.registrarUso(item.alimento, comidaNombre);
    this.buscadorEnComida.set(null);
  }

  agregarReciente(comidaIdx: number, alimento: AlimentoOFF) {
    this.agregarAlimento(comidaIdx, { alimento, cantidadG: 100 });
  }

  quitarAlimento(comidaIdx: number, alimentoIdx: number) {
    this.comidas.update(l => {
      const n = [...l];
      n[comidaIdx] = { ...n[comidaIdx], alimentos: n[comidaIdx].alimentos.filter((_, i) => i !== alimentoIdx) };
      return n;
    });
  }

  yaEnComida(comidaIdx: number, codigo: string): boolean {
    return this.comidas()[comidaIdx].alimentos.some(a => a.alimento.codigo === codigo);
  }

  // ── Guardar / eliminar plan ───────────────────────────────────────────────

  guardarPlan() {
    const atletaId    = this.atletaId();
    const editandoId  = this.planEditandoId();
    if (!atletaId || !this.nombrePlan().trim()) return;
    this.guardando.set(true);
    this.errorGuardando.set('');

    if (editandoId) {
      this.nutricion
        .actualizarPlan(editandoId, atletaId, this.nombrePlan(), this.descripcionPlan(), this.comidas(), this.planActivo())
        .subscribe({
          next: () => {
            this.planes.update(p => p.map(x => x.id === editandoId
              ? { ...x, nombre: this.nombrePlan(), descripcion: this.descripcionPlan(), comidas: this.comidas() }
              : x
            ));
            this.guardando.set(false);
            this.vista.set('lista');
          },
          error: (err) => {
            this.guardando.set(false);
            const msg = err?.error?.errors?.[0]?.defaultMessage
              ?? err?.error?.message
              ?? `Error ${err?.status ?? ''}`;
            this.errorGuardando.set(msg);
          },
        });
    } else {
      this.nutricion
        .crearPlan(atletaId, this.nombrePlan(), this.descripcionPlan(), this.comidas(), this.planActivo())
        .subscribe({
          next: plan => {
            this.planes.update(p => [...p, plan]);
            this.guardando.set(false);
            this.vista.set('lista');
          },
          error: (err) => {
            this.guardando.set(false);
            const msg = err?.error?.errors?.[0]?.defaultMessage
              ?? err?.error?.message
              ?? `Error ${err?.status ?? ''}`;
            this.errorGuardando.set(msg);
          },
        });
    }
  }

  activarPlan(planId: string) {
    const id = this.atletaId();
    if (!id) return;
    this.nutricion.activarPlan(id, planId).subscribe(() => {
      this.planes.update(p => p.map(x => ({ ...x, activo: x.id === planId })));
    });
  }

  eliminarPlan(planId: string) {
    const id = this.atletaId();
    if (!id) return;
    this.nutricion.eliminarPlan(id, planId).subscribe(() => {
      this.planes.update(p => p.filter(x => x.id !== planId));
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  macrosComida(comida: Comida) {
    return this.nutricion.calcularMacros([comida]);
  }

  totalAlimentos(comida: Comida): number {
    return comida.alimentos.reduce((s, a) => s + a.cantidadG, 0);
  }

}
