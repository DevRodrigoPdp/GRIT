import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';
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
  imports: [GestionNutricionComponent, GestionEntrenamientoComponent, FormsModule, PerfilEntrenadorVistaComponent, ComunicacionComponent, NgClass],
  templateUrl: './dashboard-entrenador.html',
})
export class DashboardEntrenadorPage implements OnInit {
  readonly auth      = inject(AuthService);
  readonly entrenador = inject(EntrenadorService);
  readonly theme     = inject(ThemeService);

  atletas       = signal<AtletaAsignado[]>([]);
  atletaActivo  = signal<AtletaAsignado | null>(null);
  tabActiva     = signal<Tab>('NUTRICION');
  vistaActual   = signal<Vista>('atletas');
  perfil        = signal<PerfilEntrenador | null>(null);

  busqueda       = signal('');
  filtroServicio = signal<Filtro>('TODOS');
  codigoCopiado  = signal(false);

  // ── Sidebar ───────────────────────────────────────────────────────────────
  sidebarPinned  = signal(false);
  sidebarHovered = signal(false);
  mobileMenuOpen = signal(false);
  readonly sidebarOpen = computed(() => this.sidebarPinned() || this.sidebarHovered() || this.mobileMenuOpen());

  // ── Desconectar atleta ────────────────────────────────────────────────────
  readonly confirmandoDesconectar = signal<string | null>(null);
  readonly desconectando          = signal(false);

  // ── Ajustes ───────────────────────────────────────────────────────────────
  readonly passAbierto        = signal(false);
  readonly passActual         = signal('');
  readonly passNueva          = signal('');
  readonly passConfirm        = signal('');
  readonly cambiandoPass      = signal(false);
  readonly passCambiada       = signal(false);
  readonly passError          = signal('');
  readonly showPassActual     = signal(false);
  readonly showPassNueva      = signal(false);
  readonly showPassConfirm    = signal(false);

  private readonly PASS_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{8,}$/;

  readonly passReglas = computed(() => {
    const v = this.passNueva();
    return [
      { label: 'Mínimo 8 caracteres',  ok: v.length >= 8 },
      { label: 'Una mayúscula',         ok: /[A-Z]/.test(v) },
      { label: 'Una minúscula',         ok: /[a-z]/.test(v) },
      { label: 'Un número',             ok: /\d/.test(v) },
      { label: 'Un carácter especial',  ok: /[^a-zA-Z\d]/.test(v) },
    ];
  });

  readonly bajaAbierta        = signal(false);
  readonly confirmarBaja      = signal(false);
  readonly textoConfirmaBaja  = signal('');
  readonly eliminandoCuenta   = signal(false);

  // ── Editar perfil ─────────────────────────────────────────────────────────
  readonly editarPerfilAbierto  = signal(false);
  readonly editNombre           = signal('');
  readonly editDescripcion      = signal('');
  readonly editExperiencia      = signal(0);
  readonly editMasters          = signal<string[]>([]);
  readonly nuevoMaster          = signal('');
  readonly guardandoPerfil      = signal(false);
  readonly perfilGuardado       = signal(false);
  readonly perfilError          = signal('');


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
    this.entrenador.getPerfil().subscribe(p => this.perfil.set(p));
    this.entrenador.getMisAtletas().subscribe(a => this.atletas.set(a));
  }

  navegarA(vista: Vista): void {
    this.vistaActual.set(vista);
    this.atletaActivo.set(null);
    this.mobileMenuOpen.set(false);
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
    if (!this.PASS_REGEX.test(this.passNueva())) {
      this.passError.set('La contraseña no cumple los requisitos de seguridad.');
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

  desconectarAtleta(atletaId: string): void {
    if (this.confirmandoDesconectar() !== atletaId) {
      this.confirmandoDesconectar.set(atletaId);
      return;
    }
    this.desconectando.set(true);
    this.entrenador.desconectarAtleta(atletaId).subscribe({
      next: () => {
        this.atletas.update(a => a.filter(x => x.id !== atletaId));
        this.atletaActivo.set(null);
        this.confirmandoDesconectar.set(null);
        this.desconectando.set(false);
      },
      error: () => {
        this.confirmandoDesconectar.set(null);
        this.desconectando.set(false);
      },
    });
  }

  abrirEditarPerfil(): void {
    const p = this.perfil();
    this.editNombre.set(p?.nombre ?? '');
    this.editDescripcion.set(p?.descripcion ?? '');
    this.editExperiencia.set(p?.experienciaAnos ?? 0);
    this.editMasters.set([...(p?.masters ?? [])]);
    this.perfilGuardado.set(false);
    this.perfilError.set('');
    this.editarPerfilAbierto.set(true);
  }

  agregarMaster(): void {
    const m = this.nuevoMaster().trim();
    if (!m || this.editMasters().includes(m)) return;
    this.editMasters.update(l => [...l, m]);
    this.nuevoMaster.set('');
  }

  eliminarMaster(idx: number): void {
    this.editMasters.update(l => l.filter((_, i) => i !== idx));
  }

  guardarPerfil(): void {
    this.guardandoPerfil.set(true);
    this.perfilError.set('');
    this.entrenador.actualizarPerfil({
      nombre:         this.editNombre(),
      descripcion:    this.editDescripcion(),
      experienciaAnos: this.editExperiencia(),
      masters:        this.editMasters(),
    }).subscribe({
      next: () => {
        this.perfil.update(p => p ? {
          ...p,
          nombre:          this.editNombre(),
          descripcion:     this.editDescripcion(),
          experienciaAnos: this.editExperiencia(),
          masters:         this.editMasters(),
        } : p);
        this.perfilGuardado.set(true);
        this.guardandoPerfil.set(false);
        this.editarPerfilAbierto.set(false);
      },
      error: () => {
        this.perfilError.set('Error al guardar. Inténtalo de nuevo.');
        this.guardandoPerfil.set(false);
      },
    });
  }

}
