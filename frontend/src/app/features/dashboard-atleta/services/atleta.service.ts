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

export interface PerfilAtleta {
  id: string;
  nombre: string;
  email: string;
  fechaNac: string;
  genero: 'HOMBRE' | 'MUJER' | 'OTRO';
  peso: number;
  altura: number;
  deporte: string;
  nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | 'ELITE';
  servicio: 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';
  objetivo: 'RENDIMIENTO' | 'MASA_MUSCULAR' | 'PERDER_PESO' | 'SALUD' | 'RESISTENCIA' | null;
  alergias: string[];
  lesiones: string[];
  fotoUrl: string | null;
}


export interface Ejercicio {
  nombre: string;
  series: number;
  reps: string;
  descanso?: string;
  notas?: string;
}

export interface SesionEntrenamiento {
  nombre: string;
  ejercicios: Ejercicio[];
}

export interface PlanEntrenamiento {
  id: string;
  nombre: string;
  descripcion: string;
  semanas?: number;
  semanaActual?: number;
  sesiones: SesionEntrenamiento[];
  creadoEn?: string;
  atletaId?: string;
}

export interface SolicitudCheckIn {
  id: string;
  fecha: string;
  solicitadoPor: string;
}

export interface CheckInPeso {
  id: string;
  fecha: string;
  pesoKg: number;
}

export interface Alimento {
  id?: string;
  codigo?: string;
  nombre: string;
  marca?: string;
  cantidadG: number;
  kcal?: number;
  proteinas?: number;
  carbos?: number;
  grasas?: number;
}

export interface Comida {
  nombre: string;
  alimentos: Alimento[];
  notas?: string;
}

export interface NotaNutricionista {
  id: string;
  texto: string;
  fecha: string;
}

export interface MediaAdjunto {
  id: string;
  tipo: 'foto' | 'video';
  url: string;
  fecha: string;
}

export interface MensajeHilo {
  id: string;
  texto: string;
  fecha: string;
  esAtleta: boolean;
  autor: string;
}

export interface HiloEjercicio {
  sesionDia: string;
  ejercicioNombre: string;
  notaEntrenador?: string;
  media: MediaAdjunto[];
  mensajes: MensajeHilo[];
}

export interface AdjuntoChat {
  url: string;
  tipo: 'foto' | 'video';
  nombre: string;
}

export interface MensajeChat {
  id: string;
  texto: string;
  fecha: string;
  esAtleta: boolean;
  autor: string;
  adjunto?: AdjuntoChat;
}

export interface Chat {
  tipo: 'entrenador' | 'nutricionista';
  interlocutor: string;
  mensajes: MensajeChat[];
}

export interface MensajeHiloComida {
  id: string;
  texto: string;
  fecha: string;
  esAtleta: boolean;
  autor: string;
}

export interface HiloComida {
  comidaNombre: string;
  mensajes: MensajeHiloComida[];
}

export interface PlanNutricion {
  id: string;
  nombre: string;
  descripcion: string;
  kcalDiarias: number;
  proteinas?: number;
  carbos?: number;
  grasas?: number;
  comidas: Comida[];
}

export interface ProfesionalAsignado {
  id: string;
  nombre: string;
  titulacion: string;
  rol: 'ENTRENADOR' | 'NUTRICIONISTA';
  descripcion?: string;
  anosExperiencia?: number | null;
  sobreMi?: string | null;
  masters?: string[];
}


