import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

// ── Tipos ─────────────────────────────────────────────────────────────────────

export interface CheckInPeso {
  id:             string;
  pesoKg:         number;
  fecha:          string;         // ISO date string
  solicitadoPor:  'ENTRENADOR' | 'NUTRICIONISTA';
}

export interface SolicitudPeso {
  atletaId:       string;
  solicitadoPor:  'ENTRENADOR' | 'NUTRICIONISTA';
  fecha:          string;
}

export interface MediaAdjunto {
  id:    string;
  tipo:  'foto' | 'video';
  url:   string;
  fecha: string;
}

// ── Mocks ─────────────────────────────────────────────────────────────────────

const MOCK_HISTORIAL: Record<string, CheckInPeso[]> = {
  'atleta-1': [
    { id: '1', pesoKg: 81.2, fecha: '2026-03-01', solicitadoPor: 'ENTRENADOR'     },
    { id: '2', pesoKg: 80.5, fecha: '2026-03-08', solicitadoPor: 'NUTRICIONISTA'  },
    { id: '3', pesoKg: 80.1, fecha: '2026-03-15', solicitadoPor: 'ENTRENADOR'     },
    { id: '4', pesoKg: 79.8, fecha: '2026-03-22', solicitadoPor: 'NUTRICIONISTA'  },
    { id: '5', pesoKg: 79.3, fecha: '2026-03-29', solicitadoPor: 'ENTRENADOR'     },
    { id: '6', pesoKg: 78.9, fecha: '2026-04-05', solicitadoPor: 'NUTRICIONISTA'  },
  ],
  'atleta-2': [
    { id: '1', pesoKg: 65.0, fecha: '2026-03-10', solicitadoPor: 'ENTRENADOR' },
    { id: '2', pesoKg: 64.5, fecha: '2026-03-24', solicitadoPor: 'ENTRENADOR' },
    { id: '3', pesoKg: 64.2, fecha: '2026-04-07', solicitadoPor: 'ENTRENADOR' },
  ],
  'atleta-3': [],
  'atleta-4': [],
};

/** Si hay solicitud pendiente sin respuesta por atleta */
const MOCK_PENDIENTE: Record<string, boolean> = {
  'atleta-1': false,
  'atleta-2': true,
  'atleta-3': false,
  'atleta-4': false,
};

// ── Servicio ──────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class SeguimientoService {

  private historial = new Map<string, CheckInPeso[]>(
    Object.entries(MOCK_HISTORIAL).map(([k, v]) => [k, [...v]])
  );

  private pendiente = new Map<string, boolean>(
    Object.entries(MOCK_PENDIENTE)
  );

  /** Devuelve el historial de pesos de un atleta */
  getHistorialPesos(atletaId: string): Observable<CheckInPeso[]> {
    return of(this.historial.get(atletaId) ?? []);
  }

  /** Indica si hay una solicitud de peso pendiente de respuesta */
  tieneCheckInPendiente(atletaId: string): Observable<boolean> {
    return of(this.pendiente.get(atletaId) ?? false);
  }

  /**
   * El entrenador o nutricionista solicita un check-in de peso al atleta.
   * En mock simplemente marca la solicitud como pendiente.
   */
  solicitarCheckIn(atletaId: string, solicitadoPor: 'ENTRENADOR' | 'NUTRICIONISTA'): Observable<void> {
    this.pendiente.set(atletaId, true);
    return of(undefined);
  }

  /**
   * Simula la respuesta del atleta con su peso.
   * (Útil para testing manual en el mock)
   */
  responderCheckIn(atletaId: string, pesoKg: number, solicitadoPor: 'ENTRENADOR' | 'NUTRICIONISTA'): Observable<CheckInPeso> {
    const entry: CheckInPeso = {
      id:            crypto.randomUUID(),
      pesoKg,
      fecha:         new Date().toISOString().slice(0, 10),
      solicitadoPor,
    };
    const existentes = this.historial.get(atletaId) ?? [];
    this.historial.set(atletaId, [...existentes, entry]);
    this.pendiente.set(atletaId, false);
    return of(entry);
  }

  /** Simula la subida de un archivo y devuelve los datos del adjunto */
  subirMedia(archivo: File): Observable<MediaAdjunto> {
    const hoy = new Date().toISOString().split('T')[0];
    const tipo: 'foto' | 'video' = archivo.type.startsWith('video') ? 'video' : 'foto';
    // En mock usamos URL.createObjectURL para previsualizar el archivo local
    return of({
      id: `media-${Date.now()}`,
      tipo,
      url: URL.createObjectURL(archivo),
      fecha: hoy
    });
  }
}
