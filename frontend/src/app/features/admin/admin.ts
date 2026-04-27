import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, EntrenadorPendienteDTO, UsuarioDTO } from './services/admin.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.html',
})
export class AdminPage implements OnInit {
  private adminService = inject(AdminService);
  readonly auth = inject(AuthService);

  // Navegación
  readonly seccionActual = signal<'SOLICITUDES' | 'USUARIOS'>('SOLICITUDES');

  // Estados Solicitudes con paginación
  readonly solicitudes = signal<EntrenadorPendienteDTO[]>([]);
  readonly loadingSolicitudes = signal(false);
  readonly paginaSolicitudes = signal(0);
  readonly totalPaginasSolicitudes = signal(0);
  readonly pageSize = 10;
  
  // Estados Usuarios
  readonly usuarios = signal<UsuarioDTO[]>([]);
  readonly loadingUsuarios = signal(false);
  readonly filtroRol = signal<'TODOS' | 'ATLETA' | 'ENTRENADOR'>('TODOS');

  // Comunes
  readonly busqueda = signal('');
  readonly error = signal<string | null>(null);

  // Selección y Modales
  readonly entrenadorSeleccionado = signal<EntrenadorPendienteDTO | null>(null);
  readonly usuarioSeleccionado = signal<UsuarioDTO | null>(null);
  readonly mostrarModalRechazo = signal(false);
  readonly motivoRechazo = signal('');
  readonly mostrarModalConfirmacion = signal(false);
  readonly mostrarModalExito = signal(false);
  readonly mostrarModalEliminacion = signal(false);
  readonly nombreAprobado = signal('');

  // Solicitudes filtradas
  readonly solicitudesFiltradas = computed(() => {
    const query = this.busqueda().toLowerCase().trim();
    if (!query) return this.solicitudes();
    return this.solicitudes().filter(s => 
      s.nombre.toLowerCase().includes(query) || 
      s.correo.toLowerCase().includes(query)
    );
  });

  // Usuarios filtrados
  readonly usuariosFiltrados = computed(() => {
    let result = this.usuarios();
    
    // Filtrar por rol
    if (this.filtroRol() !== 'TODOS') {
      result = result.filter(u => u.rol === this.filtroRol());
    }

    // Filtrar por búsqueda
    const query = this.busqueda().toLowerCase().trim();
    if (query) {
      result = result.filter(u => 
        u.nombre.toLowerCase().includes(query) || 
        u.correo.toLowerCase().includes(query)
      );
    }

    return result;
  });

  ngOnInit() {
    this.cargarSolicitudes();
    this.cargarUsuarios();
  }

  cambiarSeccion(seccion: 'SOLICITUDES' | 'USUARIOS') {
    this.seccionActual.set(seccion);
    this.busqueda.set(''); // Limpiar búsqueda al cambiar
  }

  cargarUsuarios() {
    this.loadingUsuarios.set(true);
    this.error.set(null);
    
    this.adminService.getUsuarios().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.loadingUsuarios.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('Error al cargar usuarios. Inténtalo de nuevo más tarde.');
        this.loadingUsuarios.set(false);
      }
    });
  }

  cargarSolicitudes(pagina: number = 0) {
    this.loadingSolicitudes.set(true);
    this.error.set(null);
    this.paginaSolicitudes.set(pagina);

    this.adminService.getPendingTrainers(pagina, this.pageSize).subscribe({
      next: (res) => {
        this.solicitudes.set(res.content);
        this.totalPaginasSolicitudes.set(res.totalPages);
        this.loadingSolicitudes.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('Error al cargar las solicitudes. Inténtalo de nuevo más tarde.');
        this.loadingSolicitudes.set(false);
      }
    });
  }

  seleccionarSolicitud(entrenador: EntrenadorPendienteDTO) {
    this.entrenadorSeleccionado.set(entrenador);
  }

  seleccionarUsuario(usuario: UsuarioDTO) {
    this.usuarioSeleccionado.set(usuario);
  }

  // Abre el modal de confirmación antes de aprobar
  pedirConfirmacionAprobacion() {
    if (!this.entrenadorSeleccionado()) return;
    this.mostrarModalConfirmacion.set(true);
  }

  // Llamado cuando el usuario confirma en el modal
  confirmarAprobacion() {
    const e = this.entrenadorSeleccionado();
    if (!e) return;

    this.mostrarModalConfirmacion.set(false);
    this.loadingSolicitudes.set(true);
    this.adminService.processReview(e.id, true).subscribe({
      next: () => {
        this.nombreAprobado.set(e.nombre);
        this.solicitudes.update(list => list.filter(item => item.id !== e.id));
        this.entrenadorSeleccionado.set(null);
        this.loadingSolicitudes.set(false);
        this.mostrarModalExito.set(true);
      },
      error: (err) => {
        console.error(err);
        this.error.set('No se pudo aprobar al entrenador.');
        this.loadingSolicitudes.set(false);
      }
    });
  }

  // Mantener por compatibilidad (usado en mock)
  aprobar() {
    this.pedirConfirmacionAprobacion();
  }

  // --- ELIMINACIÓN DE USUARIOS ---

  pedirConfirmacionEliminacion(usuario: UsuarioDTO) {
    this.usuarioSeleccionado.set(usuario);
    this.mostrarModalEliminacion.set(true);
  }

  confirmarEliminacion() {
    const u = this.usuarioSeleccionado();
    if (!u) return;

    this.loadingUsuarios.set(true);
    this.adminService.eliminarUsuario(u.id).subscribe({
      next: () => {
        this.usuarios.update(list => list.filter(item => item.id !== u.id));
        this.usuarioSeleccionado.set(null);
        this.mostrarModalEliminacion.set(false);
        this.loadingUsuarios.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('No se pudo eliminar el usuario.');
        this.loadingUsuarios.set(false);
      }
    });
  }

  abrirModalRechazo() {
    this.motivoRechazo.set('');
    this.mostrarModalRechazo.set(true);
  }

  confirmarRechazo() {
    const e = this.entrenadorSeleccionado();
    const motivo = this.motivoRechazo().trim();
    if (!e || !motivo) return;

    this.loadingSolicitudes.set(true);
    this.adminService.processReview(e.id, false, motivo).subscribe({
      next: () => {
        this.solicitudes.update(list => list.filter(item => item.id !== e.id));
        this.entrenadorSeleccionado.set(null);
        this.mostrarModalRechazo.set(false);
        this.motivoRechazo.set('');
        this.loadingSolicitudes.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('No se pudo rechazar la solicitud.');
        this.loadingSolicitudes.set(false);
      }
    });
  }

  iniciales(nombre: string): string {
    return nombre
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  // ── Paginación ──────────────────────────────────────────────────────────

  irPaginaAnterior() {
    const pagina = this.paginaSolicitudes();
    if (pagina > 0) {
      this.cargarSolicitudes(pagina - 1);
    }
  }

  irPaginaSiguiente() {
    const pagina = this.paginaSolicitudes();
    if (pagina < this.totalPaginasSolicitudes() - 1) {
      this.cargarSolicitudes(pagina + 1);
    }
  }

  irPagina(pagina: number) {
    if (pagina >= 0 && pagina < this.totalPaginasSolicitudes()) {
      this.cargarSolicitudes(pagina);
    }
  }

  cerrarSesion() {
    this.auth.logout();
  }
}
