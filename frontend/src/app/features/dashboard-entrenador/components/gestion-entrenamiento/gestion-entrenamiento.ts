import { Component, inject, signal, computed, input, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { EntrenamientoService, Rutina, Sesion, EjercicioManual } from '../../services/entrenamiento.service';

type Vista = 'lista' | 'crear' | 'detalle';

@Component({
  selector: 'app-gestion-entrenamiento',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './gestion-entrenamiento.html',
})
export class GestionEntrenamientoComponent implements OnInit {
  readonly entrenamiento = inject(EntrenamientoService);
  readonly atletaId      = input<string | null>(null);

  // ── Estado principal ──────────────────────────────────────────────────────
  readonly vista          = signal<Vista>('lista');
  readonly rutinas        = signal<Rutina[]>([]);
  readonly guardando      = signal(false);
  readonly rutinaDetalle  = signal<Rutina | null>(null);
  readonly sesionDetalleIdx = signal(0);
  readonly sesionDetalleActiva = computed(() =>
    this.rutinaDetalle()?.sesiones[this.sesionDetalleIdx()] ?? null
  );

  // ── Formulario rutina ─────────────────────────────────────────────────────
  readonly nombreRutina      = signal('');
  readonly descripcionRutina = signal('');

  readonly sesiones = signal<Sesion[]>([
    { id: crypto.randomUUID(), nombre: 'Sesión 1', tipo: 'entrenamiento', ejercicios: [] },
  ]);
  readonly sesionActivaIdx       = signal(0);
  readonly editandoNombreSesion  = signal(false);

  readonly sesionActiva = computed(() => this.sesiones()[this.sesionActivaIdx()] ?? null);

  readonly totalEjerciciosBorrador = computed(() =>
    this.sesiones().reduce((s, ses) => s + ses.ejercicios.length, 0)
  );

  // ── Modal ejercicio ───────────────────────────────────────────────────────
  readonly modalAbierto       = signal(false);
  readonly mostrarSugerencias = signal(false);
  readonly nuevoNombre  = signal('');
  readonly nuevoSeries  = signal(3);
  readonly nuevoReps    = signal('10');
  readonly nuevoNotas   = signal('');

  abrirModal(nombreInicial = ''): void {
    this.limpiarForm();
    if (nombreInicial) this.nuevoNombre.set(nombreInicial);
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  ngOnInit(): void {
    const id = this.atletaId();
    if (id) this.entrenamiento.getRutinas(id).subscribe(r => this.rutinas.set(r));
  }

  // ── Sesiones ──────────────────────────────────────────────────────────────

  abrirDetalle(rutina: Rutina): void {
    this.rutinaDetalle.set(rutina);
    this.sesionDetalleIdx.set(0);
    this.vista.set('detalle');
  }

  iniciarCreacion(): void {
    this.nombreRutina.set('');
    this.descripcionRutina.set('');
    this.sesiones.set([{ id: crypto.randomUUID(), nombre: 'Sesión 1', tipo: 'entrenamiento', ejercicios: [] }]);
    this.sesionActivaIdx.set(0);
    this.editandoNombreSesion.set(false);
    this.limpiarForm();
    this.vista.set('crear');
  }

  agregarSesion(): void {
    const n = this.sesiones().length + 1;
    this.sesiones.update(l => [...l, { id: crypto.randomUUID(), nombre: `Sesión ${n}`, tipo: 'entrenamiento', ejercicios: [] }]);
    this.sesionActivaIdx.set(this.sesiones().length - 1);
    this.editandoNombreSesion.set(true);
    this.limpiarForm();
  }

  eliminarSesion(idx: number): void {
    if (this.sesiones().length <= 1) return;
    this.sesiones.update(l => l.filter((_, i) => i !== idx));
    if (this.sesionActivaIdx() >= this.sesiones().length)
      this.sesionActivaIdx.set(this.sesiones().length - 1);
  }

  renombrarSesion(idx: number, nombre: string): void {
    this.sesiones.update(l => {
      const n = [...l];
      n[idx] = { ...n[idx], nombre };
      return n;
    });
  }

  // ── Ejercicios ────────────────────────────────────────────────────────────

  agregarEjercicio(): void {
    const nombre = this.nuevoNombre().trim();
    if (!nombre) return;
    this.entrenamiento.registrarUsoEjercicio(nombre);
    const ej: EjercicioManual = {
      id: crypto.randomUUID(),
      nombre,
      series: this.nuevoSeries(),
      reps:   this.nuevoReps().trim() || '10',
      notas:  this.nuevoNotas().trim(),
    };
    const idx = this.sesionActivaIdx();
    this.sesiones.update(l => {
      const n = [...l];
      n[idx] = { ...n[idx], ejercicios: [...n[idx].ejercicios, ej] };
      return n;
    });
    this.limpiarForm();
  }

  quitarEjercicio(sesionIdx: number, ejId: string): void {
    this.sesiones.update(l => {
      const n = [...l];
      n[sesionIdx] = { ...n[sesionIdx], ejercicios: n[sesionIdx].ejercicios.filter(e => e.id !== ejId) };
      return n;
    });
  }

  actualizarEjercicio(sesionIdx: number, ejId: string, campo: keyof EjercicioManual, valor: string | number): void {
    this.sesiones.update(l => {
      const n = [...l];
      n[sesionIdx] = { ...n[sesionIdx], ejercicios: n[sesionIdx].ejercicios.map(e => e.id === ejId ? { ...e, [campo]: valor } : e) };
      return n;
    });
  }

  limpiarForm(): void {
    this.nuevoNombre.set('');
    this.nuevoSeries.set(3);
    this.nuevoReps.set('10');
    this.nuevoNotas.set('');
  }

  // ── Guardar / eliminar ────────────────────────────────────────────────────

  guardarRutina(): void {
    const id = this.atletaId();
    if (!id || !this.nombreRutina().trim()) return;
    this.guardando.set(true);
    this.entrenamiento.crearRutina(id, this.nombreRutina(), this.descripcionRutina(), this.sesiones())
      .subscribe(rutina => {
        this.rutinas.update(r => [...r, rutina]);
        this.guardando.set(false);
        this.vista.set('lista');
      });
  }

  activarRutina(rutinaId: string): void {
    const id = this.atletaId();
    if (!id) return;
    this.entrenamiento.activarRutina(id, rutinaId).subscribe(() =>
      this.rutinas.update(r => r.map(x => ({ ...x, activa: x.id === rutinaId })))
    );
  }

  eliminarRutina(rutinaId: string): void {
    const id = this.atletaId();
    if (!id) return;
    this.entrenamiento.eliminarRutina(id, rutinaId).subscribe(() =>
      this.rutinas.update(r => r.filter(x => x.id !== rutinaId))
    );
  }
}
