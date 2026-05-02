import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-entrenadores',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './entrenadores.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntrenadoresPage {
  readonly rutinaFeatures = [
    'Sesiones organizadas por días de la semana',
    'Define ejercicios, series, reps, peso y notas por bloque',
    'Clona rutinas entre atletas o semanas',
    'El atleta accede al plan actualizado en todo momento',
  ];

  readonly seguimientoFeatures = [
    'Check-ins de peso periódicos con gráfica histórica',
    'Registro de evolución semana a semana',
    'Comunicación directa con cada atleta desde el panel',
    'Ajusta el plan en tiempo real según el progreso',
  ];

  readonly gestionFeatures = [
    'Panel centralizado con todos tus atletas',
    'Código único de invitación para vincular clientes',
    'Insignia verificada visible en tu perfil público',
    'Solo entrenadores con titulación oficial verificada',
  ];

  readonly pasos = [
    { num: '01', titulo: 'Regístrate', desc: 'Crea tu cuenta como entrenador personal en menos de 5 minutos.' },
    { num: '02', titulo: 'Verifica tu titulación', desc: 'Sube tu documentación oficial. Activación en 24-48 horas.' },
    { num: '03', titulo: 'Empieza a entrenar', desc: 'Invita a tus atletas con tu código único y diseña sus rutinas.' },
  ];
}
