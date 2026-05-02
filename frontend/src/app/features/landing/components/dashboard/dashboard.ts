import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html',
})
export class DashboardComponent {
  readonly entrenadorFeatures = [
    'Panel centralizado para gestionar múltiples atletas',
    'Entrenadores: diseña rutinas y haz seguimiento semanal',
    'Nutricionistas: crea planes de alimentación con control de macros',
    'Comunicación directa con cada atleta desde el dashboard',
    'Insignia de profesional certificado visible en tu perfil',
  ];

  readonly atletaFeatures = [
    'Acceso a tu plan de entrenamiento actualizado en todo momento',
    'Seguimiento de tu progreso semana a semana',
    'Planes nutricionales diseñados por tu entrenador',
    'Comunicación directa con tu profesional certificado',
    'Historial completo de tu evolución física',
  ];
}
