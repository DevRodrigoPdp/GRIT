import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgTemplateOutlet } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
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
import { VistaEntrenamientoComponent } from './components/vista-entrenamiento/vista-entrenamiento';
import { VistaDietaComponent } from './components/vista-dieta/vista-dieta';
import { VistaPerfilAtletaComponent } from './components/vista-perfil/vista-perfil-atleta';
import { AjustesAtletaComponent } from './components/ajustes/ajustes-atleta';
import { VistaProfesionalesComponent } from './components/vista-profesionales/vista-profesionales';

type Vista = 'entrenamiento' | 'dieta' | 'cuaderno' | 'profesionales' | 'perfil' | 'ajustes';

interface ChartPoint { x: number; y: number; peso: number; fecha: string; }

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
    FormsModule,
    NgTemplateOutlet,
  ],
  templateUrl: './dashboard-atleta.html',
})
export class DashboardAtletaPage implements OnInit {
  readonly auth   = inject(AuthService);
  readonly atleta = inject(AtletaService);

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
  readonly chartData = computed<{ points: ChartPoint[]; polyline: string } | null>(() => {
    const pesos = this.historialPesos();
    if (pesos.length < 2) return null;
    const W = 460, H = 60, padX = 20, padY = 8;
    const weights = pesos.map(p => p.pesoKg);
    const minW = Math.min(...weights) - 1;
    const maxW = Math.max(...weights) + 1;
    const toX = (i: number) => padX + (i / (pesos.length - 1)) * (W - 2 * padX);
    const toY = (w: number) => padY + H - ((w - minW) / (maxW - minW)) * H;
    const points: ChartPoint[] = pesos.map((p, i) => ({
      x: toX(i), y: toY(p.pesoKg), peso: p.pesoKg, fecha: p.fecha,
    }));
    return { points, polyline: points.map(p => `${p.x},${p.y}`).join(' ') };
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
  navegarA(vista: Vista): void { this.vistaActual.set(vista); }

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
