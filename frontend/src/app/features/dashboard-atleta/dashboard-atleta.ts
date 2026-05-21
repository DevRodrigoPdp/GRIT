import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgTemplateOutlet, NgClass } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';
import {
  AtletaService,
  PerfilAtleta,
  PlanEntrenamiento,
  PlanNutricion,
  SolicitudCheckIn,
  CheckInPeso,
  NotaNutricionista,
  ProfesionalAsignado,
} from './services/atleta.service';
import { ComunicacionAtletaComponent } from './components/comunicacion/comunicacion-atleta';
import { LucidePin } from '@lucide/angular';
import { VistaEntrenamientoComponent } from './components/vista-entrenamiento/vista-entrenamiento';
import { VistaDietaComponent } from './components/vista-dieta/vista-dieta';
import { VistaPerfilAtletaComponent } from './components/vista-perfil/vista-perfil-atleta';
import { AjustesAtletaComponent } from './components/ajustes/ajustes-atleta';
import { VistaProfesionalesComponent } from './components/vista-profesionales/vista-profesionales';
import { ScrollIndicatorDirective } from '../../shared/directives/scroll-indicator.directive';
import { NgApexchartsModule } from 'ng-apexcharts';
import type { ApexOptions } from 'ng-apexcharts';

type Vista = 'entrenamiento' | 'dieta' | 'cuaderno' | 'profesionales' | 'perfil' | 'ajustes';

@Component({
  selector: 'app-dashboard-atleta',
  standalone: true,
  imports: [
    ComunicacionAtletaComponent,
    VistaEntrenamientoComponent,
    VistaDietaComponent,
    VistaPerfilAtletaComponent,
    AjustesAtletaComponent,
    VistaProfesionalesComponent,
    ScrollIndicatorDirective,
    NgApexchartsModule,
    FormsModule,
    NgTemplateOutlet,
    NgClass,
    LucidePin,
  ],
  templateUrl: './dashboard-atleta.html',
})
export class DashboardAtletaPage implements OnInit {
  readonly auth   = inject(AuthService);
  readonly atleta = inject(AtletaService);
  readonly theme  = inject(ThemeService);

  readonly cargando = signal(true);

  // ── Datos ─────────────────────────────────────────────────────────────────
  readonly perfilAtleta       = signal<PerfilAtleta | null>(null);
  readonly profesionales      = signal<ProfesionalAsignado[]>([]);
  readonly planEntrenamiento  = signal<PlanEntrenamiento | null>(null);
  readonly planNutricion      = signal<PlanNutricion | null>(null);
  readonly solicitudCheckIn   = signal<SolicitudCheckIn | null>(null);
  readonly historialPesos     = signal<CheckInPeso[]>([]);
  readonly notasNutricionista = signal<NotaNutricionista[]>([]);

  // ── Navegación ────────────────────────────────────────────────────────────
  readonly vistaActual = signal<Vista>('entrenamiento');

  // ── Sidebar ───────────────────────────────────────────────────────────────
  sidebarPinned  = signal(false);
  sidebarHovered = signal(false);
  mobileMenuOpen = signal(false);
  readonly sidebarOpen = computed(() => this.sidebarPinned() || this.sidebarHovered() || this.mobileMenuOpen());

  readonly navItems = computed(() => {
    const s = this.auth.servicio();
    const items: { id: Vista; label: string }[] = [];
    if (s === 'ENTRENAMIENTO' || s === 'AMBOS') items.push({ id: 'entrenamiento', label: 'ENTRENAMIENTO' });
    if (s === 'NUTRICION'     || s === 'AMBOS') items.push({ id: 'dieta',          label: 'DIETA'           });
    items.push({ id: 'cuaderno',      label: 'COMUNICACIÓN'  });
    items.push({ id: 'profesionales', label: 'PROFESIONALES' });
    items.push({ id: 'perfil',        label: 'MI PERFIL'     });
    items.push({ id: 'ajustes',       label: 'AJUSTES'       });
    return items;
  });

  // ── Peso (cuaderno check-in widget) ───────────────────────────────────────
  readonly pesoInputValor = signal('');
  readonly enviandoPeso   = signal(false);

  // ── Comunicación ─────────────────────────────────────────────────────────
  readonly contextoChat = signal<'ENTRENAMIENTO' | 'NUTRICION'>('ENTRENAMIENTO');

  readonly profesionalEntrenador = computed(() =>
    this.profesionales().find(p => p.rol === 'ENTRENADOR')
  );
  readonly profesionalNutricionista = computed(() =>
    this.profesionales().find(p => p.rol === 'NUTRICIONISTA')
  );

