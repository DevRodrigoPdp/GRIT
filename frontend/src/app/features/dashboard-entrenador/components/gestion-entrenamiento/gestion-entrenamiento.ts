import { Component, inject, signal, computed, input, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { EntrenamientoService, Rutina, Sesion, EjercicioManual } from '../../services/entrenamiento.service';


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
})
export class GestionEntrenamientoComponent implements OnInit {
  readonly entrenamiento = inject(EntrenamientoService);
  readonly atletaId = input<string | null>(null);

  // ── Estado principal ──────────────────────────────────────────────────────
  readonly vista = signal<Vista>('lista');
  readonly rutinas = signal<Rutina[]>([]);
  readonly guardando = signal(false);
  readonly errorGuardando = signal('');
  readonly rutinaDetalle = signal<Rutina | null>(null);
  readonly sesionDetalleIdx = signal(0);
  readonly sesionDetalleActiva = computed(() =>
    this.rutinaDetalle()?.sesiones[this.sesionDetalleIdx()] ?? null
  );

  // ── Formulario rutina ─────────────────────────────────────────────────────
  readonly nombreRutina = signal('');
  readonly descripcionRutina = signal('');

  readonly sesiones = signal<Sesion[]>([
    { id: crypto.randomUUID(), nombre: 'Sesión 1', tipo: 'entrenamiento', ejercicios: [] },
  ]);
  readonly sesionActivaIdx = signal(0);
  readonly editandoNombreSesion = signal(false);

  readonly sesionActiva = computed(() => this.sesiones()[this.sesionActivaIdx()] ?? null);

  readonly totalEjerciciosBorrador = computed(() =>
    this.sesiones().reduce((s, ses) => s + ses.ejercicios.length, 0)
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

  seleccionarSugerencia(sugerencia: EjercicioSugerencia): void {
  // 1. Guardamos el objeto completo (incluyendo el ID real de la DB)
  this.ejercicioSeleccionado.set(sugerencia); 
  
  // 2. Sincronizamos el nombre para la UI
  this.nuevoNombre.set(sugerencia.nombre); 
  
  // 3. Cerramos la lista de sugerencias
  this.mostrarSugerencias.set(false); 
}

  agregarEjercicio(): void {
    const maestro = this.ejercicioSeleccionado();
    const nombreEnInput = this.nuevoNombre().trim();

    // 1. Validamos que exista un ejercicio seleccionado
    if (!maestro) {
      alert('Debes seleccionar un ejercicio de la lista de sugerencias.');
      return;
    }

    // 2. Validamos coincidencia ignorando mayúsculas/minúsculas
    // Esto evita que falle por una letra minúscula
    if (maestro.nombre.toLowerCase() !== nombreEnInput.toLowerCase()) {
      alert('El nombre no coincide con el ejercicio seleccionado. Por favor, selecciona uno de la lista.');
      return;
    }

    // 3. Si todo está bien, creamos el objeto para la sesión
    const ej: EjercicioManual = {
      id: maestro.id, // ID real de la DB
      nombre: maestro.nombre, // Nombre oficial del catálogo
      series: this.nuevoSeries(),
      reps: this.nuevoReps().toString().trim() || '10',
      notas: this.nuevoNotas().trim(),
    };

    const idx = this.sesionActivaIdx();
    this.sesiones.update(lista => {
      const nuevasSesiones = [...lista];
      const sesion = nuevasSesiones[idx];
      if (sesion) {
        nuevasSesiones[idx] = {
          ...sesion,
          ejercicios: [...sesion.ejercicios, ej]
        };
      }
      return nuevasSesiones;
    });

    this.cerrarModal(); // Esto debería limpiar el form y el ejercicioSeleccionado
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
    this.ejercicioSeleccionado.set(null);
  }

  // ── Guardar / eliminar ────────────────────────────────────────────────────

  guardarRutina(): void {
    const id = this.atletaId();
    if (!id || !this.nombreRutina().trim()) return;
    this.guardando.set(true);
    this.errorGuardando.set('');
    this.entrenamiento.crearRutina(id, this.nombreRutina(), this.descripcionRutina(), this.sesiones())
      .subscribe({
        next: rutina => {
          this.rutinas.update(r => [...r, rutina]);
          this.guardando.set(false);
          this.vista.set('lista');
        },
        error: () => {
          this.errorGuardando.set('No se pudo guardar la rutina. Inténtalo de nuevo.');
          this.guardando.set(false);
        },
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
