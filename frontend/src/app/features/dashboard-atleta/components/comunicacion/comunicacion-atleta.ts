import { Component, inject, input, output, signal, computed, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AtletaService, SolicitudCheckIn } from '../../services/atleta.service';

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
  categoria: string;
  contexto: string;
  creadoPor: string;
  creadoEn: string; // ISO String
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
  fecha: Date;          // Cambiado de fechaAbierto a fecha
  leido: boolean;
  total: number;        // Agregado para el conteo de mensajes
  mensajes: MensajeHilo[]; // Mantenerlo como opcional o inicializar vacío
}

type Vista = 'lista' | 'detalle' | 'nuevo';

@Component({
  selector: 'app-comunicacion-atleta',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './comunicacion-atleta.html',
})
export class ComunicacionAtletaComponent implements OnInit, OnDestroy {
  private atleta = inject(AtletaService);
  private http = inject(HttpClient);

  private readonly API = '/api/v1/comunicacion';
  private readonly opts = { withCredentials: true };

  readonly atletaId = input.required<string>();
  readonly contexto = input<'ENTRENAMIENTO' | 'NUTRICION'>('ENTRENAMIENTO');

  readonly profesionalNombre = input<string>('Tu profesional');
  readonly solicitudCheckIn = input<SolicitudCheckIn | null>(null);
  readonly checkInCompletado = output<void>();

  // ── Check-in de peso ──────────────────────────────────────────────────────
  readonly pesoInput = signal('');
  readonly enviandoPeso = signal(false);
  readonly checkInHecho = signal(false);

  readonly pesoValido = computed(() => {
    const v = parseFloat(this.pesoInput().replace(',', '.'));
    return !isNaN(v) && v >= 30 && v <= 300;
  });

  registrarPeso(): void {
    const solicitud = this.solicitudCheckIn();
    if (!solicitud || !this.pesoValido()) return;
    const kg = parseFloat(this.pesoInput().replace(',', '.'));
    this.enviandoPeso.set(true);
    this.atleta.registrarPeso(solicitud.id, kg).subscribe({
      next: () => {
        this.checkInHecho.set(true);
        this.pesoInput.set('');
        this.enviandoPeso.set(false);
        this.checkInCompletado.emit();
      },
      error: () => this.enviandoPeso.set(false),
    });
  }

  // ── Hilos ─────────────────────────────────────────────────────────────────
  readonly vista = signal<Vista>('lista');
  readonly hilos = signal<Hilo[]>([]);
  readonly hiloActivo = signal<Hilo | null>(null);

  // ── Formulario nuevo hilo ─────────────────────────────────────────────────
  readonly nuevoTitulo = signal('');
  readonly nuevaCategoria = signal<CategoriaHilo>('apunte');
  readonly nuevoTexto = signal('');
  readonly nuevoAdjuntos = signal<Adjunto[]>([]);
  private nuevoFiles = new Map<string, File>();

  // ── Lightbox ──────────────────────────────────────────────────────────────
  readonly zoomUrl = signal<string | null>(null);

  // ── Formulario respuesta ──────────────────────────────────────────────────
  readonly textoRespuesta = signal('');
  readonly respuestaAdjuntos = signal<Adjunto[]>([]);
  private respuestaFiles = new Map<string, File>();

  readonly categorias: { value: CategoriaHilo; label: string }[] = [
    { value: 'tecnica', label: 'TÉCNICA' },
    { value: 'duda', label: 'DUDA' },
    { value: 'apunte', label: 'APUNTE' },
  ];

  readonly busqueda = signal('');
  readonly filtroCategoria = signal<CategoriaHilo | 'TODOS'>('TODOS');

  readonly hilosNoLeidos = computed(() => this.hilos().filter(h => !h.leido).length);

  readonly ultimoMensaje = (hilo: Hilo): MensajeHilo => {
    if (!hilo?.mensajes || hilo.mensajes.length === 0) {
      return { id: '', texto: '', de: 'atleta', fecha: new Date() } as MensajeHilo;
    }
    return hilo.mensajes[hilo.mensajes.length - 1];
  };