@Injectable({ providedIn: 'root' })
export class AtletaService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private readonly API = '/api/v1/atleta';

  getPerfil(): Observable<PerfilAtleta> {
    return this.http.get<ApiResponseDTO<PerfilAtleta>>(`${this.API}/perfil`, { withCredentials: true })
      .pipe(map(r => ({
        ...r.data,
        alergias: r.data.alergias ?? [],
        lesiones: r.data.lesiones ?? [],
      })));
  }

  getProfesionalesAsignados(): Observable<ProfesionalAsignado[]> {
    return this.http.get<ApiResponseDTO<ProfesionalAsignado[]>>(`${this.API}/profesionales`, { withCredentials: true })
      .pipe(map(response => response.data || []));
  }

  getPlanEntrenamiento(): Observable<PlanEntrenamiento | null> {
    return this.http.get<ApiResponseDTO<any>>(`${this.API}/entrenamiento/plan-activo`, { withCredentials: true })
      .pipe(map(response => {
        const d = response.data;
        if (!d) return null;
        return {
          ...d,
          sesiones: (d.sesiones ?? []).map((s: any) => ({
            ...s,
            ejercicios: (s.ejercicios ?? []).map((e: any) => ({
              nombre:   e.ejercicioNombre ?? e.nombre ?? '',
              series:   e.series  ?? 0,
              reps:     String(e.reps ?? ''),
              notas:    e.notas   ?? '',
              descanso: e.descanso,
            }))
          }))
        } as PlanEntrenamiento;
      }));
  }

  getPlanNutricion(): Observable<PlanNutricion | null> {
    return this.http.get<ApiResponseDTO<PlanNutricion>>(`${this.API}/nutricion/plan-activo`, { withCredentials: true })
      .pipe(map(response => response.data || null));
  }

  getSolicitudCheckIn(): Observable<SolicitudCheckIn | null> {
    return this.http.get<ApiResponseDTO<SolicitudCheckIn>>(`${this.API}/peso/solicitud-pendiente`, { withCredentials: true })
      .pipe(
        map(response => response.data || null),
        catchError(() => of(null)),
      );
  }

  getHistorialPesos(): Observable<CheckInPeso[]> {
    return this.http.get<ApiResponseDTO<CheckInPeso[]>>(`${this.API}/peso/historial`, { withCredentials: true })
      .pipe(map(response => response.data || []));
  }

  registrarPeso(solicitudId: string, pesoKg: number): Observable<CheckInPeso> {
    return this.http.post<ApiResponseDTO<CheckInPeso>>(`${this.API}/peso`, { solicitudId, pesoKg }, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  getNotasNutricionista(): Observable<NotaNutricionista[]> {
    return this.http.get<ApiResponseDTO<NotaNutricionista[]>>(`${this.API}/nutricion/notas`, { withCredentials: true })
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

  getHiloEjercicio(sesionDia: string, ejercicioNombre: string): Observable<HiloEjercicio> {
    return this.http.get<ApiResponseDTO<HiloEjercicio>>(
      `${this.API}/entrenamiento/hilo?dia=${sesionDia}&ejercicio=${encodeURIComponent(ejercicioNombre)}`,
      { withCredentials: true }
    ).pipe(map(response => response.data));
  }

  enviarMensajeHilo(sesionDia: string, ejercicioNombre: string, texto: string): Observable<MensajeHilo> {
    return this.http.post<ApiResponseDTO<MensajeHilo>>(`${this.API}/entrenamiento/hilo/mensaje`,
      { sesionDia, ejercicioNombre, texto }, { withCredentials: true }
    ).pipe(map(response => response.data));
  }

  getChat(tipo: 'entrenador' | 'nutricionista'): Observable<Chat> {
    return this.http.get<ApiResponseDTO<Chat>>(`${this.API}/chat/${tipo}`, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  enviarMensajeChat(tipo: 'entrenador' | 'nutricionista', texto: string, adjunto?: AdjuntoChat): Observable<MensajeChat> {
    const form = new FormData();
    if (texto) form.append('texto', texto);
    if (adjunto) {
      form.append('adjuntoUrl', adjunto.url);
      form.append('adjuntoTipo', adjunto.tipo);
      form.append('adjuntoNombre', adjunto.nombre);
    }
    return this.http.post<ApiResponseDTO<MensajeChat>>(`${this.API}/chat/${tipo}/mensaje`, form, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  subirArchivoChat(tipo: 'entrenador' | 'nutricionista', archivo: File): Observable<AdjuntoChat> {
    const form = new FormData();
    form.append('archivo', archivo);
    return this.http.post<ApiResponseDTO<AdjuntoChat>>(`${this.API}/chat/${tipo}/archivo`, form, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  getHiloComida(comidaNombre: string): Observable<HiloComida> {
    return this.http.get<ApiResponseDTO<HiloComida>>(
      `${this.API}/nutricion/hilo?comida=${encodeURIComponent(comidaNombre)}`,
      { withCredentials: true }
    ).pipe(map(response => response.data));
  }

  enviarMensajeHiloComida(comidaNombre: string, texto: string): Observable<MensajeHiloComida> {
    return this.http.post<ApiResponseDTO<MensajeHiloComida>>(`${this.API}/nutricion/hilo/mensaje`,
      { comidaNombre, texto }, { withCredentials: true }
    ).pipe(map(response => response.data));
  }

  subirMediaHilo(sesionDia: string, ejercicioNombre: string, archivo: File): Observable<MediaAdjunto> {
    const form = new FormData();
    form.append('archivo', archivo);
    form.append('sesionDia', sesionDia);
    form.append('ejercicioNombre', ejercicioNombre);
    return this.http.post<ApiResponseDTO<MediaAdjunto>>(`${this.API}/entrenamiento/hilo/media`, form, { withCredentials: true })
      .pipe(map(response => response.data));
  }

  conectarConEntrenador(codigo: string, rolSolicitado: 'ENTRENAMIENTO' | 'NUTRICION'): Observable<void> {
    return this.http.post<ApiResponseDTO<void>>(`${this.API}/conectar`, { codigo, rolSolicitado }, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  desconectarProfesional(profesionalId: string): Observable<void> {
    return this.http.delete<ApiResponseDTO<void>>(`${this.API}/profesionales/${profesionalId}`, { withCredentials: true })
      .pipe(map(() => undefined));
  }

  actualizarPerfil(datos: { deporte?: string; nivel?: string; objetivo?: string | null }): Observable<PerfilAtleta> {
    return this.http.patch<ApiResponseDTO<PerfilAtleta>>(`${this.API}/perfil`, datos, { withCredentials: true })
      .pipe(map(r => r.data));
  }
}
