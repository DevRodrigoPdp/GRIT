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
    'Diseño de planes de entrenamiento personalizados',
    'Seguimiento nutricional con control de macros',
    'Historial de progreso y métricas por atleta',
    'Insignia de entrenador certificado visible en tu perfil',
  ];

  readonly atletaFeatures = [
    'Acceso a tu plan de entrenamiento actualizado en todo momento',
    'Seguimiento de tu progreso semana a semana',
    'Planes nutricionales diseñados por tu entrenador',
    'Comunicación directa con tu profesional certificado',
    'Historial completo de tu evolución física',
  ];
}
