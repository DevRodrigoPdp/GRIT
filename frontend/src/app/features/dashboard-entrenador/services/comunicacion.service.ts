import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export type CategoriaHilo = 'tecnica' | 'duda' | 'apunte';

export interface Adjunto {
  id: string;
  url: string;
  tipo: 'imagen' | 'video';
  nombre: string;
}

export interface MensajeHilo {
  id: string;
  texto: string;
  de: 'entrenador' | 'atleta';
  fecha: Date;
  adjuntos?: Adjunto[];
}

export interface HiloResumenDTO {
  id: string;
  titulo: string;
  categoria: string; // TECNICA, DUDA, etc.
  contexto: string;
  creadoPor: string;
  creadoEn: string;
  totalMensajes: number;
  ultimoTexto: string;
  ultimoEnvio: string;
  ultimoEnviadoPor: string;
  leido: boolean;
}

export interface Hilo {
  id: string;
  titulo: string;
  categoria: CategoriaHilo;
  de: 'entrenador' | 'atleta';
  fechaAbierto: Date;
  mensajes: MensajeHilo[];
  leido: boolean;
}


@Injectable({
  providedIn: 'root'
})
export class ComunicacionService {
  private http = inject(HttpClient);
  private readonly API = '/api/v1/comunicacion';

  /**
   * Obtiene la lista de hilos filtrada por atleta y contexto (ENTRENAMIENTO/NUTRICION)
   */
  getHilos(atletaId: string, contexto: string): Observable<Hilo[]> {
    const params = new HttpParams()
      .set('contexto', contexto);

    return this.http.get<{ ok: boolean; data: any[] }>(`${this.API}/entrenador/atleta/${atletaId}/hilos`, { params }).pipe(
      map(res => res.data.map(h => this.mapToHilo(h)))
    );
  }

  /**
   * Obtiene un hilo específico con todos sus mensajes
   */
  getHiloDetalle(hiloId: string): Observable<Hilo> {
    return this.http.get<{ ok: boolean; data: any }>(`${this.API}/hilos/${hiloId}`).pipe(
      map(res => this.mapToHilo(res.data))
    );
  }

  /**
   * Crea un nuevo hilo enviando datos y archivos mediante FormData
   */
  crearHilo(atletaId: string, titulo: string, categoria: string, contexto: string, texto: string, archivos: File[]): Observable<any> {
    const fd = new FormData();
    fd.append('atletaId', atletaId);
    fd.append('titulo', titulo);
    fd.append('categoria', categoria.toUpperCase());
    fd.append('contexto', contexto);
    fd.append('texto', texto);

    archivos.forEach(file => fd.append('archivos', file));

    return this.http.post(`${this.API}/hilos`, fd);
  }

  /**
   * Envía una respuesta a un hilo existente
   */
  enviarRespuesta(hiloId: string, texto: string, archivos: File[]): Observable<any> {
    const fd = new FormData();
    if (texto) fd.append('texto', texto);
    archivos.forEach(file => fd.append('archivos', file));

    return this.http.post(`${this.API}/hilos/${hiloId}/mensajes`, fd);
  }

  /**
   * Mapeo de los datos del Backend (Java/Spring) al modelo del Frontend (Angular)
   */
  private mapToHilo(h: any): Hilo {
    return {
      id: h.id,
      titulo: h.titulo,
      categoria: h.categoria.toLowerCase() as CategoriaHilo,
      de: h.creadoPor === 'ENTRENADOR' ? 'entrenador' : 'atleta',
      fechaAbierto: new Date(h.fechaAbierto),
      leido: h.leidoPorMi,
      mensajes: (h.mensajes || []).map((m: any) => ({
        id: m.id,
        texto: m.texto || '',
        de: m.de === 'ENTRENADOR' ? 'entrenador' : 'atleta',
        fecha: new Date(m.fecha),
        adjuntos: m.adjuntos?.map((a: any) => ({
          id: a.id,
          url: a.url,
          tipo: a.tipo.toLowerCase() as 'imagen' | 'video',
          nombre: a.nombre
        }))
      }))
    };
  }
}