import { Component, computed, signal } from '@angular/core';
import { Metric } from '../../models/grit.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './dashboard.html',
})
export class DashboardComponent {
  readonly isSyncing = signal(true);

  readonly metrics = signal<Metric[]>([
    { label: 'VOLUMEN', value: 84.2, display: '84.2%' },
    { label: 'INTENSIDAD', value: 91.5, display: '91.5%' },
    { label: 'RECUPERACIÓN', value: 68.0, display: '68.0%' },
    { label: 'POTENCIA', value: 77.4, display: '77.4%' },
  ]);

  readonly peakMetric = computed(() =>
    this.metrics().reduce((max, m) => (m.value > max.value ? m : max))
  );

  readonly avgPerformance = computed(() => {
    const vals = this.metrics().map(m => m.value);
    return (vals.reduce((a, b) => a + b, 0) / vals.length).toFixed(1);
  });

  readonly svgPath = `M 0 185
    C 50 183 80 175 130 155
    C 180 135 200 115 250 90
    C 290 70 310 110 360 105
    C 400 100 420 70 470 50
    C 510 34 530 28 570 22
    C 610 16 640 30 680 25
    C 720 20 760 18 800 15`;

  readonly svgAreaPath = `M 0 200
    L 0 185
    C 50 183 80 175 130 155
    C 180 135 200 115 250 90
    C 290 70 310 110 360 105
    C 400 100 420 70 470 50
    C 510 34 530 28 570 22
    C 610 16 640 30 680 25
    C 720 20 760 18 800 15
    L 800 200 Z`;
}
