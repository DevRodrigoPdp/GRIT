import { Component, inject, signal, input, output } from '@angular/core';
import { AtletaService, PerfilAtleta } from '../../services/atleta.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-ajustes-atleta',
  standalone: true,
  imports: [],
  templateUrl: './ajustes-atleta.html',
})
export class AjustesAtletaComponent {
  private atleta = inject(AtletaService);
  private auth   = inject(AuthService);

  readonly perfilAtleta      = input<PerfilAtleta | null>(null);
  readonly perfilActualizado = output<PerfilAtleta>();

  private readonly PASS_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{8,}$/;

  readonly passAbierto       = signal(false);
  readonly passActual        = signal('');
  readonly passNueva         = signal('');
  readonly passConfirm       = signal('');
  readonly showPassActual    = signal(false);
  readonly showPassNueva     = signal(false);
  readonly showPassConfirm   = signal(false);
  readonly cambiandoPass     = signal(false);
  readonly passCambiada      = signal(false);
  readonly passError         = signal('');
  readonly bajaAbierta       = signal(false);
  readonly confirmarBaja     = signal(false);
  readonly textoConfirmaBaja = signal('');
  readonly eliminandoCuenta  = signal(false);

  readonly perfilAbierto   = signal(false);
  readonly editDeporte     = signal('');
  readonly editNivel       = signal<PerfilAtleta['nivel']>('PRINCIPIANTE');
  readonly editObjetivo    = signal<PerfilAtleta['objetivo']>(null);
  readonly guardandoPerfil = signal(false);
  readonly perfilGuardado  = signal(false);
  readonly perfilError     = signal('');

  readonly objetivos: { value: PerfilAtleta['objetivo']; label: string }[] = [
    { value: null,              label: 'Sin objetivo específico' },
    { value: 'RENDIMIENTO',     label: 'Rendimiento deportivo'  },
    { value: 'MASA_MUSCULAR',   label: 'Ganancia muscular'      },
    { value: 'PERDER_PESO',     label: 'Pérdida de peso'        },
    { value: 'SALUD',           label: 'Salud general'          },
    { value: 'RESISTENCIA',     label: 'Resistencia'            },
  ];

  readonly niveles: { value: PerfilAtleta['nivel']; label: string }[] = [
    { value: 'PRINCIPIANTE', label: 'Principiante' },
    { value: 'INTERMEDIO',   label: 'Intermedio'   },
    { value: 'AVANZADO',     label: 'Avanzado'     },
    { value: 'ELITE',        label: 'Élite'        },
  ];

  abrirEditarPerfil(): void {
    const p = this.perfilAtleta();
    if (p && !this.perfilAbierto()) {
      this.editDeporte.set(p.deporte);
      this.editNivel.set(p.nivel);
      this.editObjetivo.set(p.objetivo);
      this.perfilGuardado.set(false);
      this.perfilError.set('');
    }
    this.perfilAbierto.set(!this.perfilAbierto());
  }

  guardarPerfil(): void {
    this.guardandoPerfil.set(true);
    this.perfilError.set('');
    this.perfilGuardado.set(false);
    this.atleta.actualizarPerfil({
      deporte:  this.editDeporte(),
      nivel:    this.editNivel(),
      objetivo: this.editObjetivo(),
    }).subscribe({
      next: (p) => {
        this.perfilActualizado.emit(p);
        this.perfilGuardado.set(true);
        this.guardandoPerfil.set(false);
      },
      error: () => {
        this.perfilError.set('No se pudo guardar. Inténtalo de nuevo.');
        this.guardandoPerfil.set(false);
      },
    });
  }

  cambiarPassword(): void {
    this.passError.set('');
    if (this.passNueva() !== this.passConfirm()) { this.passError.set('Las contraseñas nuevas no coinciden.'); return; }
    if (!this.PASS_REGEX.test(this.passNueva())) { this.passError.set('La contraseña debe tener 8+ caracteres, mayúscula, minúscula, número y símbolo.'); return; }
    this.cambiandoPass.set(true);
    this.atleta.cambiarPassword(this.passActual(), this.passNueva()).subscribe({
      next: () => {
        this.passCambiada.set(true);
        this.passActual.set(''); this.passNueva.set(''); this.passConfirm.set('');
        this.cambiandoPass.set(false);
      },
      error: () => { this.passError.set('Contraseña actual incorrecta.'); this.cambiandoPass.set(false); },
    });
  }

  eliminarCuenta(): void {
    if (this.textoConfirmaBaja() !== 'ELIMINAR') return;
    this.eliminandoCuenta.set(true);
    this.atleta.eliminarCuenta().subscribe({
      next: () => { this.eliminandoCuenta.set(false); this.auth.logout(); },
      error: () => this.eliminandoCuenta.set(false),
    });
  }
}
