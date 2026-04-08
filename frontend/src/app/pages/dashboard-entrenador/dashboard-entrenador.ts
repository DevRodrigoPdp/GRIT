import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { EntrenadorService, AtletaAsignado } from '../../services/entrenador.service';
import { GestionNutricionComponent } from '../../components/gestion-nutricion/gestion-nutricion';

type Tab = 'ENTRENAMIENTO' | 'NUTRICION';
type Filtro = 'TODOS' | 'ENTRENAMIENTO' | 'NUTRICION';

@Component({
  selector: 'app-dashboard-entrenador',
  standalone: true,
  imports: [GestionNutricionComponent, FormsModule],
  templateUrl: './dashboard-entrenador.html',
})
export class DashboardEntrenadorPage implements OnInit {
  readonly auth      = inject(AuthService);
  private entrenador = inject(EntrenadorService);

  atletas      = signal<AtletaAsignado[]>([]);
  atletaActivo = signal<AtletaAsignado | null>(null);
  tabActiva    = signal<Tab>('NUTRICION');

  busqueda      = signal('');
  filtroServicio = signal<Filtro>('TODOS');

  atletasFiltrados = computed(() => {
    const q       = this.busqueda().toLowerCase().trim();
    const filtro  = this.filtroServicio();
    return this.atletas().filter(a => {
      const coincideNombre = !q || a.nombre.toLowerCase().includes(q);
      const coincideServicio =
        filtro === 'TODOS' ||
        a.servicio === filtro ||
        a.servicio === 'AMBOS';
      return coincideNombre && coincideServicio;
    });
  });

  /**
   * Tabs visibles para el atleta activo:
   * intersección entre los títulos del entrenador y el servicio contratado por el atleta.
   */
  tabsDisponibles = computed<Tab[]>(() => {
    const atleta = this.atletaActivo();
    if (!atleta) return [];

    const tieneEntrenamiento =
      this.auth.tituloEntrenamiento() &&
      (atleta.servicio === 'ENTRENAMIENTO' || atleta.servicio === 'AMBOS');

    const tieneNutricion =
      this.auth.tituloNutricion() &&
      (atleta.servicio === 'NUTRICION' || atleta.servicio === 'AMBOS');

    const tabs: Tab[] = [];
    if (tieneEntrenamiento) tabs.push('ENTRENAMIENTO');
    if (tieneNutricion)     tabs.push('NUTRICION');
    return tabs;
  });

  ngOnInit() {
    this.auth.me().subscribe();
    this.entrenador.getMisAtletas().subscribe(a => this.atletas.set(a));
  }

  seleccionarAtleta(atleta: AtletaAsignado) {
    this.atletaActivo.set(atleta);
    const tabs = this.tabsDisponibles();
    this.tabActiva.set(tabs[0] ?? 'ENTRENAMIENTO');
  }

  volver() {
    this.atletaActivo.set(null);
  }

  iniciales(nombre: string): string {
    return nombre
      .split(' ')
      .slice(0, 2)
      .map(p => p[0])
      .join('')
      .toUpperCase();
  }

  nivelLabel(nivel: AtletaAsignado['nivel']): string {
    const map: Record<AtletaAsignado['nivel'], string> = {
      PRINCIPIANTE: 'Principiante',
      INTERMEDIO:   'Intermedio',
      AVANZADO:     'Avanzado',
      ELITE:        'Élite',
    };
    return map[nivel];
  }
}
