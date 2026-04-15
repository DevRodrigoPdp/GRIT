import { Component, inject, computed, output, input } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { PerfilEntrenador } from '../../services/entrenador.service';

const TITULACION_ENT_LABEL: Record<string, string> = {
  GRADO_CAFYD:   'Grado en CAFYD — Ciencias de la Actividad Física y del Deporte',
  TSAF_TSEAS:    'TSAF / TSEAS — Técnico Superior en Animación de Actividades Físicas',
  CERT_AFDA0210: 'Certificado de Profesionalidad AFDA0210',
};

const TITULACION_NUTR_LABEL: Record<string, string> = {
  GRADO_NUTRICION_DIETETICA: 'Grado en Nutrición Humana y Dietética',
  TSD:                       'TSD — Técnico Superior en Dietética',
};

@Component({
  selector: 'app-perfil-entrenador-vista',
  standalone: true,
  templateUrl: './perfil-entrenador-vista.html',
})
export class PerfilEntrenadorVistaComponent {
  readonly auth   = inject(AuthService);
  readonly volver = output<void>();
  readonly perfil = input<PerfilEntrenador | null>(null);

  readonly nombre = computed(() => this.perfil()?.nombre ?? this.auth.nombre() ?? '');

  readonly iniciales = computed(() => {
    return this.nombre()
      .split(' ')
      .slice(0, 2)
      .map(p => p[0])
      .join('')
      .toUpperCase() || '?';
  });

  readonly titulacionEntLabel = computed(() => {
    const t = this.perfil()?.titulacionEntrenamiento;
    return t ? TITULACION_ENT_LABEL[t] ?? t : null;
  });

  readonly titulacionNutrLabel = computed(() => {
    const t = this.perfil()?.titulacionNutricion;
    return t ? TITULACION_NUTR_LABEL[t] ?? t : null;
  });

  readonly experiencia = computed(() => this.perfil()?.experienciaAnos ?? null);
  readonly descripcion = computed(() => this.perfil()?.descripcion ?? null);
  readonly correo      = computed(() => this.perfil()?.correo ?? null);
}
