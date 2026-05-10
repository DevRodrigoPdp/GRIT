import { Component, inject, input, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SeguimientoService, CheckInPeso } from '../../services/seguimiento.service';
import { HttpClient } from '@angular/common/http';

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

export interface Hilo {
  id: string;
  titulo: string;
  categoria: CategoriaHilo;
  de: 'entrenador' | 'atleta';
  fechaAbierto: Date;
  mensajes: MensajeHilo[];
  leido: boolean;
}

type Vista = 'lista' | 'detalle' | 'nuevo';

interface ChartPoint { x: number; y: number; peso: number; fecha: string; }

@Component({
  selector: 'app-comunicacion',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './comunicacion.html',
})
export class ComunicacionComponent implements OnInit {
  private seg = inject(SeguimientoService);
  private http = inject(HttpClient);

  private readonly API = '/api/v1/comunicacion';

  readonly atletaId = input.required<string>();
  readonly atletaNombre = input.required<string>();
  readonly servicio = input<string>('');

  // ── Hilos ─────────────────────────────────────────────────────────────────
  readonly vista = signal<Vista>('lista');
  readonly hiloActivo = signal<Hilo | null>(null);
  readonly hilos = signal<Hilo[]>([]);

  // ── Formulario nuevo hilo ─────────────────────────────────────────────────
  nuevoTitulo = '';
  nuevaCategoria = signal<CategoriaHilo>('apunte');
  nuevoTexto = '';
  readonly nuevoAdjuntos = signal<Adjunto[]>([]);
  // Cuando se conecte la API: guardar los File reales aquí en paralelo a nuevoAdjuntos
  private nuevoFiles = new Map<string, File>();

  // ── Formulario respuesta ──────────────────────────────────────────────────
  textoRespuesta = '';
  readonly respuestaAdjuntos = signal<Adjunto[]>([]);
  private respuestaFiles = new Map<string, File>();

  readonly categorias: { value: CategoriaHilo; label: string }[] = [
    { value: 'tecnica', label: 'TÉCNICA' },
    { value: 'duda', label: 'DUDA' },
    { value: 'apunte', label: 'APUNTE' },
  ];

  busqueda = '';
  filtroCategoria = signal<CategoriaHilo | 'TODOS'>('TODOS');

  readonly hilosNoLeidos = computed(() => this.hilos().filter(h => !h.leido).length);
  readonly ultimoMensaje = (hilo: Hilo): MensajeHilo => {
    // Si no hay mensajes, devolvemos un objeto vacío con la estructura de MensajeHilo
    // para evitar que el HTML intente leer propiedades de 'undefined'
    if (!hilo?.mensajes || hilo.mensajes.length === 0) {
      return {
        id: '',
        texto: '',
        de: 'atleta', // Valor por defecto del DTO
        fecha: new Date()
      } as MensajeHilo;
    }
    return hilo.mensajes[hilo.mensajes.length - 1];
  };

  readonly hilosFiltrados = computed(() => {
    const q = this.busqueda.trim().toLowerCase();
    const filtro = this.filtroCategoria();
    return this.hilos().filter(h => {
      const coincideCategoria = filtro === 'TODOS' || h.categoria === filtro;
      const coincideBusqueda = !q ||
        h.titulo.toLowerCase().includes(q) ||
        h.mensajes.some(m => m.texto.toLowerCase().includes(q));
      return coincideCategoria && coincideBusqueda;
    });
  });

  // ── Seguimiento de peso ───────────────────────────────────────────────────
  historialPesos = signal<CheckInPeso[]>([]);
  checkInPendiente = signal(false);
  solicitando = signal(false);

  readonly chartData = computed<{ points: ChartPoint[]; polyline: string } | null>(() => {
    const pesos = this.historialPesos();
    if (pesos.length < 2) return null;
    const W = 460, H = 60, padX = 20, padY = 8;
    const weights = pesos.map(p => p.pesoKg);
    const minW = Math.min(...weights) - 1;
    const maxW = Math.max(...weights) + 1;
    const toX = (i: number) => padX + (i / (pesos.length - 1)) * (W - 2 * padX);
    const toY = (w: number) => padY + H - ((w - minW) / (maxW - minW)) * H;
    const points: ChartPoint[] = pesos.map((p, i) => ({
      x: toX(i), y: toY(p.pesoKg), peso: p.pesoKg, fecha: p.fecha,
    }));
    return { points, polyline: points.map(p => `${p.x},${p.y}`).join(' ') };
  });