  // ── Computeds ─────────────────────────────────────────────────────────────
  readonly chartOptions = computed<ApexOptions>(() => {
    const pesos = this.historialPesos();
    return {
      series: [{ name: 'Peso', data: pesos.map(p => p.pesoKg) }],
      chart: { type: 'area', height: 180, toolbar: { show: false }, zoom: { enabled: false }, animations: { enabled: true, speed: 1200, animateGradually: { enabled: true, delay: 200 }, dynamicAnimation: { enabled: true, speed: 600 } }, background: 'transparent', foreColor: 'rgba(150,150,150,0.8)' },
      stroke: { curve: 'smooth', width: 2, colors: ['#2ED38D'] },
      fill: { type: 'gradient', gradient: { colorStops: [{ offset: 0, color: '#2ED38D', opacity: 0.25 }, { offset: 100, color: '#2ED38D', opacity: 0.02 }] } },
      markers: { size: 4, colors: ['#2ED38D'], strokeColors: 'transparent', hover: { size: 6 } },
      xaxis: { categories: pesos.map(p => p.fecha), labels: { style: { fontSize: '10px', colors: 'rgba(150,150,150,0.75)' } }, axisBorder: { show: false }, axisTicks: { show: false }, tooltip: { enabled: false } },
      yaxis: { labels: { style: { fontSize: '10px', colors: 'rgba(150,150,150,0.75)' }, formatter: (v: number) => `${v} kg`, offsetX: -4 }, tickAmount: 3 },
      grid: { borderColor: 'rgba(180,180,180,0.10)', strokeDashArray: 3, xaxis: { lines: { show: false } }, yaxis: { lines: { show: true } }, padding: { left: 10, right: 8 } },
      tooltip: { theme: 'dark', x: { show: true }, y: { formatter: (v: number) => `${v} kg` } },
      dataLabels: { enabled: false },
    };
  });

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    // Restaurar sesión si fue necesario (respaldo si guard no lo hizo)
    if (!this.auth.rol()) {
      this.auth.me().subscribe();
    }
    this.cargarDatos();
  }

  private cargarDatos(): void {
    const s = this.auth.servicio();
    const incluyeEntrenamiento = s === 'ENTRENAMIENTO' || s === 'AMBOS';
    const incluyeNutricion     = s === 'NUTRICION'     || s === 'AMBOS';

    if (!incluyeEntrenamiento) {
      this.vistaActual.set('dieta');
      this.contextoChat.set('NUTRICION');
    }

    this.atleta.getPerfil().subscribe(p => this.perfilAtleta.set(p));
    this.atleta.getProfesionalesAsignados().subscribe(p => this.profesionales.set(p));
    this.atleta.getSolicitudCheckIn().subscribe(s => this.solicitudCheckIn.set(s));
    this.atleta.getHistorialPesos().subscribe(h => this.historialPesos.set(h));

    if (incluyeEntrenamiento) {
      this.atleta.getPlanEntrenamiento().subscribe(p => this.planEntrenamiento.set(p));
    }

    if (incluyeNutricion) {
      this.atleta.getPlanNutricion().subscribe(p => this.planNutricion.set(p));    }

    this.cargando.set(false);
  }

  // ── Navegación ────────────────────────────────────────────────────────────
  navegarA(vista: Vista): void { this.vistaActual.set(vista); this.mobileMenuOpen.set(false); }

  // ── Registro de peso (cuaderno check-in) ──────────────────────────────────
  pesoInputValido(): boolean {
    const v = Number(this.pesoInputValor());
    return !isNaN(v) && v >= 30 && v <= 300;
  }

  registrarPeso(): void {
    const valor = Number(this.pesoInputValor());
    const solicitud = this.solicitudCheckIn();
    if (!solicitud || isNaN(valor) || valor < 30 || valor > 300) return;
    this.enviandoPeso.set(true);
    this.atleta.registrarPeso(solicitud.id, valor).subscribe(entrada => {
      this.historialPesos.update(h => [...h, entrada].sort((a, b) => a.fecha.localeCompare(b.fecha)));
      this.solicitudCheckIn.set(null);
      this.pesoInputValor.set('');
      this.enviandoPeso.set(false);
    });
  }

  // ── Handlers para outputs de componentes hijos ────────────────────────────
  onPesoRegistrado(entrada: CheckInPeso): void {
    this.historialPesos.update(h => [...h, entrada].sort((a, b) => a.fecha.localeCompare(b.fecha)));
    this.solicitudCheckIn.set(null);
  }

  onPerfilActualizado(p: PerfilAtleta): void { this.perfilAtleta.set(p); }

  onProfesionalesActualizados(p: ProfesionalAsignado[]): void { this.profesionales.set(p); }

  // ── Helper (cuaderno graficaPeso template) ────────────────────────────────
  formatFecha(fecha: string): string {
    const [year, month, day] = fecha.split('-');
    const meses = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic'];
    return `${parseInt(day)} ${meses[parseInt(month) - 1]} ${year}`;
  }
}
