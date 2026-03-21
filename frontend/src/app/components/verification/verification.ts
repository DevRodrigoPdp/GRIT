import { Component, signal } from '@angular/core';
import { VerificationItem } from '../../models/grit.models';

@Component({
  selector: 'app-verification',
  standalone: true,
  imports: [],
  templateUrl: './verification.html',
})
export class VerificationComponent {
  readonly items = signal<VerificationItem[]>([
    {
      number: '01',
      title: 'NUTRICIÓN CON CREDENCIALES',
      description:
        'Acceso exclusivo a planes nutricionales diseñados por especialistas certificados. Cada macro calculado con precisión científica para maximizar tu composición corporal.',
    },
    {
      number: '02',
      title: 'INTEGRACIÓN BIOMÉTRICA',
      description:
        'Métricas sincronizadas en tiempo real desde tus dispositivos. Frecuencia cardíaca, VFC, sueño y recuperación muscular unificados en un único dashboard de mando.',
    },
    {
      number: '03',
      title: 'ANÁLISIS DE VÍDEO TÉCNICO',
      description:
        'Procesamiento de vídeo con IA para analizar biomecánica de movimiento. Corrección postural automatizada y detección de patrones de fatiga neuro-muscular.',
      isLast: true,
    },
  ]);
}
