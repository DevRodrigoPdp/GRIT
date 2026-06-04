import { Component, inject, signal, computed, input, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { trigger, transition, style, animate } from '@angular/animations';
import {
  EntrenamientoService,
  Rutina,
  Sesion,
  EjercicioManual,
} from '../../services/entrenamiento.service';

export interface EjercicioSugerencia {
  id: string;
  nombre: string;
  dificultad: string;
  grupoMuscular: string;
  equipoNecesario: string;
}

type Vista = 'lista' | 'crear' | 'detalle';

@Component({
  selector: 'app-gestion-entrenamiento',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './gestion-entrenamiento.html',
  animations: [
    trigger('fadeView', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(10px)' }),
        animate('200ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
      ]),
    ]),
    trigger('fadeSesion', [
      transition('* => *', [
        style({ opacity: 0 }),
        animate('180ms ease-out', style({ opacity: 1 })),
      ]),
    ]),
  ],
})
export class GestionEntrenamientoComponent implements OnInit {
  readonly entrenamiento = inject(EntrenamientoService);
  readonly atletaId = input<string | null>(null);

  readonly loadingRutinas = signal(false);

  // ── Estado principal ──────────────────────────────────────────────────────
  readonly vista = signal<Vista>('lista');
  readonly rutinas = signal<Rutina[]>([]);
  readonly guardando = signal(false);
  readonly errorGuardando = signal('');
  readonly rutinaDetalle = signal<Rutina | null>(null);
  readonly rutinaEditandoId = signal<string | null>(null);
  readonly sesionDetalleIdx = signal(0);
  readonly sesionDetalleActiva = computed(
    () => this.rutinaDetalle()?.sesiones[this.sesionDetalleIdx()] ?? null,
  );

  // ── Formulario rutina ─────────────────────────────────────────────────────
  readonly nombreRutina = signal('');
  readonly descripcionRutina = signal('');
  readonly rutinaActiva = signal(false);

  readonly sesiones = signal<Sesion[]>([
    { id: crypto.randomUUID(), nombre: 'Sesión 1', tipo: 'entrenamiento', ejercicios: [] },
  ]);
  readonly sesionActivaIdx = signal(0);
  readonly editandoNombreSesion = signal(false);

  readonly sesionActiva = computed(() => this.sesiones()[this.sesionActivaIdx()] ?? null);

  readonly totalEjerciciosBorrador = computed(() =>
    this.sesiones().reduce((s, ses) => s + ses.ejercicios.length, 0),
  );

  // ── Modal ejercicio ───────────────────────────────────────────────────────
  readonly modalAbierto = signal(false);
  readonly mostrarSugerencias = signal(false);
  readonly nuevoNombre = signal('');
  readonly nuevoSeries = signal(3);
  readonly nuevoReps = signal('10');
  readonly nuevoNotas = signal('');
  readonly ejercicioSeleccionado = signal<EjercicioSugerencia | null>(null);

  abrirModal(nombreInicial = ''): void {
    this.limpiarForm();
    this.mostrarSugerencias.set(false);
    if (nombreInicial) this.nuevoNombre.set(nombreInicial);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.mostrarSugerencias.set(false);
    this.modalAbierto.set(false);
  }

  ngOnInit(): void {
    const id = this.atletaId();
    if (id) {
      this.loadingRutinas.set(true);
      this.entrenamiento.getRutinas(id).subscribe({
        next: (r) => {
          this.rutinas.set(r);
          this.loadingRutinas.set(false);
        },
        error: () => {
          this.loadingRutinas.set(false);
        },
      });
    }
  }

  // ── Sesiones ──────────────────────────────────────────────────────────────

  abrirDetalle(rutina: Rutina): void {
    this.rutinaDetalle.set(rutina);
    this.sesionDetalleIdx.set(0);
    this.vista.set('detalle');
  }

