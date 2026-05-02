import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-para-profesionales',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './para-profesionales.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ParaProfesionalesPage {
  readonly entrenadorFeatures = [
    'Diseña rutinas por sesiones y días de la semana',
    'Solicita check-ins de peso y visualiza la evolución',
    'Comunicación directa con cada atleta',
    'Gestiona múltiples atletas desde un panel centralizado',
  ];

  readonly nutricionistaFeatures = [
    'Crea planes nutricionales con control de macros automático',
    'Base de alimentos propia con valores nutricionales',
    'Notas y recomendaciones personalizadas por comida',
    'Seguimiento y ajuste de planes en tiempo real',
  ];

  readonly commonFeatures = [
    { titulo: 'Verificación oficial',  desc: 'Insignia de profesional certificado visible en tu perfil público.' },
    { titulo: 'Sin comisiones',        desc: 'Tú fijas tus tarifas. GRIT solo cobra por el acceso a la herramienta.' },
    { titulo: 'Hasta 3 atletas gratis', desc: 'Empieza sin coste. Escala cuando necesites más capacidad.' },
    { titulo: 'Soporte prioritario',   desc: 'Acceso a soporte directo para profesionales verificados.' },
  ];
}
