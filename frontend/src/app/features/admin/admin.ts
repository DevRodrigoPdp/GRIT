import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, SolicitudEntrenador, UsuarioAdmin } from './services/admin.service';
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

  // Estados Solicitudes
  readonly solicitudes = signal<SolicitudEntrenador[]>([]);
  readonly loadingSolicitudes = signal(false);
  
  // Estados Usuarios
  readonly usuarios = signal<UsuarioAdmin[]>([]);
  readonly loadingUsuarios = signal(false);
  readonly filtroRol = signal<'TODOS' | 'ATLETA' | 'ENTRENADOR'>('TODOS');

  // Comunes
  readonly busqueda = signal('');
  readonly error = signal<string | null>(null);

  // Selección y Modales
  readonly entrenadorSeleccionado = signal<SolicitudEntrenador | null>(null);
  readonly usuarioSeleccionado = signal<UsuarioAdmin | null>(null);
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
    
    // --- MOCK DATA PARA USUARIOS ---
    setTimeout(() => {
      const mockUsuarios: UsuarioAdmin[] = [
        {
          id: 'u1',
          nombre: 'RODRIGO PÉREZ',
          correo: 'rodrigo@test.com',
          rol: 'ATLETA',
          estado: 'ACTIVO',
          fechaRegistro: '2026-03-01T10:00:00Z',
          detallesAtleta: { servicio: 'AMBOS', entrenadorAsignado: 'Carlos Martínez' }
        },
        {
          id: 'u2',
          nombre: 'MARÍA GARCÍA',
          correo: 'maria@test.com',
          rol: 'ENTRENADOR',
          estado: 'ACTIVO',
          fechaRegistro: '2026-02-15T09:30:00Z',
          detallesEntrenador: { numAtletas: 12, titulacion: 'GRADO_CAFYD' }
        },
        {
          id: 'u3',
          nombre: 'JUAN LÓPEZ',
          correo: 'juan@test.com',
          rol: 'ATLETA',
          estado: 'INACTIVO',
          fechaRegistro: '2026-04-10T11:00:00Z',
          detallesAtleta: { servicio: 'ENTRENAMIENTO', entrenadorAsignado: 'Carlos Martínez' }
        },
        {
          id: 'u4',
          nombre: 'SARA MONTES',
          correo: 'sara@test.com',
          rol: 'ENTRENADOR',
          estado: 'ACTIVO',
          fechaRegistro: '2026-01-20T14:20:00Z',
          detallesEntrenador: { numAtletas: 8, titulacion: 'TSD' }
        }
      ];
      this.usuarios.set(mockUsuarios);
      this.loadingUsuarios.set(false);
    }, 1000);

    /*
    // COMENTADO: Llamada real
    this.adminService.getUsuarios().subscribe({
      next: (res) => {
        this.usuarios.set(res.data);
        this.loadingUsuarios.set(false);
      },
      error: () => this.loadingUsuarios.set(false)
    });
    */
  }

  cargarSolicitudes() {
    this.loadingSolicitudes.set(true);
    this.error.set(null);

    // --- MOCK DATA PARA SOLICITUDES ---
    setTimeout(() => {
      const mockSolicitudes: SolicitudEntrenador[] = [
        {
          id: '550e8400-e29b-41d4-a716-446655440000',
          nombre: 'CARLOS MARTÍNEZ RUIZ',
          correo: 'carlos.mtz@rendimiento.com',
          titulacionEntrenamiento: 'GRADO_CAFYD',
          titulacionNutricion: 'GRADO_NUTRICION_DIETETICA',
          codigoProfesional: 'COL. 45.231',
          uploaded_at: new Date().toISOString(),
          documentos: [
            { id: 'd1', nombre_archivo: 'TITULO_GRADO_CAFYD.pdf', url_firmada: '#', uploaded_at: '' },
            { id: 'd2', nombre_archivo: 'TITULO_NUTRICION.pdf', url_firmada: '#', uploaded_at: '' },
            { id: 'd3', nombre_archivo: 'DNI_FRONTAL.jpg', url_firmada: '#', uploaded_at: '' }
          ]
        },
        {
          id: '67c8dc00-f12b-42d4-b716-557766551111',
          nombre: 'ELENA GÓMEZ SILVA',
          correo: 'elena.gomez@grit-atletismo.es',
          titulacionEntrenamiento: 'TSAF_TSEAS',
          titulacionNutricion: null,
          codigoProfesional: 'REG. 12-B-99',
          uploaded_at: new Date(Date.now() - 86400000).toISOString(),
          documentos: [
            { id: 'd4', nombre_archivo: 'CERTIFICADO_TSAF.pdf', url_firmada: '#', uploaded_at: '' },
            { id: 'd5', nombre_archivo: 'VIDA_LABORAL.pdf', url_firmada: '#', uploaded_at: '' }
          ]
        },
        {
          id: '88d9ec11-a33c-55e5-c817-668877662222',
          nombre: 'MARIO CASAS PESCADO',
          correo: 'mario.nutritionist@fitness.io',
          titulacionEntrenamiento: null,
          titulacionNutricion: 'TSD',
          codigoProfesional: 'ASOC-77821',
          uploaded_at: new Date(Date.now() - 172800000).toISOString(),
          documentos: [
            { id: 'd6', nombre_archivo: 'TECNICO_SUPERIOR_DIETETICA.pdf', url_firmada: '#', uploaded_at: '' }
          ]
        }
      ];
      this.solicitudes.set(mockSolicitudes);
      this.loadingSolicitudes.set(false);
    }, 800);

    /* 
    // COMENTADO: Llamada real al backend
    this.adminService.getPendingTrainers().subscribe({
      next: (res) => {
        this.solicitudes.set(res.data);
        this.loadingSolicitudes.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('Error al cargar las solicitudes. Inténtalo de nuevo más tarde.');
        this.loadingSolicitudes.set(false);
      }
    });
    */
  }

  seleccionarSolicitud(entrenador: SolicitudEntrenador) {
    this.entrenadorSeleccionado.set(entrenador);
  }

  seleccionarUsuario(usuario: UsuarioAdmin) {
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
    this.adminService.approveTrainer(e.id).subscribe({
      next: () => {
        this.nombreAprobado.set(e.nombre);
        this.solicitudes.update(list => list.filter(item => item.id !== e.id));
        this.entrenadorSeleccionado.set(null);
        this.loadingSolicitudes.set(false);
        this.mostrarModalExito.set(true);
      },
      error: () => {
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

  pedirConfirmacionEliminacion(usuario: UsuarioAdmin) {
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
        // Podríamos mostrar un mensaje de éxito, pero el borrado es inmediato en la UI
      },
      error: () => {
        this.error.set('No se pudo eliminar el usuario.');
        this.loadingUsuarios.set(false);
      }
    });

    // Simulando éxito para el mock si falla el backend
    /*
    setTimeout(() => {
      this.usuarios.update(list => list.filter(item => item.id !== u.id));
      this.usuarioSeleccionado.set(null);
      this.mostrarModalEliminacion.set(false);
      this.loadingUsuarios.set(false);
    }, 500);
    */
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
    this.adminService.rejectTrainer(e.id, motivo).subscribe({
      next: () => {
        this.solicitudes.update(list => list.filter(item => item.id !== e.id));
        this.entrenadorSeleccionado.set(null);
        this.mostrarModalRechazo.set(false);
        this.loadingSolicitudes.set(false);
        alert('Solicitud rechazada correctamente.');
      },
      error: () => {
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

  cerrarSesion() {
    this.auth.logout();
  }
}