  iniciarCreacion(): void {
    this.rutinaEditandoId.set(null);
    this.nombreRutina.set('');
    this.descripcionRutina.set('');
    this.rutinaActiva.set(false);
    this.sesiones.set([
      { id: crypto.randomUUID(), nombre: 'Sesión 1', tipo: 'entrenamiento', ejercicios: [] },
    ]);
    this.sesionActivaIdx.set(0);
    this.editandoNombreSesion.set(false);
    this.limpiarForm();
    this.vista.set('crear');
  }

  editarRutina(rutina: Rutina): void {
    this.rutinaEditandoId.set(rutina.id);
    this.nombreRutina.set(rutina.nombre);
    this.descripcionRutina.set(rutina.descripcion);
    this.sesiones.set(rutina.sesiones.map((s) => ({ ...s, ejercicios: [...s.ejercicios] })));
    this.sesionActivaIdx.set(0);
    this.rutinaActiva.set(rutina.activa);
    this.editandoNombreSesion.set(false);
    this.errorGuardando.set('');
    this.limpiarForm();
    this.vista.set('crear');
  }

  agregarSesion(): void {
    const n = this.sesiones().length + 1;
    this.sesiones.update((l) => [
      ...l,
      { id: crypto.randomUUID(), nombre: `Sesión ${n}`, tipo: 'entrenamiento', ejercicios: [] },
    ]);
    this.sesionActivaIdx.set(this.sesiones().length - 1);
    this.editandoNombreSesion.set(true);
    this.limpiarForm();
  }

  eliminarSesion(idx: number): void {
    if (this.sesiones().length <= 1) return;
    this.sesiones.update((l) => l.filter((_, i) => i !== idx));
    if (this.sesionActivaIdx() >= this.sesiones().length)
      this.sesionActivaIdx.set(this.sesiones().length - 1);
  }

  renombrarSesion(idx: number, nombre: string): void {
    this.sesiones.update((l) => {
      const n = [...l];
      n[idx] = { ...n[idx], nombre };
      return n;
    });
  }

  // ── Ejercicios ────────────────────────────────────────────────────────────

  seleccionarSugerencia(sugerencia: EjercicioSugerencia): void {
    // 1. Guardamos el objeto completo (incluyendo el ID real de la DB)
    this.ejercicioSeleccionado.set(sugerencia);

    // 2. Sincronizamos el nombre para la UI
    this.nuevoNombre.set(sugerencia.nombre);

    // 3. Cerramos la lista de sugerencias
    this.mostrarSugerencias.set(false);
  }

  agregarEjercicio(): void {
    const nombreEnInput = this.nuevoNombre().trim();
    const maestro = this.ejercicioSeleccionado();
    const esDescanso = nombreEnInput.toLowerCase() === 'descanso';

    if (!esDescanso && !maestro) {
      alert('Selecciona un ejercicio del catálogo o escribe "Descanso".');
      return;
    }

    if (!esDescanso && maestro) {
      if (maestro.nombre.toLowerCase() !== nombreEnInput.toLowerCase()) {
        alert('El nombre no coincide con el catálogo.');
        return;
      }
    }

    const idFinal = esDescanso ? crypto.randomUUID() : maestro!.id;
    const nombreFinal = esDescanso ? 'Descanso' : maestro!.nombre;

    // 4. Creación del DTO limpio
    const ej: EjercicioManual = {
      id: idFinal,
      nombre: nombreFinal,
      series: this.nuevoSeries(),
      reps: esDescanso ? '0' : this.nuevoReps().toString(),
      notas: this.nuevoNotas().trim(),
    };

    // 5. Actualización del estado (Signals)
    const idx = this.sesionActivaIdx();
    this.sesiones.update((lista) => {
      const nuevasSesiones = [...lista];
      const sesion = nuevasSesiones[idx];
      if (sesion) {
        nuevasSesiones[idx] = {
          ...sesion,
          ejercicios: [...sesion.ejercicios, ej],
        };
      }
      return nuevasSesiones;
    });

    this.cerrarModal();
  }

