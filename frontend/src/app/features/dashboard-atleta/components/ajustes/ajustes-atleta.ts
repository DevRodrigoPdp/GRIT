import { Component, inject, signal } from '@angular/core';
import { AtletaService } from '../../services/atleta.service';
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

  readonly passAbierto       = signal(false);
  readonly passActual        = signal('');
  readonly passNueva         = signal('');
  readonly passConfirm       = signal('');
  readonly cambiandoPass     = signal(false);
  readonly passCambiada      = signal(false);
  readonly passError         = signal('');
  readonly bajaAbierta       = signal(false);
  readonly confirmarBaja     = signal(false);
  readonly textoConfirmaBaja = signal('');
  readonly eliminandoCuenta  = signal(false);

  cambiarPassword(): void {
    this.passError.set('');
    if (this.passNueva() !== this.passConfirm()) { this.passError.set('Las contraseñas nuevas no coinciden.'); return; }
    if (this.passNueva().length < 8) { this.passError.set('La contraseña debe tener al menos 8 caracteres.'); return; }
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
