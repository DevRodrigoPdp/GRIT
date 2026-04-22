import {
  Component, inject, signal, output
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { Subject, EMPTY } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { AlimentosService, AlimentoOFF } from '../../services/alimentos.service';
import { AlimentoEnPlan } from '../../services/nutricion.service';

@Component({
  selector: 'app-buscador-alimento',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  templateUrl: './buscador-alimento.html',
})
export class BuscadorAlimentoComponent {
  private svc = inject(AlimentosService);

  readonly seleccionado = output<AlimentoEnPlan>();
  readonly cancelado    = output();

  query      = signal('');
  resultados = signal<AlimentoOFF[]>([]);
  cargando   = signal(false);
  error      = signal<string | null>(null);
  seleccion  = signal<AlimentoOFF | null>(null);
  cantidadG  = signal(100);

  private busqueda$ = new Subject<string>();

  constructor() {
    this.busqueda$
      .pipe(
        debounceTime(400),
        switchMap(q => {
          if (!q.trim()) {
            this.resultados.set([]);
            return EMPTY;
          }
          this.cargando.set(true);
          this.error.set(null);
          return this.svc.buscarPorNombre(q);
        }),
        takeUntilDestroyed()
      )
      .subscribe({
        next: res => {
          this.cargando.set(false);
          this.resultados.set(res);
          if (res.length === 0) this.error.set('Sin resultados. Prueba otro término.');
        },
        error: () => {
          this.cargando.set(false);
          this.error.set('Error de red. Inténtalo de nuevo.');
        },
      });
  }

  onQueryChange(value: string) {
    this.query.set(value);
    this.seleccion.set(null);
    this.busqueda$.next(value);
  }

  elegir(alimento: AlimentoOFF) {
    this.seleccion.set(alimento);
    this.cantidadG.set(100);
  }

  confirmar() {
    const alimento = this.seleccion();
    if (!alimento) return;
    this.seleccionado.emit({ alimento, cantidadG: this.cantidadG() });
  }

  kcalParaCantidad(): number {
    const a = this.seleccion();
    if (!a) return 0;
    return (a.kcalPor100g * this.cantidadG()) / 100;
  }
}
