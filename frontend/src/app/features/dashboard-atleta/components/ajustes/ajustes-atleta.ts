import { Component, inject, signal, computed, input, output } from '@angular/core';
import { AtletaService, PerfilAtleta } from '../../services/atleta.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ThemeService } from '../../../../core/services/theme.service';

@Component({
  selector: 'app-ajustes-atleta',
  standalone: true,
  imports: [],
  templateUrl: './ajustes-atleta.html',
  styles: [`
    input[type=number]::-webkit-inner-spin-button,
    input[type=number]::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
    input[type=number] { -moz-appearance: textfield; }
    input:-webkit-autofill,
    input:-webkit-autofill:hover,
    input:-webkit-autofill:focus {
      -webkit-box-shadow: 0 0 0 30px #0F0F1A inset !important;
      -webkit-text-fill-color: white !important;
      caret-color: white;
    }
  `],
})
export class AjustesAtletaComponent {
  private atleta = inject(AtletaService);
  readonly auth   = inject(AuthService);
  readonly theme  = inject(ThemeService);

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

  readonly passReglas = computed(() => {
    const v = this.passNueva();
    return [
      { label: 'Mínimo 8 caracteres',  ok: v.length >= 8 },
      { label: 'Una mayúscula',         ok: /[A-Z]/.test(v) },
      { label: 'Una minúscula',         ok: /[a-z]/.test(v) },
      { label: 'Un número',             ok: /\d/.test(v) },
      { label: 'Un carácter especial',  ok: /[^a-zA-Z\d]/.test(v) },
    ];
  });

  readonly passConfirmError = computed(() => {
    const c = this.passConfirm();
    if (!c) return '';
    return c !== this.passNueva() ? 'Las contraseñas no coinciden' : '';
  });

  readonly perfilAbierto    = signal(false);
  readonly perfilIntentado  = signal(false);
  readonly editDeporte      = signal('');
  readonly editRestriccionesDieteticas = signal('');
  readonly editRestriccionesFisicas    = signal('');
  readonly editNivel        = signal<PerfilAtleta['nivel']>('PRINCIPIANTE');
  readonly editObjetivo     = signal<PerfilAtleta['objetivo']>(null);
  readonly nivelDropdownOpen    = signal(false);
  readonly objetivoDropdownOpen = signal(false);

  labelNivel()   { return this.niveles.find(n => n.value === this.editNivel())?.label   ?? ''; }
  labelObjetivoEdit() { return this.objetivos.find(o => o.value === this.editObjetivo())?.label ?? ''; }
  readonly editPeso         = signal(0);
  readonly editAltura       = signal(0);
  readonly guardandoPerfil  = signal(false);
  readonly perfilGuardado   = signal(false);
  readonly perfilError      = signal('');

  readonly deporteError = computed(() => {
    if (!this.perfilIntentado()) return '';
    const v = this.editDeporte().trim();
    if (!v) return 'Requerido';
    if (v.length > 50) return 'Máximo 50 caracteres';
    return '';
  });

  readonly pesoError = computed(() => {
    if (!this.perfilIntentado()) return '';
    const v = this.editPeso();
    if (!v || v < 30 || v > 300) return 'Valor entre 30 y 300 kg';
    return '';
  });

  readonly alturaError = computed(() => {
    if (!this.perfilIntentado()) return '';
    const v = this.editAltura();
    if (!v || v < 100 || v > 250) return 'Valor entre 100 y 250 cm';
    return '';
  });

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
      this.editPeso.set(p.peso);
      this.editAltura.set(p.altura);
      this.editRestriccionesDieteticas.set(p.restriccionesDieteticas);
      this.editRestriccionesFisicas.set(p.restriccionesFisicas);
      this.editAltura.set(p.altura);
      this.perfilGuardado.set(false);
      this.perfilError.set('');
      this.perfilIntentado.set(false);
    }
    this.perfilAbierto.set(!this.perfilAbierto());
  }

  guardarPerfil(): void {
    this.perfilIntentado.set(true);
    const peso = this.editPeso();
    const altura = this.editAltura();
    const deporte = this.editDeporte().trim();
    if (!deporte || deporte.length > 50 || peso < 30 || peso > 300 || altura < 100 || altura > 250) return;
    this.guardandoPerfil.set(true);
    this.perfilError.set('');
    this.perfilGuardado.set(false);
    this.atleta.actualizarPerfil({
      deporte:  this.editDeporte(),
      nivel:    this.editNivel(),
      objetivo: this.editObjetivo(),
      peso:     this.editPeso(),
      altura:   this.editAltura(),
      restriccionesDieteticas: this.editRestriccionesDieteticas(),
      restriccionesFisicas: this.editRestriccionesFisicas(),
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
