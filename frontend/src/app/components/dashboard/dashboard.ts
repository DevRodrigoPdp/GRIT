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

  // SVG chart: sinusoidal path across a 800x200 viewBox
  readonly svgPath = `M 0 160
    C 40 160 60 140 100 120
    C 140 100 160 80 200 60
    C 240 40 260 30 300 25
    C 340 20 360 35 400 55
    C 440 75 460 90 500 95
    C 540 100 560 85 600 70
    C 640 55 660 40 700 35
    C 730 30 760 45 800 55`;

  readonly svgAreaPath = `M 0 200
    L 0 160
    C 40 160 60 140 100 120
    C 140 100 160 80 200 60
    C 240 40 260 30 300 25
    C 340 20 360 35 400 55
    C 440 75 460 90 500 95
    C 540 100 560 85 600 70
    C 640 55 660 40 700 35
    C 730 30 760 45 800 55
    L 800 200 Z`;
}
