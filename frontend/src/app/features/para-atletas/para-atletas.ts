import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-para-atletas',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './para-atletas.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ParaAtletasPage {

  readonly servicios = [
    {
      tag: 'Entrenamiento',
      titulo: 'ENTRENA CON UN PLAN.',
      desc: 'Tu entrenador diseña rutinas personalizadas adaptadas a tu nivel, objetivo y disponibilidad. Cada sesión, cada ejercicio, cada serie — todo pensado para ti.',
      features: ['Plan de entrenamiento actualizado', 'Sesiones organizadas por bloques', 'Seguimiento de progreso semana a semana', 'Check-in de peso con gráfica de evolución'],
      foto: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80',
    },
    {
      tag: 'Nutrición',
      titulo: 'COME CON PROPÓSITO.',
      desc: 'Tu nutricionista crea planes de dieta detallados con las comidas del día, cantidades exactas y macros calculados. Sin adivinanzas, sin dietas genéricas.',
      features: ['Plan nutricional personalizado', 'Comidas con cantidades y macros', 'Notas y recomendaciones de tu nutricionista', 'Actualización del plan cuando sea necesario'],
      foto: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=800&q=80',
    },
  ];

  readonly pasos = [
    { num: '01', titulo: 'Regístrate', desc: 'Crea tu cuenta indicando tu deporte, nivel y objetivo. Solo necesitas tu correo y unos minutos.' },
    { num: '02', titulo: 'Conéctate con tu profesional', desc: 'Introduce el código de invitación de tu entrenador o nutricionista para vincularte directamente a su panel.' },
    { num: '03', titulo: 'Sigue tu plan', desc: 'Accede a tu plan de entrenamiento y nutrición actualizado en cualquier momento. Comunícate con tu profesional desde la app.' },
  ];
}