  ngOnInit(): void {
    const id = this.atletaId();
    this.seg.getHistorialPesos(id).subscribe(h => this.historialPesos.set(h));
    this.seg.tieneCheckInPendiente(id).subscribe(b => this.checkInPendiente.set(b));

    const ctx = this.servicio() === 'NUTRICION' ? 'NUTRICION' : 'ENTRENAMIENTO';

    this.http.get<any>(
      `${this.API}/hilos?atletaId=${id}&contexto=${ctx}`
    ).subscribe({
      next: (res: any) => {
        const arr: any[] = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : []);
        const hilosMapeados = arr.map((h: any) => ({
          id: h.id,
          titulo: h.titulo,
          categoria: h.categoria.toLowerCase() as CategoriaHilo,
          de: (h.de === 'ENTRENADOR' ? 'entrenador' : 'atleta') as 'entrenador' | 'atleta',
          fechaAbierto: new Date(h.fechaAbierto),
          leido: h.leidoPorMi,
          mensajes: [],
        }));
        this.hilos.set(hilosMapeados);
      },
      error: () => {}
    });
  }

  // ── Multimedia ────────────────────────────────────────────────────────────

  adjuntarArchivos(event: Event, destino: 'nuevo' | 'respuesta'): void {
    const files = (event.target as HTMLInputElement).files;
    if (!files) return;

    Array.from(files).forEach(file => {
      const id = crypto.randomUUID(); // ID único para vincular UI y Archivo
      const tipo: 'imagen' | 'video' = file.type.startsWith('video/') ? 'video' : 'imagen';
      const url = URL.createObjectURL(file);

      const adjunto: Adjunto = { id, url, tipo, nombre: file.name };

      if (destino === 'nuevo') {
        this.nuevoAdjuntos.update(l => [...l, adjunto]);
        this.nuevoFiles.set(id, file);
      } else {
        this.respuestaAdjuntos.update(l => [...l, adjunto]);
        this.respuestaFiles.set(id, file);
      }
    });
    (event.target as HTMLInputElement).value = '';
  }

  quitarAdjunto(id: string, destino: 'nuevo' | 'respuesta'): void {
    const lista = destino === 'nuevo' ? this.nuevoAdjuntos : this.respuestaAdjuntos;
    const mapa = destino === 'nuevo' ? this.nuevoFiles : this.respuestaFiles;

    const adjunto = lista().find(a => a.id === id);
    if (adjunto) URL.revokeObjectURL(adjunto.url); // Liberar memoria RAM

    lista.update(l => l.filter(a => a.id !== id));
    mapa.delete(id);
  }

  // ── Navegación ────────────────────────────────────────────────────────────

  abrirHilo(hilo: Hilo): void {
    this.hilos.update(list => list.map(h => h.id === hilo.id ? { ...h, leido: true } : h));
    this.hiloActivo.set(this.hilos().find(h => h.id === hilo.id) ?? hilo);
    this.textoRespuesta = '';
    this.respuestaAdjuntos.set([]);
    this.vista.set('detalle');

    //── Descomentar para cargar mensajes completos desde la API ────────────
    //El GET /hilos/:hiloId ya marca el hilo como leído en el backend (no hace falta PUT /leer aparte)
    this.http.get<any>(`${this.API}/hilos/${hilo.id}`)
      .subscribe(r => {
        if (!r) return;
        const h = r?.data ?? r;
        const hiloCompleto: Hilo = {
          id: h.id,
          titulo: h.titulo,
          categoria: h.categoria.toLowerCase() as CategoriaHilo,
          de: h.de === 'ENTRENADOR' ? 'entrenador' : 'atleta',
          fechaAbierto: new Date(h.fechaAbierto),
          leido: true,
          mensajes: h.mensajes.map((m: any) => ({
            id: m.id,
            texto: m.texto ?? '',
            de: m.de === 'ENTRENADOR' ? 'entrenador' : 'atleta',
            fecha: new Date(m.fecha),
            adjuntos: m.adjuntos?.map((a: any) => ({
              id: a.id, url: a.url,
              tipo: a.tipo.toLowerCase() as 'imagen' | 'video',
              nombre: a.nombre,
            })),
          })),
        };
        this.hilos.update(list => list.map(x => x.id === hilo.id ? hiloCompleto : x));
        this.hiloActivo.set(hiloCompleto);
      });
  }

  volverALista(): void {
    this.hiloActivo.set(null);
    this.vista.set('lista');
  }

  // ── Acciones ──────────────────────────────────────────────────────────────

  crearHilo(): void {
    const titulo = this.nuevoTitulo.trim();
    const texto = this.nuevoTexto.trim();
    if (!titulo || !texto) return;

    // Determinamos el contexto según el input del componente
    const ctx = this.servicio() === 'NUTRICION' ? 'NUTRICION' : 'ENTRENAMIENTO';

    // 1. Creamos el objeto local para la UI (Optimistic UI)
    // Usamos un ID temporal que luego reemplazaremos con el del servidor
    const idTemporal = crypto.randomUUID();
    const hiloLocal: Hilo = {
      id: idTemporal,
      titulo,
      categoria: this.nuevaCategoria(),
      de: 'entrenador',
      fechaAbierto: new Date(),
      leido: true,
      mensajes: [{
        id: crypto.randomUUID(),
        texto,
        de: 'entrenador',
        fecha: new Date(),
        adjuntos: this.nuevoAdjuntos().length ? [...this.nuevoAdjuntos()] : undefined,
      }],
    };

    // Actualizamos la lista localmente para que el usuario vea el hilo al instante
    this.hilos.update(list => [hiloLocal, ...list]);

    // 2. Preparamos el FormData para el Backend
    const datosDTO = {
      atletaId: this.atletaId(),
      titulo: titulo,
      categoria: this.nuevaCategoria().toUpperCase(), // Spring suele esperar Enums en Mayúsculas
      contexto: ctx,
      texto: texto
    };

    const fd = new FormData();

    // Convertimos el DTO a Blob JSON para cumplir con @RequestPart("datos")
    fd.append('datos', new Blob([JSON.stringify(datosDTO)], {
      type: 'application/json'
    }));

    // Extraemos los archivos reales de nuestro Map y los añadimos
    Array.from(this.nuevoFiles.values()).forEach(f => fd.append('archivos', f));

    // 3. Petición al Servidor
    this.http.post<any>(`${this.API}/hilos`, fd)
      .subscribe({
        next: (response) => {
          // El backend devuelve el Hilo real. Actualizamos el ID temporal por el real.
          // Nota: Ajusta 'response.id' según la estructura exacta de tu retorno.
          const servidorId = response.id || response.data?.id;

          this.hilos.update(list =>
            list.map(h => h.id === idTemporal ? { ...h, id: servidorId } : h)
          );

          // Limpieza de archivos y memoria
          this.nuevoAdjuntos().forEach(a => URL.revokeObjectURL(a.url));
          this.nuevoFiles.clear();
        },
        error: () => {
          this.hilos.update(list => list.filter(h => h.id !== idTemporal));
        }
      });

    // 4. Limpieza de la interfaz
    this.nuevoTitulo = '';
    this.nuevoTexto = '';
    this.nuevoAdjuntos.set([]);
    this.vista.set('lista');
  }

  responder(): void {
    const texto = this.textoRespuesta.trim();
    const hilo = this.hiloActivo();
    if ((!texto && this.respuestaFiles.size === 0) || !hilo) return;

    const fd = new FormData();

    // Enviamos el texto como una parte
    if (texto) {
      fd.append('texto', new Blob([texto], { type: 'text/plain' }));
    }

    Array.from(this.respuestaFiles.values()).forEach(f => fd.append('archivos', f));

    this.http.post<any>(`${this.API}/hilos/${hilo.id}/mensajes`, fd)
      .subscribe(() => {
        this.respuestaFiles.clear();
        this.textoRespuesta = '';
        this.respuestaAdjuntos.set([]);
        this.abrirHilo(hilo); // Refrescar el hilo para ver el nuevo mensaje
      });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  labelCategoria(cat: CategoriaHilo): string {
    return { tecnica: 'TÉCNICA', duda: 'DUDA', apunte: 'APUNTE' }[cat];
  }

  categoriaEstilo(cat: CategoriaHilo): { borde: string; texto: string; fondo: string } {
    const map: Record<CategoriaHilo, { borde: string; texto: string; fondo: string }> = {
      tecnica: { borde: '#2ED38D', texto: '#2ED38D', fondo: 'rgba(46,211,141,0.06)' },
      duda: { borde: '#F97316', texto: '#F97316', fondo: 'rgba(249,115,22,0.06)' },
      apunte: { borde: '#000000', texto: '#000000', fondo: 'rgba(0,0,0,0.02)' },
    };
    return map[cat];
  }

  formatearFecha(fecha: Date): string {
    const hoy = new Date();
    const ayer = new Date(hoy); ayer.setDate(hoy.getDate() - 1);
    const hora = fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
    if (fecha.toDateString() === hoy.toDateString()) return `Hoy · ${hora}`;
    if (fecha.toDateString() === ayer.toDateString()) return `Ayer · ${hora}`;
    return fecha.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }) + ` · ${hora}`;
  }

  // ── Seguimiento de peso ───────────────────────────────────────────────────

  solicitarPeso(): void {
    const rol: 'ENTRENADOR' | 'NUTRICIONISTA' =
      this.servicio() === 'NUTRICION' ? 'NUTRICIONISTA' : 'ENTRENADOR';
    this.solicitando.set(true);
    this.seg.solicitarCheckIn(this.atletaId(), rol).subscribe(() => {
      this.checkInPendiente.set(true);
      this.solicitando.set(false);
    });
  }

  ngOnDestroy(): void {
    // Liberar todas las URLs de previsualización al destruir el componente
    this.nuevoAdjuntos().forEach(a => URL.revokeObjectURL(a.url));
    this.respuestaAdjuntos().forEach(a => URL.revokeObjectURL(a.url));
  }
}
