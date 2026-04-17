import {
  Component, inject, signal, computed, input, OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';

import {
  EntrenamientoService, Rutina, Sesion, EjercicioEnSesion
} from '../../services/entrenamiento.service';
import { EjercicioAPI } from '../../services/ejercicio.service';
import { BuscadorEjercicioComponent } from '../buscador-ejercicio/buscador-ejercicio';

type Vista = 'lista' | 'crear';

@Component({
  selector: 'app-gestion-entrenamiento',
  standalone: true,
  imports: [FormsModule, DatePipe, BuscadorEjercicioComponent],
  templateUrl: './gestion-entrenamiento.html',
})
export class GestionEntrenamientoComponent implements OnInit {
  readonly entrenamiento = inject(EntrenamientoService);

  readonly atletaId = input<string | null>(null);

  // ── Estado principal ──────────────────────────────────────────────────────
  vista     = signal<Vista>('lista');
  rutinas   = signal<Rutina[]>([]);
  guardando = signal(false);

  // ── Formulario rutina ─────────────────────────────────────────────────────
  nombreRutina      = signal('');
  descripcionRutina = signal('');
  sesiones          = signal<Sesion[]>([
    { id: crypto.randomUUID(), nombre: 'Sesión 1', ejercicios: [] },
  ]);

  /** Índice de la pestaña de sesión activa */
  sesionActivaIdx = signal(0);

  /** true mientras el nombre de sesión activa está en edición */
  editandoNombreSesion = signal(false);

  /** Sesión actualmente visible */
  sesionActiva = computed(() => this.sesiones()[this.sesionActivaIdx()] ?? null);

  /** Buscador abierto en la sesión activa */
  buscadorAbierto = signal(false);

  totalEjerciciosBorrador = computed(() =>
    this.sesiones().reduce((s, ses) => s + ses.ejercicios.length, 0)
  );

  ngOnInit() {
    const id = this.atletaId();
    if (id) this.entrenamiento.getRutinas(id).subscribe(r => this.rutinas.set(r));
  }

  // ── Gestión de sesiones ───────────────────────────────────────────────────

  iniciarCreacion() {
    this.nombreRutina.set('');
    this.descripcionRutina.set('');
    this.sesiones.set([{ id: crypto.randomUUID(), nombre: 'Sesión 1', ejercicios: [] }]);
    this.sesionActivaIdx.set(0);
    this.editandoNombreSesion.set(false);
    this.vista.set('crear');
  }

  agregarSesion() {
    this.sesiones.update(l => [
      ...l,
      { id: crypto.randomUUID(), nombre: `Sesión ${l.length + 1}`, ejercicios: [] },
    ]);
    // Navegar a la nueva sesión
    this.sesionActivaIdx.set(this.sesiones().length - 1);
    this.editandoNombreSesion.set(true);
  }

  eliminarSesion(idx: number) {
    if (this.sesiones().length <= 1) return;
    this.sesiones.update(l => l.filter((_, i) => i !== idx));
    // Ajustar índice si borramos la activa o una anterior
    if (this.sesionActivaIdx() >= this.sesiones().length) {
      this.sesionActivaIdx.set(this.sesiones().length - 1);
    }
  }

  actualizarNombreSesion(idx: number, nombre: string) {
    this.sesiones.update(l => {
      const n = [...l];
      n[idx] = { ...n[idx], nombre };
      return n;
    });
  }

  // ── Gestión de ejercicios ─────────────────────────────────────────────────

  agregarEjercicio(item: EjercicioEnSesion) {
    const idx          = this.sesionActivaIdx();
    const sesionNombre = this.sesiones()[idx]?.nombre ?? '';
    this.sesiones.update(l => {
      const n = [...l];
      n[idx] = { ...n[idx], ejercicios: [...n[idx].ejercicios, item] };
      return n;
    });
    this.entrenamiento.registrarUso(item.ejercicio, sesionNombre);
    this.buscadorAbierto.set(false);
  }

  agregarReciente(ejercicio: EjercicioAPI) {
    this.agregarEjercicio({
      ejercicio,
      series: 3,
      reps:   '10',
      notas:  '',
    });
  }

  quitarEjercicio(sesionIdx: number, ejercicioIdx: number) {
    this.sesiones.update(l => {
      const n = [...l];
      n[sesionIdx] = {
        ...n[sesionIdx],
        ejercicios: n[sesionIdx].ejercicios.filter((_, i) => i !== ejercicioIdx),
      };
      return n;
    });
  }

  actualizarEjercicio(
    sesionIdx:    number,
    ejercicioIdx: number,
    campo:        'series' | 'reps' | 'notas',
    valor:        string | number
  ) {
    this.sesiones.update(l => {
      const n  = [...l];
      const ej = [...n[sesionIdx].ejercicios];
      ej[ejercicioIdx] = { ...ej[ejercicioIdx], [campo]: valor };
      n[sesionIdx] = { ...n[sesionIdx], ejercicios: ej };
      return n;
    });
  }

  yaEnSesion(sesionIdx: number, ejercicioId: string): boolean {
    return this.sesiones()[sesionIdx]?.ejercicios.some(e => e.ejercicio.id === ejercicioId) ?? false;
  }

  // ── Guardar / eliminar rutina ─────────────────────────────────────────────

  guardarRutina() {
    const id = this.atletaId();
    if (!id || !this.nombreRutina().trim()) return;
    this.guardando.set(true);
    this.entrenamiento
      .crearRutina(id, this.nombreRutina(), this.descripcionRutina(), this.sesiones())
      .subscribe(rutina => {
        this.rutinas.update(r => [...r, rutina]);
        this.guardando.set(false);
        this.vista.set('lista');
      });
  }

  activarRutina(rutinaId: string) {
    const id = this.atletaId();
    if (!id) return;
    this.entrenamiento.activarRutina(id, rutinaId).subscribe(() => {
      this.rutinas.update(r => r.map(x => ({ ...x, activa: x.id === rutinaId })));
    });
  }

  eliminarRutina(rutinaId: string) {
    const id = this.atletaId();
    if (!id) return;
    this.entrenamiento.eliminarRutina(id, rutinaId).subscribe(() => {
      this.rutinas.update(r => r.filter(x => x.id !== rutinaId));
    });
  }

}
