import {
  Component, inject, signal, output
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, EMPTY } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { EjercicioService, EjercicioAPI } from '../../services/ejercicio.service';
import { EjercicioEnSesion } from '../../services/entrenamiento.service';

@Component({
  selector: 'app-buscador-ejercicio',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './buscador-ejercicio.html',
})
export class BuscadorEjercicioComponent {
  private svc = inject(EjercicioService);

  readonly seleccionado = output<EjercicioEnSesion>();
  readonly cancelado    = output();

  query      = signal('');
  resultados = signal<EjercicioAPI[]>([]);
  cargando   = signal(false);
  error      = signal<string | null>(null);
  seleccion  = signal<EjercicioAPI | null>(null);

  series = signal(3);
  reps   = signal('10');
  notas  = signal('');

  imagenIdx            = signal(0);
  mostrarInstrucciones = signal(false);
  traduciendo          = signal(false);
  instruccionesES      = signal<string[] | null>(null);

  private instruccionesCache = new Map<string, string[]>();

  readonly presets: { label: string; series: number; reps: string }[] = [
    { label: '5×5',        series: 5, reps: '5'       },
    { label: '4×8',        series: 4, reps: '8'        },
    { label: '3×10',       series: 3, reps: '10'       },
    { label: '4×12',       series: 4, reps: '12'       },
    { label: '3×15',       series: 3, reps: '15'       },
    { label: '3×Al fallo', series: 3, reps: 'Al fallo' },
  ];

  private busqueda$ = new Subject<string>();

  constructor() {
    this.busqueda$
      .pipe(
        debounceTime(400),
        switchMap(q => {
          if (!q.trim()) {
            this.resultados.set([]);
            this.error.set(null);
            return EMPTY;
          }
          this.cargando.set(true);
          this.error.set(null);
          return this.svc.buscarEjercicios(q);
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

  elegir(ejercicio: EjercicioAPI) {
    this.seleccion.set(ejercicio);
    this.series.set(3);
    this.reps.set('10');
    this.notas.set('');
    this.imagenIdx.set(0);
    this.mostrarInstrucciones.set(false);
    this.instruccionesES.set(null);
  }

  toggleInstrucciones(ejercicio: EjercicioAPI) {
    if (this.mostrarInstrucciones()) {
      this.mostrarInstrucciones.set(false);
      return;
    }
    if (this.instruccionesCache.has(ejercicio.id)) {
      this.instruccionesES.set(this.instruccionesCache.get(ejercicio.id)!);
      this.mostrarInstrucciones.set(true);
      return;
    }
    this.traduciendo.set(true);
    this.svc.traducirInstrucciones(ejercicio.instrucciones).subscribe({
      next: traducidas => {
        this.instruccionesCache.set(ejercicio.id, traducidas);
        this.instruccionesES.set(traducidas);
        this.traduciendo.set(false);
        this.mostrarInstrucciones.set(true);
      },
      error: () => {
        this.instruccionesES.set(ejercicio.instrucciones);
        this.traduciendo.set(false);
        this.mostrarInstrucciones.set(true);
      },
    });
  }

  siguienteImagen(total: number) {
    this.imagenIdx.update(i => (i + 1) % total);
  }

  confirmar() {
    const ejercicio = this.seleccion();
    if (!ejercicio) return;
    this.seleccionado.emit({
      ejercicio,
      series: this.series(),
      reps:   this.reps(),
      notas:  this.notas(),
    });
  }

  aplicarPreset(preset: typeof this.presets[0]) {
    this.series.set(preset.series);
    this.reps.set(preset.reps);
  }
}
