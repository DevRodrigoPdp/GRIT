import { ChangeDetectionStrategy, Component, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Plan {
  nombre: string;
  precioMensual: number;
  precioAnual: number;
  atletas: string;
  destacado: boolean;
  features: string[];
  cta: string;
  ctaRuta: string;
}

@Component({
  selector: 'app-precios',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './precios.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PreciosComponent {
  readonly anual = signal(false);

  readonly planes: Plan[] = [
    {
      nombre: 'FREE',
      precioMensual: 0,
      precioAnual: 0,
      atletas: 'Hasta 3 atletas',
      destacado: false,
      features: [
        'Gestión de hasta 3 atletas',
        'Creación de rutinas de entrenamiento',
        'Creación de planes de nutrición',
        'Chat con atletas',
        'Buscador de ejercicios y alimentos',
      ],
      cta: 'EMPEZAR GRATIS',
      ctaRuta: '/registro/entrenador',
    },
    {
      nombre: 'PRO',
      precioMensual: 19,
      precioAnual: 16,
      atletas: 'Hasta 15 atletas',
      destacado: true,
      features: [
        'Todo lo del plan Free',
        'Hasta 15 atletas',
        'Seguimiento de peso con gráficas',
        'Solicitudes de check-in al atleta',
        'Notas privadas por atleta',
        'Soporte por email',
      ],
      cta: 'EMPEZAR CON PRO',
      ctaRuta: '/registro/entrenador',
    },
    {
      nombre: 'ELITE',
      precioMensual: 39,
      precioAnual: 32,
      atletas: 'Atletas ilimitados',
      destacado: false,
      features: [
        'Todo lo del plan Pro',
        'Atletas ilimitados',
        'Analíticas de rendimiento',
        'Acceso anticipado a nuevas funciones',
        'Soporte prioritario',
      ],
      cta: 'EMPEZAR CON ELITE',
      ctaRuta: '/registro/entrenador',
    },
  ];

  precio(plan: Plan): number {
    return this.anual() ? plan.precioAnual : plan.precioMensual;
  }
}
