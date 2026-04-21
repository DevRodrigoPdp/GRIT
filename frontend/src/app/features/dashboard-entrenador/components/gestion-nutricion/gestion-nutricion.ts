import {
  Component, inject, signal, computed, input, OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, DatePipe } from '@angular/common';

import { NutricionService, PlanNutricion, Comida, AlimentoEnPlan } from '../../services/nutricion.service';
import { RecetasService, Receta } from '../../services/recetas.service';
import { AlimentoOFF } from '../../services/open-food-facts.service';
import { BuscadorAlimentoComponent } from '../buscador-alimento/buscador-alimento';

type Vista       = 'lista' | 'crear' | 'detalle';
type ModoReceta  = 'off' | 'picker' | 'crear';

@Component({
  selector: 'app-gestion-nutricion',
  standalone: true,
  imports: [FormsModule, DecimalPipe, DatePipe, BuscadorAlimentoComponent],
  templateUrl: './gestion-nutricion.html',
})
export class GestionNutricionComponent implements OnInit {
  readonly nutricion = inject(NutricionService);
  readonly recetas   = inject(RecetasService);

  readonly atletaId      = input<string | null>(null);
  readonly alergias      = input<string[]>([]);
  readonly intolerancias = input<string[]>([]);

  // ── Estado principal ──────────────────────────────────────────────────────
  vista        = signal<Vista>('lista');
  planes       = signal<PlanNutricion[]>([]);
  guardando    = signal(false);
  planDetalle  = signal<PlanNutricion | null>(null);

  // ── Formulario plan ───────────────────────────────────────────────────────
  nombrePlan      = signal('');
  descripcionPlan = signal('');
  comidas         = signal<Comida[]>([{ nombre: 'Comida 1', alimentos: [] }]);

  /** Índice de la comida con el buscador de alimentos abierto */
  buscadorEnComida = signal<number | null>(null);

  totales = computed(() => this.nutricion.calcularMacros(this.comidas()));

  // ── Estado de recetas ─────────────────────────────────────────────────────
  modoReceta       = signal<ModoReceta>('off');
  comidaParaReceta = signal<number | null>(null);

  /** Receta seleccionada en el picker, esperando confirmación de gramos */
  recetaPendiente  = signal<Receta | null>(null);
  recetaCantidadG  = signal(100);

  /** Estado del creador de recetas */
  recetaNombre      = signal('');
  recetaIngredientes = signal<AlimentoEnPlan[]>([]);
  recetaGramosTotal  = signal(0);
  buscandoIngrediente = signal(false);

  recetaGramosAuto = computed(() =>
    this.recetas.gramosIngredientes(this.recetaIngredientes())
  );

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
    this.comidas.set([{ nombre: 'Comida 1', alimentos: [] }]);
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

  // ── Recetas: picker ───────────────────────────────────────────────────────

  abrirRecetas(comidaIdx: number) {
    this.comidaParaReceta.set(comidaIdx);
    this.recetaPendiente.set(null);
    this.modoReceta.set('picker');
  }

  seleccionarReceta(receta: Receta) {
    this.recetaPendiente.set(receta);
    this.recetaCantidadG.set(100);
  }

  confirmarReceta() {
    const receta    = this.recetaPendiente();
    const comidaIdx = this.comidaParaReceta();
    if (!receta || comidaIdx === null) return;
    this.agregarAlimento(comidaIdx, {
      alimento: this.recetas.comoAlimento(receta),
      cantidadG: this.recetaCantidadG(),
    });
    this.cerrarReceta();
  }

  // ── Recetas: creador ──────────────────────────────────────────────────────

  iniciarCrearReceta() {
    this.recetaNombre.set('');
    this.recetaIngredientes.set([]);
    this.recetaGramosTotal.set(0);
    this.modoReceta.set('crear');
  }

  agregarIngredienteReceta(item: AlimentoEnPlan) {
    this.recetaIngredientes.update(l => [...l, item]);
    this.recetaGramosTotal.set(this.recetaGramosAuto());
    this.buscandoIngrediente.set(false);
  }

  quitarIngredienteReceta(idx: number) {
    this.recetaIngredientes.update(l => l.filter((_, i) => i !== idx));
    this.recetaGramosTotal.set(this.recetaGramosAuto());
  }

  guardarReceta() {
    if (!this.recetaNombre().trim() || this.recetaIngredientes().length === 0) return;
    this.recetas.guardar({
      nombre:       this.recetaNombre(),
      ingredientes: this.recetaIngredientes(),
      gramosTotal:  this.recetaGramosTotal() || this.recetaGramosAuto(),
    });
    this.modoReceta.set('picker');
    this.recetaPendiente.set(null);
  }

  cerrarReceta() {
    this.modoReceta.set('off');
    this.comidaParaReceta.set(null);
    this.recetaPendiente.set(null);
    this.buscandoIngrediente.set(false);
  }

  // ── Guardar / eliminar plan ───────────────────────────────────────────────

  guardarPlan() {
    const id = this.atletaId();
    if (!id || !this.nombrePlan().trim()) return;
    this.guardando.set(true);
    this.nutricion
      .crearPlan(id, this.nombrePlan(), this.descripcionPlan(), this.comidas())
      .subscribe(plan => {
        this.planes.update(p => [...p, plan]);
        this.guardando.set(false);
        this.vista.set('lista');
      });
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

  macrosReceta(ingredientes: AlimentoEnPlan[]) {
    return this.nutricion.calcularMacros([{ nombre: '', alimentos: ingredientes }]);
  }
}
