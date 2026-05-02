import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { VerificationItem } from '../../../../shared/models/grit.models';

@Component({
  selector: 'app-verification',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './verification.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VerificationComponent {
  readonly items = signal<VerificationItem[]>([
    {
      number: '01',
      title: 'CREA TU PERFIL',
      description:
        'Regístrate indicando si eres entrenador o atleta. Los entrenadores aportan su titulación oficial para acceder a la gestión completa de sus atletas.',
    },
    {
      number: '02',
      title: 'VERIFICAMOS TUS CREDENCIALES',
      description:
        'Validamos cada titulación para garantizar que solo profesionales certificados gestionan atletas en la plataforma. Tu seguridad como atleta, nuestra prioridad.',
    },
    {
      number: '03',
      title: 'ENTRENA Y PROGRESA',
      description:
        'Tu entrenador diseña y ajusta planes personalizados de entrenamiento y nutrición desde un panel centralizado. Tú solo te centras en rendir.',
      isLast: true,
    },
  ]);
}
