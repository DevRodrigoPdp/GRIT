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

  readonly features = [
    {
      titulo: 'Panel de atletas',
      desc: 'Gestiona todos tus atletas desde un único panel. Consulta su perfil, historial de entrenamientos y métricas de progreso en tiempo real.',
    },
    {
      titulo: 'Diseño de rutinas',
      desc: 'Crea rutinas personalizadas organizadas por sesiones. Define ejercicios, series, repeticiones, peso y añade notas específicas para cada bloque.',
    },
    {
      titulo: 'Seguimiento de progreso',
      desc: 'Solicita check-ins de peso periódicos y visualiza la evolución de cada atleta mediante gráficas históricas de su composición corporal.',
    },
    {
      titulo: 'Comunicación directa',
      desc: 'Chat integrado con cada atleta para resolver dudas, ajustar el plan sobre la marcha y mantener la motivación en todo momento.',
    },
    {
      titulo: 'Titulación verificada',
      desc: 'Solo entrenadores con titulación oficial verificada por GRIT pueden gestionar atletas, garantizando un servicio de calidad contrastada.',
    },
    {
      titulo: 'Código de invitación',
      desc: 'Comparte tu código único con tus clientes. Al registrarse lo introducen y quedan vinculados a tu panel automáticamente.',
    },
  ];

  readonly pasos = [
    { num: '01', titulo: 'Regístrate', desc: 'Crea tu cuenta indicando tu titulación como entrenador personal. El proceso tarda menos de 5 minutos.' },
    { num: '02', titulo: 'Verifica tu titulación', desc: 'Sube tu documentación oficial. El equipo de GRIT la revisa y activa tu perfil profesional en 24-48 horas.' },
    { num: '03', titulo: 'Empieza a trabajar', desc: 'Invita a tus atletas con tu código único, diseña sus rutinas y haz seguimiento de su progreso desde tu panel.' },
  ];
}