  readonly hilosFiltrados = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    const filtro = this.filtroCategoria();
    return this.hilos().filter(h => {
      const coincideCategoria = filtro === 'TODOS' || h.categoria === filtro;
      const coincideBusqueda = !q ||
        h.titulo.toLowerCase().includes(q) ||
        h.mensajes.some(m => m.texto.toLowerCase().includes(q));
      return coincideCategoria && coincideBusqueda;
    });
  });

  ngOnInit(): void {
    this.cargarHilos();
  }

  private cargarHilos(): void {
    const ctx = this.contexto();
    this.http.get<HiloResumenDTO[]>(`${this.API}/atleta/hilos?contexto=${ctx}`, this.opts)
      .subscribe({
        next: (res) => {
          this.hilos.set(res.map(h => this.mapDtoToHilo(h)));
        },
        error: (err) => console.error("Fallo en la arquitectura: no se pudieron cargar hilos", err)
      });
  }

  ngOnDestroy(): void {
    this.nuevoAdjuntos().forEach(a => URL.revokeObjectURL(a.url));
    this.respuestaAdjuntos().forEach(a => URL.revokeObjectURL(a.url));
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

  getHilosAtleta(ctx: string): Observable<HiloResumenDTO[]> {
    const params = new HttpParams().set('contexto', ctx);
    return this.http.get<HiloResumenDTO[]>(`${this.API}/atleta/hilos`, { params });
  }

  abrirHilo(hilo: Hilo): void {
    this.hilos.update(list => list.map(h => h.id === hilo.id ? { ...h, leido: true } : h));
    this.hiloActivo.set(this.hilos().find(h => h.id === hilo.id) ?? hilo);
    this.textoRespuesta.set('');
    this.respuestaAdjuntos.set([]);
    this.vista.set('detalle');

    this.http.get<any>(`${this.API}/hilos/${hilo.id}`, this.opts)
      .subscribe(h => {
        const hiloCompleto: Hilo = {
          id: h.id,
          titulo: h.titulo,
          categoria: h.categoria.toLowerCase() as CategoriaHilo,
          de: h.de === 'ATLETA' ? 'atleta' : 'entrenador',
          fecha: new Date(h.fechaAbierto),
          leido: true,
          total: h.mensajes.length,
          mensajes: h.mensajes.map((m: any) => ({
            id: m.id,
            texto: m.texto ?? '',
            de: m.de === 'ATLETA' ? 'atleta' : 'entrenador',
            fecha: new Date(m.fecha),
            adjuntos: m.adjuntos?.map((a: any) => ({
              id: a.id, url: a.url,
              tipo: a.tipo.toLowerCase() as 'imagen' | 'video',
              nombre: a.nombre,
            }))
          })),
        };
        this.hiloActivo.set(hiloCompleto);
      });
  }

  volverALista(): void {
    this.hiloActivo.set(null);
    this.vista.set('lista');
  }

  // ── Acciones ──────────────────────────────────────────────────────────────

  crearHilo(): void {
    const titulo = this.nuevoTitulo().trim();
    const texto = this.nuevoTexto().trim();
    if (!titulo || !texto) return;

    const idTemporal = crypto.randomUUID();

    // 1. DTO siguiendo el estándar del Entrenador (Blob JSON)
    const datosDTO = {
      atletaId: this.atletaId(),
      titulo: titulo,
      categoria: this.nuevaCategoria().toUpperCase(),
      contexto: this.contexto(),
      texto: texto
    };

    const fd = new FormData();
    fd.append('datos', new Blob([JSON.stringify(datosDTO)], { type: 'application/json' }));
    this.nuevoFiles.forEach(file => fd.append('archivos', file));

    // 2. Optimistic UI: Mostrar el hilo antes de la respuesta del servidor
    const nuevoHiloLocal: Hilo = {
      id: idTemporal,
      titulo,
      categoria: this.nuevaCategoria(),
      de: 'atleta',
      fecha: new Date(),
      leido: true,
      total: 1,
      mensajes: []
    };

    this.hilos.update(prev => [nuevoHiloLocal, ...prev]);
    this.resetFormularioNuevo();

    // 3. Persistencia
    this.http.post<any>(`${this.API}/hilos`, fd, this.opts).subscribe({
      next: (res) => {
        const idReal = res.id || res.data?.id;
        this.hilos.update(list => list.map(h => h.id === idTemporal ? { ...h, id: idReal } : h));
      },
      error: () => {
        // Rollback si falla
        this.hilos.update(list => list.filter(h => h.id !== idTemporal));
      }
    });
  }

  responder(): void {
    const texto = this.textoRespuesta().trim();
    const hilo = this.hiloActivo();
    if (!hilo || (!texto && this.respuestaFiles.size === 0)) return;

    const fd = new FormData();
    if (texto) fd.append('texto', new Blob([texto], { type: 'text/plain' }));
    this.respuestaFiles.forEach(f => fd.append('archivos', f));

    this.http.post<any>(`${this.API}/hilos/${hilo.id}/mensajes`, fd, this.opts)
      .subscribe({
        next: () => {
          this.limpiarRespuesta();
          this.abrirHilo(hilo); // Refrescar para obtener datos del servidor
        }
      });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private mapDtoToHilo(h: HiloResumenDTO): Hilo {
    return {
      id: h.id,
      titulo: h.titulo,
      categoria: h.categoria.toLowerCase() as CategoriaHilo,
      de: h.creadoPor === 'ATLETA' ? 'atleta' : 'entrenador',
      fecha: new Date(h.creadoEn),
      leido: h.leido,
      total: h.totalMensajes,
      mensajes: []
    };
  }

  private resetFormularioNuevo(): void {
    this.nuevoTitulo.set('');
    this.nuevoTexto.set('');
    this.nuevoAdjuntos.set([]);
    this.nuevoFiles.clear();
    this.vista.set('lista');
  }

  private limpiarRespuesta(): void {
    this.textoRespuesta.set('');
    this.respuestaAdjuntos.set([]);
    this.respuestaFiles.clear();
  }

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
}
