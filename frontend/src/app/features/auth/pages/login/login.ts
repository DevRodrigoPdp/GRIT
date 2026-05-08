import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private fb   = inject(FormBuilder);
  readonly auth = inject(AuthService);

  readonly showPassword = signal(false);

  private readonly errorMessages: Record<string, { titulo: string; desc: string }> = {
    credenciales_invalidas: {
      titulo: 'Credenciales incorrectas',
      desc: 'El correo electrónico o la contraseña no son correctos. Revísalos e inténtalo de nuevo.',
    },
    cuenta_pendiente: {
      titulo: 'Cuenta pendiente de revisión',
      desc: 'Tus credenciales son correctas, pero tu cuenta todavía no ha sido aprobada por el equipo de GRIT. Te avisaremos por correo en cuanto esté lista.',
    },
    cuenta_rechazada: {
      titulo: 'Cuenta rechazada',
      desc: 'Tu solicitud ha sido revisada y no ha podido ser aprobada. Contacta con soporte para más información.',
    },
    error_servidor: {
      titulo: 'Error de conexión',
      desc: 'No hemos podido conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo.',
    },
  };

  form = this.fb.group({
    correo:   ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  get errorActual(): { titulo: string; desc: string } | null {
    const key = this.auth.loginError();
    return key ? (this.errorMessages[key] ?? null) : null;
  }

  fieldError(name: string): boolean {
    const c = this.form.get(name);
    return !!(c?.invalid && c?.touched);
  }

  onInput(): void {
    this.auth.loginError.set(null);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { correo, password } = this.form.value;
    this.auth.login(correo!, password!).subscribe();
  }
}
