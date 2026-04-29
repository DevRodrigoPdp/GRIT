import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface CheckInPeso {
  id:             string;
  pesoKg:         number;
  fecha:          string;
  solicitadoPor:  'ENTRENADOR' | 'NUTRICIONISTA';
}

interface ApiResponse<T> { ok: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class SeguimientoService {
  private http = inject(HttpClient);

  private readonly API = '/api/v1/entrenador';

  getHistorialPesos(atletaId: string): Observable<CheckInPeso[]> {
    return this.http
      .get<ApiResponse<CheckInPeso[]>>(`${this.API}/atletas/${atletaId}/peso/historial`, { withCredentials: true })
      .pipe(map(r => r.data || []));
  }

  tieneCheckInPendiente(atletaId: string): Observable<boolean> {
    return this.http
      .get<ApiResponse<{ pendiente: boolean }>>(`${this.API}/atletas/${atletaId}/peso/pendiente`, { withCredentials: true })
      .pipe(map(r => r.data.pendiente));
  }

  solicitarCheckIn(atletaId: string, _solicitadoPor: 'ENTRENADOR' | 'NUTRICIONISTA'): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.API}/atletas/${atletaId}/peso/solicitar`, {}, { withCredentials: true })
      .pipe(map(() => undefined));
  }
}
