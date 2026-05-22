import { Component, inject, input, computed, signal, effect } from '@angular/core';
import { PlanNutricion, NotaNutricionista } from '../../services/atleta.service';
import { ThemeService } from '../../../../core/services/theme.service';
import jsPDF from 'jspdf';

interface ItemCompra { nombre: string; }

@Component({
  selector: 'app-vista-dieta',
  standalone: true,
  imports: [],
  templateUrl: './vista-dieta.html',
})
export class VistaDietaComponent {
  private readonly theme = inject(ThemeService);
  readonly plan  = input<PlanNutricion | null>(null);
  readonly notas = input<NotaNutricionista[]>([]);

  private readonly COMPRA_KEY  = 'grit_compra_checked';
  private readonly COMPRA_PLAN = 'grit_compra_plan_id';

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

  descargarListaCompra(): void {
    const plan   = this.plan();
    const items  = this.listaCompra();
    if (!items.length) return;

    const doc    = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    const checked = this.itemsCompraChecked();
    const fecha  = new Date().toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' });

    // ── Paleta según tema ─────────────────────────────────────────────────
    type RGB = [number, number, number];
    const dark = this.theme.isDark();
    const GREEN:  RGB = [46, 211, 141];
    const BG:     RGB = dark ? [10, 10, 20]    : [255, 255, 255];
    const TEXT:   RGB = dark ? [255, 255, 255] : [15,  15,  25];
    const MUTED:  RGB = dark ? [110, 110, 125] : [120, 120, 135];
    const ROWBG:  RGB = dark ? [22,  22,  34]  : [245, 246, 250];
    const ROWDON: RGB = dark ? [16,  16,  26]  : [235, 252, 244];
    const DIVID:  RGB = dark ? [40,  42,  55]  : [220, 222, 228];
    const CHKBRD: RGB = dark ? [60,  62,  80]  : [190, 192, 200];

    const W = 210, pad = 14;

    // ── Fondo ─────────────────────────────────────────────────────────────
    doc.setFillColor(...BG);
    doc.rect(0, 0, W, 297, 'F');

    // ── Accent bar top ────────────────────────────────────────────────────
    doc.setFillColor(...GREEN);
    doc.rect(0, 0, W, 2, 'F');

    // ── Header ────────────────────────────────────────────────────────────
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(7);
    doc.setTextColor(...GREEN);
    doc.text('GRIT — LISTA DE LA COMPRA', pad, 12);

    doc.setFontSize(22);
    doc.setTextColor(...TEXT);
    doc.text(plan?.nombre?.toUpperCase() ?? 'MI PLAN', pad, 24);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(7.5);
    doc.setTextColor(...MUTED);
    doc.text(fecha, pad, 30);

    // Línea separadora
    doc.setDrawColor(...GREEN);
    doc.setLineWidth(0.4);
    doc.line(pad, 34, W - pad, 34);

    // ── Stats ─────────────────────────────────────────────────────────────
    const total = items.length;

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(7);
    doc.setTextColor(...MUTED);
    doc.text('ALIMENTOS', pad, 41);

    doc.setFontSize(14);
    doc.setTextColor(...TEXT);
    doc.text(String(total), pad, 48);

    // ── Items ─────────────────────────────────────────────────────────────
    let y = 58;
    const rowH = 9;
    doc.setFontSize(7.5);

    for (const item of items) {
      const done = checked.includes(item.nombre);

      // Fondo fila
      if (done) doc.setFillColor(...ROWDON); else doc.setFillColor(...ROWBG);
      doc.rect(pad - 2, y - 5.5, W - pad * 2 + 4, rowH, 'F');

      // Checkbox
      if (done) {
        doc.setFillColor(...GREEN);
        doc.rect(pad, y - 3.5, 4, 4, 'F');
        doc.setDrawColor(...BG);
        doc.setLineWidth(0.5);
        doc.line(pad + 0.8, y - 1.5, pad + 1.8, y - 0.3);
        doc.line(pad + 1.8, y - 0.3, pad + 3.5, y - 3);
      } else {
        doc.setDrawColor(...CHKBRD);
        doc.setLineWidth(0.35);
        doc.rect(pad, y - 3.5, 4, 4);
      }

      // Nombre
      doc.setFont('helvetica', done ? 'normal' : 'bold');
      if (done) doc.setTextColor(...MUTED); else doc.setTextColor(...TEXT);
      doc.text(item.nombre, pad + 7, y - 0.5);

      // Divisor
      doc.setDrawColor(...DIVID);
      doc.setLineWidth(0.2);
      doc.line(pad - 2, y + 3.5, W - pad + 2, y + 3.5);

      y += rowH;

      if (y > 270) {
        doc.addPage();
        doc.setFillColor(...BG);
        doc.rect(0, 0, W, 297, 'F');
        doc.setFillColor(...GREEN);
        doc.rect(0, 0, W, 1.5, 'F');
        y = 20;
      }
    }

    // ── Footer ────────────────────────────────────────────────────────────
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(6.5);
    doc.setTextColor(...MUTED);
    doc.text('Generado con GRIT', pad, 290);
    doc.setTextColor(...GREEN);
    doc.text('●', W - pad - 6, 290);

    doc.save(`lista-compra-${plan?.nombre?.toLowerCase().replace(/\s+/g, '-') ?? 'plan'}.pdf`);
  }
}
