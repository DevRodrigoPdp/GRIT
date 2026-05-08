import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { EntrenadorService, AtletaAsignado, PerfilEntrenador } from './services/entrenador.service';
import { GestionNutricionComponent } from './components/gestion-nutricion/gestion-nutricion';
import { GestionEntrenamientoComponent } from './components/gestion-entrenamiento/gestion-entrenamiento';
import { PerfilEntrenadorVistaComponent } from './components/perfil-entrenador/perfil-entrenador-vista';
import { ComunicacionComponent } from './components/comunicacion/comunicacion';

type Tab    = 'ENTRENAMIENTO' | 'NUTRICION' | 'COMUNICACION';
type Filtro = 'TODOS' | 'ENTRENAMIENTO' | 'NUTRICION';
type Vista  = 'atletas' | 'perfil' | 'ajustes';

@Component({
  selector: 'app-dashboard-entrenador',
  standalone: true,
  imports: [GestionNutricionComponent, GestionEntrenamientoComponent, FormsModule, PerfilEntrenadorVistaComponent, ComunicacionComponent],
  templateUrl: './dashboard-entrenador.html',
})
export class DashboardEntrenadorPage implements OnInit {
  readonly auth      = inject(AuthService);
  readonly entrenador = inject(EntrenadorService);

  atletas       = signal<AtletaAsignado[]>([]);
  atletaActivo  = signal<AtletaAsignado | null>(null);
  tabActiva     = signal<Tab>('NUTRICION');
  vistaActual   = signal<Vista>('atletas');
  perfil        = signal<PerfilEntrenador | null>(null);

  busqueda       = signal('');
  filtroServicio = signal<Filtro>('TODOS');
  codigoCopiado  = signal(false);

  // ── Ajustes ───────────────────────────────────────────────────────────────
  readonly passAbierto        = signal(false);
  readonly passActual         = signal('');
  readonly passNueva          = signal('');
  readonly passConfirm        = signal('');
  readonly cambiandoPass      = signal(false);
  readonly passCambiada       = signal(false);
  readonly passError          = signal('');

  readonly bajaAbierta        = signal(false);
  readonly confirmarBaja      = signal(false);
  readonly textoConfirmaBaja  = signal('');
  readonly eliminandoCuenta   = signal(false);


  readonly navItems: { id: Vista; label: string }[] = [
    { id: 'atletas', label: 'ATLETAS' },
    { id: 'perfil',  label: 'MI PERFIL' },
    { id: 'ajustes', label: 'AJUSTES' },
  ];

  readonly atletasFiltrados = computed(() => {
    const q           = this.busqueda().toLowerCase().trim();
    const filtro      = this.filtroServicio();
    const tieneEntr   = this.auth.tituloEntrenamiento();
    const tieneNutr   = this.auth.tituloNutricion();
    return this.atletas().filter(a => {
      const coincideNombre   = !q || a.nombre.toLowerCase().includes(q);
      const coincideServicio =
        filtro === 'TODOS' ||
        a.servicio === filtro ||
        a.servicio === 'AMBOS';
      // Ocultar atletas cuyo servicio no cubre ninguna titulación del profesional
      const esRelevante =
        (tieneEntr && (a.servicio === 'ENTRENAMIENTO' || a.servicio === 'AMBOS')) ||
        (tieneNutr && (a.servicio === 'NUTRICION'     || a.servicio === 'AMBOS'));
      return coincideNombre && coincideServicio && esRelevante;
    });
  });

  readonly tabsDisponibles = computed<Tab[]>(() => {
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
    tabs.push('COMUNICACION');
    return tabs;
  });

  ngOnInit() {
    // Restaurar sesión si es necesario (por si el guard no lo hizo)
      this.entrenador.getPerfil().subscribe(p => this.perfil.set(p));
      this.entrenador.getMisAtletas().subscribe(a => {
        this.atletas.set(a);
        const primero = a.find(x => x.servicio === 'AMBOS') ?? a[0];
        if (primero) this.seleccionarAtleta(primero);
      });
  }

  navegarA(vista: Vista): void {
    this.vistaActual.set(vista);
    this.atletaActivo.set(null);
  }

  seleccionarAtleta(atleta: AtletaAsignado): void {
    this.atletaActivo.set(atleta);
    const tabs = this.tabsDisponibles();
    this.tabActiva.set(tabs[0] ?? 'ENTRENAMIENTO');
  }

  volver(): void {
    this.atletaActivo.set(null);
  }

  iniciales(nombre: string): string {
    return nombre.split(' ').slice(0, 2).map(p => p[0]).join('').toUpperCase();
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

  incluyeEntrenamiento(servicio: AtletaAsignado['servicio']): boolean {
    return servicio === 'ENTRENAMIENTO' || servicio === 'AMBOS';
  }

  incluyeNutricion(servicio: AtletaAsignado['servicio']): boolean {
    return servicio === 'NUTRICION' || servicio === 'AMBOS';
  }

  copiarCodigo(): void {
    const codigo = this.perfil()?.codigoInvitacion;
    if (!codigo) return;
    navigator.clipboard.writeText(codigo).then(() => {
      this.codigoCopiado.set(true);
      setTimeout(() => this.codigoCopiado.set(false), 2000);
    }).catch(() => {});
  }

  cambiarPassword(): void {
    this.passError.set('');
    if (this.passNueva() !== this.passConfirm()) {
      this.passError.set('Las contraseñas nuevas no coinciden.');
      return;
    }
    if (this.passNueva().length < 8) {
      this.passError.set('La contraseña debe tener al menos 8 caracteres.');
      return;
    }
    this.cambiandoPass.set(true);
    this.entrenador.cambiarPassword(this.passActual(), this.passNueva()).subscribe({
      next: () => {
        this.passCambiada.set(true);
        this.passActual.set('');
        this.passNueva.set('');
        this.passConfirm.set('');
        this.cambiandoPass.set(false);
      },
      error: () => {
        this.passError.set('Contraseña actual incorrecta.');
        this.cambiandoPass.set(false);
      },
    });
  }

  eliminarCuenta(): void {
    if (this.textoConfirmaBaja() !== 'ELIMINAR') return;
    this.eliminandoCuenta.set(true);
    this.entrenador.eliminarCuenta().subscribe({
      next: () => {
        this.eliminandoCuenta.set(false);
        this.auth.logout();
      },
      error: () => this.eliminandoCuenta.set(false),
    });
  }