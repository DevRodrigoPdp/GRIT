import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';

// ── Tipos de respuesta del backend ─────────────────────────────────────────

export interface ApiResponseDTO<T> {
  ok: boolean;
  message: string;
  data: T;
}

export interface PerfilEntrenador {
  id: string;
  nombre: string;
  email: string;
  titulacionEntrenamiento: 'GRADO_CAFYD' | 'TSAF_TSEAS' | 'CERT_AFDA0210' | null;
  titulacionNutricion: 'GRADO_NUTRICION_DIETETICA' | 'TSD' | null;
  experienciaAnos: number;
  descripcion: string;
  masters: string[];
  estado: 'ACTIVO' | 'PENDIENTE_REVISION' | 'RECHAZADO';
  solicitudAmpliacionPendiente?: 'ENTRENAMIENTO' | 'NUTRICION' | null;
  codigoInvitacion: string;
  fotoUrl: string | null;
}

export type TitulacionEntrenamiento = 'GRADO_CAFYD' | 'TSAF_TSEAS' | 'CERT_AFDA0210';
export type TitulacionNutricion     = 'GRADO_NUTRICION_DIETETICA' | 'TSD';

export interface AtletaAsignado {
  id: string;
  nombre: string;
  deporte: string;
  nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | 'ELITE';
  /** Servicios contratados por el atleta con este entrenador */
  servicio: 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';
  tienePlanActivo: boolean;
  alergias?: string[];
  intolerancias?: string[];
}

export interface CheckInPeso {
  id: string;
  fecha: string;
  pesoKg: number;
}

// ── Servicio ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class EntrenadorService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private readonly API = '/api/v1/entrenador';

  getPerfil(): Observable<PerfilEntrenador | null> {
    return this.http.get<ApiResponseDTO<PerfilEntrenador>>(`${this.API}/perfil`, { withCredentials: true })
      .pipe(map(response => response.data), catchError(() => of(null)));
  }

  getMisAtletas(): Observable<AtletaAsignado[]> {
    return this.http.get<ApiResponseDTO<AtletaAsignado[]>>(`${this.API}/atletas`, { withCredentials: true })
      .pipe(map(response => response.data || []));
  }

  solicitarCheckinPeso(atletaId: string): Observable<void> {
    return this.http.post<ApiResponseDTO<string>>(`${this.API}/atletas/${atletaId}/peso/solicitar`, {}, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  tieneSolicitudPesoPendiente(atletaId: string): Observable<boolean> {
    return this.http.get<ApiResponseDTO<{ pendiente: boolean }>>(`${this.API}/atletas/${atletaId}/peso/pendiente`, { withCredentials: true })
      .pipe(map(response => response.data.pendiente));
  }

  getHistorialPesosAtleta(atletaId: string): Observable<CheckInPeso[]> {
    return this.http.get<ApiResponseDTO<CheckInPeso[]>>(`${this.API}/atletas/${atletaId}/peso/historial`, { withCredentials: true })
      .pipe(map(response => response.data || []));
  }

  subirFotoPerfil(archivo: File): Observable<string> {
    const form = new FormData();
    form.append('foto', archivo);
    return this.http.post<ApiResponseDTO<{ url: string }>>(`${this.API}/foto`, form, { withCredentials: true })
      .pipe(map(r => r.data.url));
  }

  cambiarPassword(actual: string, nueva: string): Observable<void> {
    return this.http.put<ApiResponseDTO<void>>(`${this.API}/password`, { actual, nueva }, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  eliminarCuenta(): Observable<void> {
    return this.http.delete<ApiResponseDTO<void>>(`${this.API}/cuenta`, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  invitarAtleta(email: string): Observable<void> {
    return this.http.post<ApiResponseDTO<void>>(`${this.API}/invitar`, { email }, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  checkCodigoColegiadoExists(codigo: string): Observable<boolean> {
    return this.http.get<ApiResponseDTO<boolean>>(`${this.API}/check-codigo/${codigo}`, { withCredentials: true })
      .pipe(map(response => response.data ?? false));
  }

  solicitarAmpliacionFormacion(modulo: 'ENTRENAMIENTO' | 'NUTRICION', titulacion: string, documentos: File[]): Observable<void> {
    const fd = new FormData();
    fd.append('modulo', modulo);
    fd.append('titulacion', titulacion);
    documentos.forEach(f => fd.append('documentos', f));
    return this.http.post<ApiResponseDTO<void>>(`${this.API}/ampliar-formacion`, fd, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  desconectarAtleta(atletaId: string): Observable<void> {
    return this.http.delete<ApiResponseDTO<void>>(`${this.API}/atletas/${atletaId}/desconectar`, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  actualizarPerfil(data: { nombre: string; descripcion: string; experienciaAnos: number; masters: string[] }): Observable<void> {
    return this.http.put<ApiResponseDTO<void>>(`${this.API}/perfil`, data, { withCredentials: true })
      .pipe(map(() => undefined));
  }
}
