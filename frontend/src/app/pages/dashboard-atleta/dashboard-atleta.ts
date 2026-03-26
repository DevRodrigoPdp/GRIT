import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

type Tab = 'entrenamiento' | 'dieta';

@Component({
  selector: 'app-dashboard-atleta',
  standalone: true,
  templateUrl: './dashboard-atleta.html',
})
export class DashboardAtletaPage implements OnInit {
  readonly auth = inject(AuthService);

  readonly cargando = signal(true);

  readonly tabs = computed<Tab[]>(() => {
    const s = this.auth.servicio();
    if (s === 'ENTRENAMIENTO') return ['entrenamiento'];
    if (s === 'NUTRICION')     return ['dieta'];
    return ['entrenamiento', 'dieta'];
  });

  readonly tabActiva = signal<Tab>('entrenamiento');

  readonly labelTab: Record<Tab, string> = {
    entrenamiento: 'ENTRENAMIENTO',
    dieta:         'DIETA',
  };

  ngOnInit(): void {
    setTimeout(() => this.cargando.set(false), 2200);
  }

  seleccionarTab(t: Tab): void {
    this.tabActiva.set(t);
  }
}
