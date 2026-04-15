import {
  Component, inject, signal, output, OnInit, OnDestroy
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { Subject, EMPTY } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { OpenFoodFactsService, AlimentoOFF } from '../../services/open-food-facts.service';
import { AlimentoEnPlan } from '../../services/nutricion.service';

type Modo = 'nombre' | 'barras';

@Component({
  selector: 'app-buscador-alimento',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  templateUrl: './buscador-alimento.html',
})
export class BuscadorAlimentoComponent {
  private off = inject(OpenFoodFactsService);

  readonly seleccionado = output<AlimentoEnPlan>();
  readonly cancelado    = output();

  modo         = signal<Modo>('nombre');
  query        = signal('');
  resultados   = signal<AlimentoOFF[]>([]);
  cargando     = signal(false);
  error        = signal<string | null>(null);
  seleccion    = signal<AlimentoOFF | null>(null);
  cantidadG    = signal(100);

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
          return this.modo() === 'nombre'
            ? this.off.buscarPorNombre(q)
            : this.off.buscarPorCodigoBarras(q).pipe(
                // barcode devuelve AlimentoOFF | null → normalizar a array
              );
        }),
        takeUntilDestroyed()
      )
      .subscribe({
        next: res => {
          this.cargando.set(false);
          if (Array.isArray(res)) {
            this.resultados.set(res);
            if (res.length === 0) this.error.set('Sin resultados. Prueba otro término.');
          } else if (res) {
            this.resultados.set([res]);
          } else {
            this.resultados.set([]);
            this.error.set('Código de barras no encontrado.');
          }
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

  cambiarModo(modo: Modo) {
    this.modo.set(modo);
    this.query.set('');
    this.resultados.set([]);
    this.error.set(null);
    this.seleccion.set(null);
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
