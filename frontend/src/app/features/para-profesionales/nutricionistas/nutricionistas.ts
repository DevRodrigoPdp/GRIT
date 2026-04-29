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

  readonly features = [
    {
      titulo: 'Planes nutricionales',
      desc: 'Crea planes de dieta estructurados por comidas del día. Añade alimentos con cantidades exactas y el sistema calcula los macros automáticamente.',
    },
    {
      titulo: 'Base de alimentos propia',
      desc: 'Accede a nuestra base de datos de alimentos con proteínas, carbohidratos, grasas y calorías por cada 100 g. Busca por nombre y añade al instante.',
    },
    {
      titulo: 'Macros en tiempo real',
      desc: 'Mientras construyes el plan, el total de proteínas, carbos, grasas y kcal se actualiza al instante. Sin hojas de cálculo ni cálculos manuales.',
    },
    {
      titulo: 'Notas por comida',
      desc: 'Añade indicaciones específicas en cada comida: preparación, restricciones, alternativas o recordatorios que el atleta verá directamente en su app.',
    },
    {
      titulo: 'Gestión de atletas',
      desc: 'Panel centralizado con todos tus clientes. Consulta su historial nutricional, actualiza el plan cuando sea necesario y mantén el control de su progreso.',
    },
    {
      titulo: 'Titulación verificada',
      desc: 'Solo nutricionistas con titulación oficial verificada por GRIT pueden crear planes, garantizando que cada recomendación parte de un profesional certificado.',
    },
  ];

  readonly pasos = [
    { num: '01', titulo: 'Regístrate', desc: 'Crea tu cuenta indicando tu titulación como nutricionista. El proceso tarda menos de 5 minutos.' },
    { num: '02', titulo: 'Verifica tu titulación', desc: 'Sube tu documentación oficial. El equipo de GRIT la revisa y activa tu perfil profesional en 24-48 horas.' },
    { num: '03', titulo: 'Empieza a nutrir', desc: 'Invita a tus clientes con tu código único, crea sus planes nutricionales y actualízalos cuando lo necesiten.' },
  ];
}