  quitarEjercicio(sesionIdx: number, ejId: string): void {
    this.sesiones.update((l) => {
      const n = [...l];
      n[sesionIdx] = {
        ...n[sesionIdx],
        ejercicios: n[sesionIdx].ejercicios.filter((e) => e.id !== ejId),
      };
      return n;
    });
  }

  actualizarEjercicio(
    sesionIdx: number,
    ejId: string,
    campo: keyof EjercicioManual,
    valor: string | number,
  ): void {
    this.sesiones.update((l) => {
      const n = [...l];
      n[sesionIdx] = {
        ...n[sesionIdx],
        ejercicios: n[sesionIdx].ejercicios.map((e) =>
          e.id === ejId ? { ...e, [campo]: valor } : e,
        ),
      };
      return n;
    });
  }

  limpiarForm(): void {
    this.nuevoNombre.set('');
    this.nuevoSeries.set(3);
    this.nuevoReps.set('10');
    this.nuevoNotas.set('');
    this.ejercicioSeleccionado.set(null);
    this.mostrarSugerencias.set(false);
    this.entrenamiento.limpiarSugerencias();
  }

  // ── Guardar / eliminar ────────────────────────────────────────────────────

  guardarRutina(): void {
    const id = this.atletaId();
    if (!id || !this.nombreRutina().trim()) return;

    const totalEj = this.sesiones().reduce((s, ses) => s + ses.ejercicios.length, 0);
    if (totalEj === 0) {
      this.errorGuardando.set('La rutina debe tener al menos un ejercicio.');
      return;
    }

    this.guardando.set(true);
    this.errorGuardando.set('');

    const rutinaId = this.rutinaEditandoId();

    const observer = {
      next: (rutina: Rutina) => {
        if (rutinaId) {
          this.rutinas.update((r) => r.map((x) => (x.id === rutinaId ? rutina : x)));
        } else {
          this.rutinas.update((r) => [...r, rutina]);
        }
        this.finalizarFlujo();
      },
      error: (err: any) => {
        this.guardando.set(false);
        this.errorGuardando.set('Error en el servidor. Inténtalo de nuevo.');
        console.error('Error persistiendo rutina:', err);
      },
    };

    if (rutinaId) {
      this.entrenamiento
        .actualizarRutina(
          rutinaId,
          id,
          this.nombreRutina(),
          this.descripcionRutina(),
          this.rutinaActiva(),
          this.sesiones(),
        )
        .subscribe(observer);
    } else {
      this.entrenamiento
        .crearRutina(id, this.nombreRutina(), this.descripcionRutina(), this.sesiones())
        .subscribe(observer);
    }
  }

  private finalizarFlujo(): void {
    this.rutinaEditandoId.set(null);
    this.guardando.set(false);
    this.vista.set('lista');
  }

  activarRutina(rutinaId: string): void {
    const id = this.atletaId();
    if (!id) return;
    this.entrenamiento.activarRutina(id, rutinaId).subscribe(() => {
      this.rutinas.update((r) => r.map((x) => ({ ...x, activa: x.id === rutinaId })));
      if (this.rutinaDetalle()?.id === rutinaId)
        this.rutinaDetalle.update((p) => (p ? { ...p, activa: true } : p));
    });
  }

  desactivarRutina(rutinaId: string): void {
    const id = this.atletaId();
    if (!id) return;
    this.entrenamiento.desactivarRutina(id, rutinaId).subscribe(() => {
      this.rutinas.update((r) => r.map((x) => (x.id === rutinaId ? { ...x, activa: false } : x)));
      if (this.rutinaDetalle()?.id === rutinaId)
        this.rutinaDetalle.update((p) => (p ? { ...p, activa: false } : p));
    });
  }

  eliminarRutina(rutinaId: string): void {
    const id = this.atletaId();
    if (!id) return;
    this.entrenamiento
      .eliminarRutina(id, rutinaId)
      .subscribe(() => this.rutinas.update((r) => r.filter((x) => x.id !== rutinaId)));
  }
}
