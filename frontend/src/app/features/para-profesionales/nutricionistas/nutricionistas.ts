import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-nutricionistas',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './nutricionistas.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NutricionistasPage {
  readonly planFeatures = [
    'Comidas organizadas por desayuno, almuerzo, merienda y cena',
    'Añade alimentos con gramos exactos en segundos',
    'Notas e indicaciones por comida visibles al atleta',
    'Clona o edita planes sin empezar desde cero',
  ];

  readonly macroFeatures = [
    'Proteínas, carbos, grasas y kcal calculados al instante',
    'Base de datos de alimentos con valores nutricionales reales',
    'Búsqueda instantánea de alimentos por nombre',
    'Totales diarios actualizados mientras construyes el plan',
  ];

  readonly gestionFeatures = [
    'Panel centralizado con todos tus clientes',
    'Historial nutricional y registro de evolución por cliente',
    'Insignia verificada visible en tu perfil público',
    'Solo nutricionistas con titulación oficial verificada',
  ];

  readonly pasos = [
    { num: '01', titulo: 'Regístrate', desc: 'Crea tu cuenta como nutricionista en menos de 5 minutos.' },
    { num: '02', titulo: 'Verifica tu titulación', desc: 'Sube tu documentación oficial. Activación en 24-48 horas.' },
    { num: '03', titulo: 'Empieza a nutrir', desc: 'Invita a tus clientes con tu código único y crea sus planes.' },
  ];
}
