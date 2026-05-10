import { Component, inject, signal, computed, OnInit, OnDestroy } from '@angular/core';
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
export class AdminPage implements OnInit, OnDestroy {
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
  readonly paginaUsuarios = signal(0);
  readonly totalPaginasUsuarios = signal(0);
  private _searchTimer: any;
  private _searchSolicitudesTimer: any;

  // Comunes
  readonly busqueda = signal('');
  readonly busquedaSolicitudes = signal('');
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

  // Edición de usuario
  readonly mostrarModalEdicion = signal(false);
  readonly usuarioEnEdicion = signal<Partial<UsuarioDTO> | null>(null);

  // Usuarios filtrados por rol (client-side sobre los resultados del search)
  readonly usuariosFiltrados = computed(() => {
    const rol = this.filtroRol();
    if (rol === 'TODOS') return this.usuarios();
    return this.usuarios().filter(u => u.rol === rol);
  });

  // Solicitudes filtradas
  readonly solicitudesFiltradas = computed(() => {
    const query = this.busqueda().toLowerCase().trim();
    if (!query) return this.solicitudes();
    return this.solicitudes().filter(s =>
      s.nombre.toLowerCase().includes(query) ||
      s.correo.toLowerCase().includes(query)
    );
  });


  ngOnInit() {
    // Restaurar sesión si fue necesario (respaldo si guard no lo hizo)
    if (!this.auth.rol()) {
      this.auth.me().subscribe();
    }
    this.cargarSolicitudes();
    this.cargarUsuarios();
  }

  ngOnDestroy() {
    clearTimeout(this._searchTimer);
    clearTimeout(this._searchSolicitudesTimer);
  }

  cambiarSeccion(seccion: 'SOLICITUDES' | 'USUARIOS') {
    this.seccionActual.set(seccion);
    this.busqueda.set('');
    this.busquedaSolicitudes.set('');
    if (seccion === 'USUARIOS') this.cargarUsuarios();
  }

  cargarUsuarios(pagina: number = 0) {
    this.loadingUsuarios.set(true);
    this.paginaUsuarios.set(pagina);
    this.adminService.buscarUsuarios(this.busqueda(), pagina).subscribe(res => {
      this.usuarios.set(res.usuarios);
      this.totalPaginasUsuarios.set(res.totalPages);
      this.loadingUsuarios.set(false);
    });
  }

  onBusquedaUsuariosChange(q: string) {
    this.busqueda.set(q);
    clearTimeout(this._searchTimer);
    this._searchTimer = setTimeout(() => this.cargarUsuarios(), 350);
  }

  buscarAhora() {
    clearTimeout(this._searchTimer);
    this.cargarUsuarios();
  }

  cambiarFiltroRol(rol: 'TODOS' | 'ATLETA' | 'ENTRENADOR') {
    this.filtroRol.set(rol);
  }

  cargarSolicitudes(pagina: number = 0) {
    this.loadingSolicitudes.set(true);
    this.error.set(null);
    this.paginaSolicitudes.set(pagina);

    const q = this.busquedaSolicitudes();
    const obs = q.trim()
      ? this.adminService.buscarEntrenadores(q, pagina, this.pageSize)
      : this.adminService.getPendingTrainers(pagina, this.pageSize);

    obs.subscribe({
      next: (res) => {
        this.solicitudes.set(res.content);
        this.totalPaginasSolicitudes.set(res.totalPages);
        this.loadingSolicitudes.set(false);
      },
      error: (err) => {

        this.error.set('Error al cargar las solicitudes. Inténtalo de nuevo más tarde.');
        this.loadingSolicitudes.set(false);
      }
    });
  }

  onBusquedaSolicitudesChange(q: string) {
    this.busquedaSolicitudes.set(q);
    clearTimeout(this._searchSolicitudesTimer);
    this._searchSolicitudesTimer = setTimeout(() => this.cargarSolicitudes(), 350);
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
    if (!u || !u.id) return;

    this.loadingUsuarios.set(true);
    this.adminService.eliminarUsuario(u.id).subscribe({
      next: () => {
        this.usuarios.update(list => list.filter(item => item.id !== u.id));
        this.usuarioSeleccionado.set(null);
        this.mostrarModalEliminacion.set(false);
        this.loadingUsuarios.set(false);
      },
      error: (err) => {

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

    // Primero rechazar la solicitud, luego eliminar el usuario
    this.adminService.processReview(e.id, false, motivo).subscribe({
      next: () => {
        // Eliminar también el usuario definitivamente
        this.adminService.eliminarUsuario(e.id).subscribe({
          next: () => {
            this.solicitudes.update(list => list.filter(item => item.id !== e.id));
            this.entrenadorSeleccionado.set(null);
            this.mostrarModalRechazo.set(false);
            this.motivoRechazo.set('');
            this.loadingSolicitudes.set(false);
          },
          error: (err) => {
            this.error.set('La solicitud fue rechazada pero hubo un error al eliminar la cuenta.');
            this.loadingSolicitudes.set(false);
          }
        });
      },
      error: (err) => {

        this.error.set('No se pudo rechazar la solicitud.');
        this.loadingSolicitudes.set(false);
      }
    });
  }

  toggleBloqueo(usuario: UsuarioDTO) {
    const nuevoEstado = usuario.estado === 'BLOQUEADO' ? 'ACTIVO' : 'BLOQUEADO';
    this.adminService.actualizarUsuario(usuario.id, { estado: nuevoEstado }).subscribe({
      next: (actualizado) => {
        this.usuarios.update(list => list.map(u => u.id === actualizado.id ? actualizado : u));
      },
      error: (err) => {

        this.error.set('No se pudo cambiar el estado del usuario.');
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

  // --- EDICIÓN DE USUARIOS ---

  abrirModalEdicion(usuario: UsuarioDTO) {
    this.usuarioSeleccionado.set(usuario);
    this.usuarioEnEdicion.set({
      nombre: usuario.nombre,
      correo: usuario.correo,
      estado: usuario.estado
    });
    this.mostrarModalEdicion.set(true);
  }

  confirmarEdicion() {
    const u = this.usuarioSeleccionado();
    const edicion = this.usuarioEnEdicion();
    if (!u || !edicion) return;

    this.loadingUsuarios.set(true);
    this.adminService.actualizarUsuario(u.id, edicion).subscribe({
      next: (usuarioActualizado) => {
        // Actualizar la lista de usuarios
        this.usuarios.update(list =>
          list.map(item => item.id === u.id ? usuarioActualizado : item)
        );
        this.mostrarModalEdicion.set(false);
        this.usuarioSeleccionado.set(null);
        this.usuarioEnEdicion.set(null);
        this.loadingUsuarios.set(false);
      },
      error: (err) => {

        this.error.set('No se pudieron guardar los cambios.');
        this.loadingUsuarios.set(false);
      }
    });
  }

  cerrarSesion() {
    this.auth.logout();
  }
}
