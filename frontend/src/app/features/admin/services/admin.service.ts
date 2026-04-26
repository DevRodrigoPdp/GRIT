import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SolicitudEntrenador {
  id: string;
  nombre: string;
  correo: string;
  titulacionEntrenamiento: string | null;
  titulacionNutricion: string | null;
  codigoProfesional: string;
  uploaded_at: string;
  documentos: {
    id: string;
    nombre_archivo: string;
    url_firmada: string;
    uploaded_at: string;
  }[];
}

export interface UsuarioAdmin {
  id: string;
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

export interface AdminResponse<T> {
  ok: boolean;
  data: T;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/v1/admin';

  /**
   * Obtiene la lista de todos los usuarios registrados (Atletas y Entrenadores).
   */
  getUsuarios(): Observable<AdminResponse<UsuarioAdmin[]>> {
    return this.http.get<AdminResponse<UsuarioAdmin[]>>(
      `${this.API}/usuarios`,
      { withCredentials: true }
    );
  }

  /**
   * Elimina permanentemente un usuario.
   */
  eliminarUsuario(id: string): Observable<AdminResponse<void>> {
    return this.http.delete<AdminResponse<void>>(
      `${this.API}/usuarios/${id}`,
      { withCredentials: true }
    );
  }

  /**
   * Obtiene la lista de entrenadores con documentación pendiente.
   */
  getPendingTrainers(): Observable<AdminResponse<SolicitudEntrenador[]>> {
    return this.http.get<AdminResponse<SolicitudEntrenador[]>>(
      `${this.API}/entrenadores?status=pending`,
      { withCredentials: true }
    );
  }

  /**
   * Aprueba a un entrenador.
   */
  approveTrainer(id: string): Observable<AdminResponse<void>> {
    return this.http.post<AdminResponse<void>>(
      `${this.API}/entrenadores/${id}/aprobar`,
      {},
      { withCredentials: true }
    );
  }

  /**
   * Rechaza a un entrenador con un motivo.
   */
  rejectTrainer(id: string, motivo: string): Observable<AdminResponse<void>> {
    return this.http.post<AdminResponse<void>>(
      `${this.API}/entrenadores/${id}/rechazar`,
      { motivo },
      { withCredentials: true }
    );
  }
}
