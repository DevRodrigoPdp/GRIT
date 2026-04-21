import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

// ── Tipos de respuesta del backend ─────────────────────────────────────────

export interface PerfilAtleta {
  id: string;
  nombre: string;
  correo: string;
  fechaNac: string;
  genero: 'HOMBRE' | 'MUJER' | 'OTRO';
  peso: number;
  altura: number;
  deporte: string;
  nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | 'ELITE';
  servicio: 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS';
  objetivo: 'RENDIMIENTO' | 'MASA_MUSCULAR' | 'PERDER_PESO' | 'SALUD' | 'RESISTENCIA' | null;
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
  semanas: number;
  semanaActual: number;
  sesiones: SesionEntrenamiento[];
}

export interface Alimento {
  nombre: string;
  cantidad: string;
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

// ── Mocks ────────────────────────────────────────────────────────────────────

const MOCK_PROFESIONALES: ProfesionalAsignado[] = [
  {
    id: 'prof-1',
    nombre: 'Carlos López',
    titulacion: 'Grado en Ciencias de la Actividad Física y del Deporte',
    rol: 'ENTRENADOR',
    descripcion: 'Especialista en fuerza e hipertrofia. Más de 8 años trabajando con atletas de todos los niveles, desde principiantes hasta competidores de powerlifting.',
    anosExperiencia: 8,
    sobreMi: 'Entreno a deportistas que quieren mejorar su rendimiento de forma sostenible. Me especializo en el trabajo de fuerza con base científica, adaptando cada bloque a las necesidades reales del atleta.',
    masters: ['Máster en Alto Rendimiento Deportivo', 'Máster en Entrenamiento de Fuerza y Acondicionamiento Físico'],
  },
  {
    id: 'prof-2',
    nombre: 'María González',
    titulacion: 'Dietista-Nutricionista (Graduada en Nutrición Humana y Dietética)',
    rol: 'NUTRICIONISTA',
    descripcion: 'Especializada en nutrición deportiva y composición corporal. Trabaja con atletas de resistencia y fuerza para optimizar el rendimiento y la recuperación.',
    anosExperiencia: 6,
    sobreMi: 'Mi enfoque es la nutrición práctica: planes que se adaptan a tu vida, no al revés. Combino evidencia científica con un seguimiento cercano para que los cambios sean duraderos.',
    masters: ['Máster en Nutrición Deportiva'],
  },
];

const MOCK_PERFIL: PerfilAtleta = {
  id: 'mock-uuid-atleta',
  nombre: 'Atleta Demo',
  correo: 'atleta@demo.com',
  fechaNac: '1998-05-14',
  genero: 'HOMBRE',
  peso: 80,
  altura: 180,
  deporte: 'Ciclismo',
  nivel: 'AVANZADO',
  servicio: 'AMBOS',
  objetivo: 'RENDIMIENTO',
};

const MOCK_PLAN_ENTRENAMIENTO: PlanEntrenamiento = {
  id: 'plan-1',
  nombre: 'Fuerza — Mesociclo 2',
  descripcion: 'Bloque de hipertrofia con énfasis en tren superior. Progresión semanal en carga.',
  semanas: 8,
  semanaActual: 3,
  sesiones: [
    {
      nombre: 'Lunes',
      ejercicios: [
        { nombre: 'Press de banca', series: 4, reps: '8-10', descanso: '90s', notas: 'Codos a 45° del torso. Baja hasta que el pecho casi toque la barra y empuja de forma explosiva. Pies firmes en el suelo.' },
        { nombre: 'Remo en polea baja', series: 4, reps: '10-12', descanso: '60s', notas: 'Tira hacia el ombligo, no hacia el pecho. Mantén la espalda neutra y el pecho fuera.' },
        { nombre: 'Press militar', series: 3, reps: '10', descanso: '60s' },
        { nombre: 'Aperturas con mancuernas', series: 3, reps: '12-15', descanso: '45s', notas: 'Movimiento controlado en la bajada. Llega hasta paralelo, no bajes más para proteger el hombro.' },
      ],
    },
    {
      nombre: 'Miércoles',
      ejercicios: [
        { nombre: 'Sentadilla', series: 4, reps: '6-8', descanso: '120s', notas: 'Esta semana trabaja la profundidad sin añadir peso. Baja por debajo del paralelo, espalda neutra y rodillas alineadas con los pies.' },
        { nombre: 'Peso muerto rumano', series: 3, reps: '10', descanso: '90s', notas: 'Empuja las caderas hacia atrás, no dobles las rodillas en exceso. Siente el estiramiento en los isquiotibiales.' },
        { nombre: 'Hip thrust', series: 3, reps: '12', descanso: '60s' },
        { nombre: 'Prensa 45°', series: 3, reps: '12', descanso: '60s' },
      ],
    },
    {
      nombre: 'Viernes',
      ejercicios: [
        { nombre: 'Dominadas', series: 4, reps: 'Máx', descanso: '90s' },
        { nombre: 'Fondos en paralelas', series: 4, reps: 'Máx', descanso: '90s' },
        { nombre: 'Curl de bíceps', series: 3, reps: '12', descanso: '45s' },
        { nombre: 'Extensión de tríceps en polea', series: 3, reps: '12', descanso: '45s' },
      ],
    },
  ],
};

const MOCK_PLAN_NUTRICION: PlanNutricion = {
  id: 'nutri-1',
  nombre: 'Definición — 2.400 kcal',
  descripcion: 'Déficit moderado con alta proteína para preservar masa muscular durante la fase de definición.',
  kcalDiarias: 2400,
  proteinas: 195,
  carbos: 260,
  grasas: 65,
  comidas: [
    {
      nombre: 'Desayuno',
      notas: 'Tómalo entre 30 y 60 min después de levantarte. Si entrenas por la mañana, añade 20 g de proteína en polvo a la avena.',
      alimentos: [
        { nombre: 'Avena', cantidad: '80 g', kcal: 300, proteinas: 10, carbos: 54, grasas: 6 },
        { nombre: 'Leche desnatada', cantidad: '200 ml', kcal: 70, proteinas: 7, carbos: 10, grasas: 0 },
        { nombre: 'Plátano', cantidad: '1 unidad', kcal: 90, proteinas: 1, carbos: 23, grasas: 0 },
      ],
    },
    {
      nombre: 'Media mañana',
      alimentos: [
        { nombre: 'Frutos secos mixtos', cantidad: '30 g', kcal: 180, proteinas: 5, carbos: 6, grasas: 15 },
        { nombre: 'Yogur griego desnatado', cantidad: '200 g', kcal: 110, proteinas: 17, carbos: 8, grasas: 1 },
      ],
    },
    {
      nombre: 'Comida',
      notas: 'Es la toma más importante del día. No la saltes aunque no tengas hambre. Puedes variar la verdura pero mantén siempre las proteínas y los carbos.',
      alimentos: [
        { nombre: 'Pechuga de pollo', cantidad: '200 g', kcal: 220, proteinas: 46, carbos: 0, grasas: 4 },
        { nombre: 'Arroz integral', cantidad: '100 g en seco', kcal: 350, proteinas: 7, carbos: 74, grasas: 3 },
        { nombre: 'Brócoli al vapor', cantidad: '150 g', kcal: 50, proteinas: 4, carbos: 10, grasas: 0 },
        { nombre: 'Aceite de oliva virgen', cantidad: '1 cda', kcal: 90, proteinas: 0, carbos: 0, grasas: 10 },
      ],
    },
    {
      nombre: 'Merienda',
      alimentos: [
        { nombre: 'Pechuga de pavo', cantidad: '100 g', kcal: 110, proteinas: 24, carbos: 0, grasas: 1 },
        { nombre: 'Tortitas de arroz', cantidad: '2 unidades', kcal: 70, proteinas: 1, carbos: 14, grasas: 1 },
      ],
    },
    {
      nombre: 'Cena',
      notas: 'Cena al menos 2 horas antes de dormir. Si tienes hambre después, puedes tomar un yogur griego desnatado extra sin problema.',
      alimentos: [
        { nombre: 'Salmón al horno', cantidad: '180 g', kcal: 320, proteinas: 38, carbos: 0, grasas: 18 },
        { nombre: 'Patata cocida', cantidad: '150 g', kcal: 120, proteinas: 3, carbos: 27, grasas: 0 },
        { nombre: 'Espinacas salteadas', cantidad: '100 g', kcal: 35, proteinas: 3, carbos: 4, grasas: 1 },
      ],
    },
  ],
};

const MOCK_SOLICITUD_CHECKIN: SolicitudCheckIn = {
  id: 'checkin-req-1',
  fecha: '2026-04-16',
  solicitadoPor: 'Carlos López',
};

const MOCK_HISTORIAL_PESOS: CheckInPeso[] = [
  { id: 'w1', fecha: '2026-02-03', pesoKg: 85.2 },
  { id: 'w2', fecha: '2026-02-17', pesoKg: 84.0 },
  { id: 'w3', fecha: '2026-03-03', pesoKg: 83.1 },
  { id: 'w4', fecha: '2026-03-17', pesoKg: 82.4 },
  { id: 'w5', fecha: '2026-04-01', pesoKg: 81.5 },
];

const MOCK_NOTAS_NUTRICIONISTA: NotaNutricionista[] = [
  {
    id: 'nota-1',
    texto: 'Intenta comer las comidas principales siempre a la misma hora. La regularidad horaria ayuda a regular el metabolismo y reduce el hambre entre horas.',
    fecha: '2026-04-10',
  },
  {
    id: 'nota-2',
    texto: 'La hidratación es clave en tu fase de definición. Mínimo 2.5L de agua al día. Si entrenas, añade 500 ml extra por cada hora de sesión.',
    fecha: '2026-04-10',
  },
  {
    id: 'nota-3',
    texto: 'Si tienes hambre entre horas, opta por clara de huevo cocida, atún al natural o un yogur griego desnatado. Evita fruta sola por la noche.',
    fecha: '2026-04-14',
  },
];

const MOCK_HILOS: Record<string, HiloEjercicio> = {
  'Lunes-Press de banca': {
    sesionDia: 'Lunes',
    ejercicioNombre: 'Press de banca',
    notaEntrenador: 'Codos a 45° del torso, no los abras demasiado. Baja hasta que el pecho casi toque la barra y empuja de forma explosiva. Mantén los pies firmes en el suelo.',
    media: [
      { id: 'media-1', tipo: 'foto', url: '', fecha: '2026-04-15' },
    ],
    mensajes: [
      { id: 'm1', texto: '¿Estoy bajando suficiente los codos?', fecha: '2026-04-15', esAtleta: true, autor: 'Tú' },
      { id: 'm2', texto: 'Sí, pero intenta bajar un poco más la barra. Buen trabajo con la espalda recta.', fecha: '2026-04-15', esAtleta: false, autor: 'Carlos López' },
    ],
  },
  'Miércoles-Sentadilla': {
    sesionDia: 'Miércoles',
    ejercicioNombre: 'Sentadilla',
    notaEntrenador: 'Pies a la anchura de los hombros, puntas ligeramente hacia fuera. Baja por debajo del paralelo manteniendo la espalda neutra. Esta semana no añadas peso, trabaja la profundidad.',
    media: [],
    mensajes: [],
  },
};

const MOCK_HILOS_COMIDA: Record<string, HiloComida> = {
  'Desayuno': {
    comidaNombre: 'Desayuno',
    mensajes: [
      { id: 'hc1', texto: '¿Puedo sustituir la leche desnatada por bebida de avena?', fecha: '2026-04-12', esAtleta: true, autor: 'Tú' },
      { id: 'hc2', texto: 'Sí, sin problema. Elige la variante sin azúcares añadidos y sin enriquecer para que los macros se mantengan similares.', fecha: '2026-04-12', esAtleta: false, autor: 'María González' },
    ],
  },
  'Cena': {
    comidaNombre: 'Cena',
    mensajes: [
      { id: 'hc3', texto: '¿Puedo cambiar el salmón por merluza? No me gusta mucho el sabor', fecha: '2026-04-14', esAtleta: true, autor: 'Tú' },
      { id: 'hc4', texto: 'Claro, pero añade una cucharada de aceite de oliva extra para compensar las grasas saludables que aporta el salmón.', fecha: '2026-04-14', esAtleta: false, autor: 'María González' },
    ],
  },
};

const MOCK_CHAT_ENTRENADOR: Chat = {
  tipo: 'entrenador',
  interlocutor: 'Carlos López',
  mensajes: [
    { id: 'ce1', texto: '¡Hola! ¿Cómo llevas la semana?', fecha: '2026-04-10', esAtleta: false, autor: 'Carlos López' },
    { id: 'ce2', texto: 'Bien, aunque noto las piernas cargadas los lunes', fecha: '2026-04-10', esAtleta: true, autor: 'Tú' },
    { id: 'ce3', texto: 'Es normal en esta fase de carga. A partir de ahora nada de actividad intensa los domingos.', fecha: '2026-04-10', esAtleta: false, autor: 'Carlos López' },
    { id: 'ce4', texto: '¿Puedo cambiar el press militar por press inclinado?', fecha: '2026-04-12', esAtleta: true, autor: 'Tú' },
    { id: 'ce5', texto: 'Sí, sin problema. Mantén las mismas series y reps.', fecha: '2026-04-12', esAtleta: false, autor: 'Carlos López' },
    { id: 'ce6', texto: 'Perfecto, lo anoto. ¡Gracias!', fecha: '2026-04-12', esAtleta: true, autor: 'Tú' },
  ],
};

const MOCK_CHAT_NUTRICIONISTA: Chat = {
  tipo: 'nutricionista',
  interlocutor: 'María González',
  mensajes: [
    { id: 'cn1', texto: '¿Llevas bien el plan? Cualquier duda con los alimentos, me escribes.', fecha: '2026-04-11', esAtleta: false, autor: 'María González' },
    { id: 'cn2', texto: 'La verdad que sí, aunque el desayuno me resulta bastante grande', fecha: '2026-04-11', esAtleta: true, autor: 'Tú' },
    { id: 'cn3', texto: 'Puedes dividirlo en dos tomas: una antes de entrenar y otra después si entrenas por la mañana.', fecha: '2026-04-11', esAtleta: false, autor: 'María González' },
    { id: 'cn4', texto: 'Eso tiene más sentido, lo pruebo esta semana', fecha: '2026-04-13', esAtleta: true, autor: 'Tú' },
  ],
};

// ── Servicio ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class AtletaService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private readonly API = '/api/v1/atleta';

