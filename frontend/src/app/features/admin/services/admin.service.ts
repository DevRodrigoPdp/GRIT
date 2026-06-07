import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface DocumentoDTO {
  id: string;
  nombreArchivo: string;
  urlFirmada: string;
  uploadedAt: string;
  status: string;
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
  nombre: string;
  correo: string;
  rol: 'ATLETA' | 'ENTRENADOR' | 'ADMIN';
  estado: string;
  fechaRegistro: string;
}

export interface PageResponse<T> {
  content: T[];
  page: {
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
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
  private readonly API = '/api/v1/admin';

  buscarUsuarios(
    q: string = '',
    page: number = 0,
    size: number = 10,
  ): Observable<{ usuarios: UsuarioDTO[]; totalPages: number }> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (q.trim()) params = params.set('search', q.trim());

    return this.http
      .get<any>(`${this.API}/usuarios/search`, { params, withCredentials: true })
      .pipe(
        map((r) => {
          const lista: any[] = Array.isArray(r) ? r : (r?.content ?? r?.data ?? []);
          return {
            usuarios: lista.map((u: any) => ({
              id: u.id,
              nombre: u.nombre,
              correo: u.email,
              rol: u.rol,
              estado: u.estado,
              fechaRegistro: u.registro ?? u.fechaRegistro ?? u.createdAt ?? null,
            })),
            totalPages: r?.page?.totalPages ?? r?.totalPages ?? 1,
          };
        }),
        catchError(() => of({ usuarios: [], totalPages: 0 })),
      );
  }

  /**
   * Actualiza los datos de un usuario.
   */
  bloquearUsuario(id: string, data: Partial<UsuarioDTO>): Observable<UsuarioDTO> {
    return this.http
      .patch<UsuarioDTO>(`${this.API}/usuarios/${id}`, data, { withCredentials: true })
      .pipe(
        map((u: any) => ({
          id: u.id,
          nombre: u.nombre,
          correo: u.email,
          rol: u.rol,
          estado: u.estado,
          fechaRegistro: u.registro ?? u.fechaRegistro ?? u.createdAt ?? null,
        })),
      );
  }

  /**
   * Elimina permanentemente un usuario.
   */
  eliminarUsuario(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API}/usuarios/${id}`, { withCredentials: true });
  }

  /**
   * Busca entrenadores pendientes por nombre o email.
   */
  buscarEntrenadores(
    q: string = '',
    page: number = 0,
    size: number = 10,
  ): Observable<PageResponse<EntrenadorPendienteDTO>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (q.trim()) params = params.set('q', q.trim());

    return this.http.get<PageResponse<EntrenadorPendienteDTO>>(
      `${this.API}/entrenadores/pendientes/search`,
      { params, withCredentials: true },
    );
  }

  /**
   * Obtiene la lista de entrenadores con documentación pendiente (con paginación).
   */
  getPendingTrainers(
    page: number = 0,
    size: number = 10,
  ): Observable<PageResponse<EntrenadorPendienteDTO>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    return this.http.get<PageResponse<EntrenadorPendienteDTO>>(
      `${this.API}/entrenadores/pendientes`,
      { params, withCredentials: true },
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
      { params, withCredentials: true },
    );
  }

  /**
   * Obtiene estadísticas del dashboard de administración.
   */
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.API}/dashboard`, { withCredentials: true });
  }
}
