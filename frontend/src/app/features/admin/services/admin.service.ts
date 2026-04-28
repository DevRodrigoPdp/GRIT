import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface DocumentoDTO {
  id: string;
  nombre_archivo: string;
  url_firmada: string;
  uploaded_at: string;
}

export interface EntrenadorPendienteDTO {
  id: string;
  nombre: string;
  correo: string;
  titulacionEntrenamiento: string | null;
  titulacionNutricion: string | null;
  codigoProfesional: string;
  createdAt: string;
  documentos: DocumentoDTO[];
}

export interface UsuarioDTO {
  id: string;
  uuid?: string;
  nombre: string;
  correo: string;
  rol: 'ATLETA' | 'ENTRENADOR';
  estado: string;
  fechaRegistro: string;
  detallesAtleta?: {
    servicio: string;
    entrenadorAsignado?: string;
  };
  detallesEntrenador?: {
    numAtletas: number;
    titulacion: string;
  };
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface DashboardStats {
  mensaje: string;
  usuario: number;
  suscripciones_premium: number;
  estado_servidor: string;
}

export interface AdminResponse<T> {
  ok?: boolean;
  data?: T;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/v1/admin';

  /**
   * Obtiene la lista de todos los usuarios registrados (Atletas y Entrenadores).
   */
  getUsuarios(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(
      `${this.API}/usuarios`,
      { withCredentials: true }
    ).pipe(
      map(usuarios => usuarios.map(u => this.normalizarUsuario(u)))
    );
  }

  /**
   * Obtiene un usuario por ID.
   */
  getUsuario(id: string): Observable<UsuarioDTO> {
    return this.http.get<UsuarioDTO>(
      `${this.API}/usuarios/${id}`,
      { withCredentials: true }
    ).pipe(
      map(u => this.normalizarUsuario(u))
    );
  }

  /**
   * Normaliza el usuario asegurando que siempre tiene un id válido
   */
  private normalizarUsuario(usuario: any): UsuarioDTO {
    return {
      ...usuario,
      id: usuario.id || usuario.uuid || usuario.userId || ''
    };
  }

  /**
   * Actualiza los datos de un usuario.
   */
  actualizarUsuario(id: string, data: Partial<UsuarioDTO>): Observable<UsuarioDTO> {
    return this.http.put<UsuarioDTO>(
      `${this.API}/usuarios/${id}`,
      data,
      { withCredentials: true }
    );
  }

  /**
   * Elimina permanentemente un usuario.
   */
  eliminarUsuario(id: string): Observable<void> {
    return this.http.delete<void>(
      `${this.API}/usuarios/${id}`,
      { withCredentials: true }
    );
  }

  /**
   * Obtiene la lista de entrenadores con documentación pendiente (con paginación).
   */
  getPendingTrainers(page: number = 0, size: number = 10): Observable<PageResponse<EntrenadorPendienteDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<EntrenadorPendienteDTO>>(
      `${this.API}/entrenadores/pendientes`,
      { params, withCredentials: true }
    );
  }

  /**
   * Procesa la revisión de un entrenador (aprueba o rechaza).
   * @param id ID del entrenador
   * @param aprobado true para aprobar, false para rechazar
   * @param motivo Motivo del rechazo (obligatorio si aprobado es false)
   */
  processReview(id: string, aprobado: boolean, motivo?: string): Observable<void> {
    const params = new HttpParams()
      .set('aprobado', aprobado.toString())
      .set('motivo', motivo || '');
    
    return this.http.post<void>(
      `${this.API}/entrenadores/${id}/revision`,
      {},
      { params, withCredentials: true }
    );
  }

  /**
   * Obtiene estadísticas del dashboard de administración.
   */
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(
      `${this.API}/dashboard`,
      { withCredentials: true }
    );
  }
}