  getPerfil(): Observable<PerfilAtleta> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<PerfilAtleta>(`${this.API}/perfil`, { withCredentials: true });
    return of({
      ...MOCK_PERFIL,
      nombre: this.auth.nombre() ?? MOCK_PERFIL.nombre,
      servicio: (this.auth.servicio() as PerfilAtleta['servicio']) ?? MOCK_PERFIL.servicio,
    });
  }

  getProfesionalesAsignados(): Observable<ProfesionalAsignado[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<ProfesionalAsignado[]>(`${this.API}/profesionales`, { withCredentials: true });
    const s = this.auth.servicio();
    if (s === 'ENTRENAMIENTO') return of([MOCK_PROFESIONALES[0]]);
    if (s === 'NUTRICION')     return of([MOCK_PROFESIONALES[1]]);
    return of(MOCK_PROFESIONALES);
  }

  getPlanEntrenamiento(): Observable<PlanEntrenamiento | null> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<PlanEntrenamiento | null>(
    //   `${this.API}/entrenamiento/plan-activo`, { withCredentials: true }
    // );
    return of(MOCK_PLAN_ENTRENAMIENTO);
  }

  getPlanNutricion(): Observable<PlanNutricion | null> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<PlanNutricion | null>(
    //   `${this.API}/nutricion/plan-activo`, { withCredentials: true }
    // );
    return of(MOCK_PLAN_NUTRICION);
  }

  getSolicitudCheckIn(): Observable<SolicitudCheckIn | null> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<SolicitudCheckIn | null>(
    //   `${this.API}/peso/solicitud-pendiente`, { withCredentials: true }
    // );
    return of(MOCK_SOLICITUD_CHECKIN);
  }

  getHistorialPesos(): Observable<CheckInPeso[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<CheckInPeso[]>(`${this.API}/peso/historial`, { withCredentials: true });
    return of(MOCK_HISTORIAL_PESOS);
  }

  registrarPeso(solicitudId: string, pesoKg: number): Observable<CheckInPeso> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.post<CheckInPeso>(`${this.API}/peso`, { solicitudId, pesoKg }, { withCredentials: true });
    const hoy = new Date().toISOString().split('T')[0];
    return of({ id: 'w-nuevo', fecha: hoy, pesoKg });
  }

  getNotasNutricionista(): Observable<NotaNutricionista[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<NotaNutricionista[]>(`${this.API}/nutricion/notas`, { withCredentials: true });
    return of(MOCK_NOTAS_NUTRICIONISTA);
  }

  cambiarPassword(actual: string, nueva: string): Observable<void> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.put<void>(`${this.API}/password`, { actual, nueva }, { withCredentials: true });
    return of(undefined);
  }

  getHiloEjercicio(sesionDia: string, ejercicioNombre: string): Observable<HiloEjercicio> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<HiloEjercicio>(
    //   `${this.API}/entrenamiento/hilo?dia=${sesionDia}&ejercicio=${encodeURIComponent(ejercicioNombre)}`,
    //   { withCredentials: true }
    // );
    const clave = `${sesionDia}-${ejercicioNombre}`;
    return of(MOCK_HILOS[clave] ?? { sesionDia, ejercicioNombre, media: [], mensajes: [] });
  }

  enviarMensajeHilo(sesionDia: string, ejercicioNombre: string, texto: string): Observable<MensajeHilo> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.post<MensajeHilo>(`${this.API}/entrenamiento/hilo/mensaje`,
    //   { sesionDia, ejercicioNombre, texto }, { withCredentials: true }
    // );
    const hoy = new Date().toISOString().split('T')[0];
    return of({ id: `m-${Date.now()}`, texto, fecha: hoy, esAtleta: true, autor: 'Tú' });
  }

  getChat(tipo: 'entrenador' | 'nutricionista'): Observable<Chat> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<Chat>(`${this.API}/chat/${tipo}`, { withCredentials: true });
    return of(tipo === 'entrenador' ? { ...MOCK_CHAT_ENTRENADOR, mensajes: [...MOCK_CHAT_ENTRENADOR.mensajes] }
                                    : { ...MOCK_CHAT_NUTRICIONISTA, mensajes: [...MOCK_CHAT_NUTRICIONISTA.mensajes] });
  }

  enviarMensajeChat(tipo: 'entrenador' | 'nutricionista', texto: string, adjunto?: AdjuntoChat): Observable<MensajeChat> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // const form = new FormData();
    // if (texto) form.append('texto', texto);
    // if (adjunto?.archivo) form.append('archivo', adjunto.archivo);
    // return this.http.post<MensajeChat>(`${this.API}/chat/${tipo}/mensaje`, form, { withCredentials: true });
    const hoy = new Date().toISOString().split('T')[0];
    return of({ id: `chat-${Date.now()}`, texto, fecha: hoy, esAtleta: true, autor: 'Tú', adjunto });
  }

  subirArchivoChat(tipo: 'entrenador' | 'nutricionista', archivo: File): Observable<AdjuntoChat> {
    // ── REAL (requiere S3) ─────────────────────────────────────────────────
    // const form = new FormData();
    // form.append('archivo', archivo);
    // return this.http.post<AdjuntoChat>(`${this.API}/chat/${tipo}/archivo`, form, { withCredentials: true });
    const tipoMedia: 'foto' | 'video' = archivo.type.startsWith('video') ? 'video' : 'foto';
    return of({ url: URL.createObjectURL(archivo), tipo: tipoMedia, nombre: archivo.name });
  }

  getHiloComida(comidaNombre: string): Observable<HiloComida> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.get<HiloComida>(
    //   `${this.API}/nutricion/hilo?comida=${encodeURIComponent(comidaNombre)}`,
    //   { withCredentials: true }
    // );
    return of(MOCK_HILOS_COMIDA[comidaNombre] ?? { comidaNombre, mensajes: [] });
  }

  enviarMensajeHiloComida(comidaNombre: string, texto: string): Observable<MensajeHiloComida> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // return this.http.post<MensajeHiloComida>(`${this.API}/nutricion/hilo/mensaje`,
    //   { comidaNombre, texto }, { withCredentials: true }
    // );
    const hoy = new Date().toISOString().split('T')[0];
    return of({ id: `hc-${Date.now()}`, texto, fecha: hoy, esAtleta: true, autor: 'Tú' });
  }

  subirMediaHilo(sesionDia: string, ejercicioNombre: string, archivo: File): Observable<MediaAdjunto> {
    // ── REAL (requiere S3) ─────────────────────────────────────────────────
    // const form = new FormData();
    // form.append('archivo', archivo);
    // form.append('sesionDia', sesionDia);
    // form.append('ejercicioNombre', ejercicioNombre);
    // return this.http.post<MediaAdjunto>(`${this.API}/entrenamiento/hilo/media`, form, { withCredentials: true });
    const hoy = new Date().toISOString().split('T')[0];
    const tipo: 'foto' | 'video' = archivo.type.startsWith('video') ? 'video' : 'foto';
    return of({ id: `media-${Date.now()}`, tipo, url: URL.createObjectURL(archivo), fecha: hoy });
  }
}
