import { Component, inject, input, output, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
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

@Component({
  selector: 'app-comunicacion-atleta',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './comunicacion-atleta.html',
})
export class ComunicacionAtletaComponent implements OnInit {
  private atleta = inject(AtletaService);
  private http = inject(HttpClient);

  private readonly API = '/api/v1/comunicacion';

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
  readonly hiloActivo = signal<Hilo | null>(null);
  readonly hilos = signal<Hilo[]>([]);

  // ── Formulario nuevo hilo ─────────────────────────────────────────────────
  nuevoTitulo = '';
  nuevaCategoria = signal<CategoriaHilo>('apunte');
  nuevoTexto = '';
  readonly nuevoAdjuntos = signal<Adjunto[]>([]);
  private nuevoFiles = new Map<string, File>();
  readonly creandoHilo = signal(false);

  // ── Formulario respuesta ──────────────────────────────────────────────────
  textoRespuesta = '';
  readonly respuestaAdjuntos = signal<Adjunto[]>([]);
  private respuestaFiles = new Map<string, File>();
  readonly respondiendo = signal(false);

  readonly categorias: { value: CategoriaHilo; label: string }[] = [
    { value: 'tecnica', label: 'TÉCNICA' },
    { value: 'duda', label: 'DUDA' },
    { value: 'apunte', label: 'APUNTE' },
  ];

  busqueda = '';
  filtroCategoria = signal<CategoriaHilo | 'TODOS'>('TODOS');

  readonly hilosNoLeidos = computed(() => this.hilos().filter(h => !h.leido).length);
  readonly ultimoMensaje = (hilo: Hilo): MensajeHilo => {
    if (!hilo?.mensajes || hilo.mensajes.length === 0) {
      return {
        id: '',
        texto: '',
        de: 'atleta',
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

  ngOnInit(): void {
    const id = this.atletaId();
    const ctx = this.contexto();
    this.http.get<any[]>(
      `${this.API}/hilos?atletaId=${id}&contexto=${ctx}`
    ).subscribe({
      next: (res) => {
        if (Array.isArray(res)) {
          const hilosMapeados = res.map((h: any) => ({
            id: h.id,
            titulo: h.titulo,
            categoria: h.categoria.toLowerCase() as CategoriaHilo,
            de: (h.creadoPor === 'ATLETA' ? 'atleta' : 'entrenador') as 'entrenador' | 'atleta',
            fechaAbierto: new Date(h.fechaAbierto),
            leido: h.leidoPorMi,
            mensajes: [],
          }));
          this.hilos.set(hilosMapeados);
        }
      },
      error: (err) => console.error("Error cargando hilos:", err)
    });
  }

  // ── Multimedia ────────────────────────────────────────────────────────────

  adjuntarArchivos(event: Event, destino: 'nuevo' | 'respuesta'): void {
    const files = (event.target as HTMLInputElement).files;
    if (!files) return;

    const lista = destino === 'nuevo' ? this.nuevoAdjuntos : this.respuestaAdjuntos;
    const mapa = destino === 'nuevo' ? this.nuevoFiles : this.respuestaFiles;

    Array.from(files).forEach(file => {
      const id = crypto.randomUUID();
      const tipo: 'imagen' | 'video' = file.type.startsWith('video/') ? 'video' : 'imagen';
      const url = URL.createObjectURL(file);

      const adjunto: Adjunto = { id, url, tipo, nombre: file.name };
      lista.update(l => [...l, adjunto]);
      mapa.set(id, file);
    });
    (event.target as HTMLInputElement).value = '';
  }

  quitarAdjunto(id: string, destino: 'nuevo' | 'respuesta'): void {
    const lista = destino === 'nuevo' ? this.nuevoAdjuntos : this.respuestaAdjuntos;
    const mapa = destino === 'nuevo' ? this.nuevoFiles : this.respuestaFiles;

    const adjunto = lista().find(a => a.id === id);
    if (adjunto) URL.revokeObjectURL(adjunto.url);

    lista.update(l => l.filter(a => a.id !== id));
    mapa.delete(id);
  }

  // ── Navegación ────────────────────────────────────────────────────────────

  abrirHilo(hilo: Hilo): void {
    this.hilos.update(list => list.map(h => h.id === hilo.id ? { ...h, leido: true } : h));
    this.textoRespuesta = '';
    this.respuestaAdjuntos.set([]);
    this.respuestaFiles.clear();
    this.vista.set('detalle');

    // Cargar detalles del hilo desde la API
    this.http.get<any>(`${this.API}/hilos/${hilo.id}`)
      .subscribe({
        next: (h) => {
          const hiloCompleto: Hilo = {
            id: h.id,
            titulo: h.titulo,
            categoria: h.categoria.toLowerCase() as CategoriaHilo,
            de: (h.de === 'ATLETA' ? 'atleta' : 'entrenador') as 'entrenador' | 'atleta',
            fechaAbierto: new Date(h.fechaAbierto),
            leido: true,
            mensajes: h.mensajes.map((m: any) => ({
              id: m.id,
              texto: m.texto ?? '',
              de: (m.de === 'ATLETA' ? 'atleta' : 'entrenador') as 'entrenador' | 'atleta',
              fecha: new Date(m.fecha),
              adjuntos: m.adjuntos?.map((a: any) => ({
                id: a.id,
                url: a.url,
                tipo: a.tipo.toLowerCase() as 'imagen' | 'video',
                nombre: a.nombre,
              })),
            })),
          };
          this.hilos.update(list => list.map(x => x.id === hilo.id ? hiloCompleto : x));
          this.hiloActivo.set(hiloCompleto);
        },
        error: (err) => console.error("Error cargando detalle del hilo:", err)
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

    this.creandoHilo.set(true);

    // Preparar FormData con la estructura esperada por el backend
    const fd = new FormData();

    // Crear JSON de datos para la parte "datos"
    const datosJSON = JSON.stringify({
      atletaId: this.atletaId(),
      titulo: titulo,
      categoria: this.nuevaCategoria().toUpperCase(),
      contexto: this.contexto(),
      texto: texto,
    });

    // Crear un Blob con los datos
    const datosBlob = new Blob([datosJSON], { type: 'application/json' });
    fd.append('datos', datosBlob, 'datos.json');

    // Agregar archivos si existen
    this.nuevoFiles.forEach((file, id) => {
      fd.append('archivos', file, file.name);
    });

    // Enviar a la API
    this.http.post<any>(`${this.API}/hilos`, fd)
      .subscribe({
        next: (hiloCreado) => {
          const nuevoHilo: Hilo = {
            id: hiloCreado.id,
            titulo: hiloCreado.titulo,
            categoria: hiloCreado.categoria.toLowerCase() as CategoriaHilo,
            de: 'atleta',
            fechaAbierto: new Date(hiloCreado.fechaAbierto),
            leido: true,
            mensajes: hiloCreado.mensajes.map((m: any) => ({
              id: m.id,
              texto: m.texto,
              de: (m.de === 'ATLETA' ? 'atleta' : 'entrenador') as 'entrenador' | 'atleta',
              fecha: new Date(m.fecha),
              adjuntos: m.adjuntos?.map((a: any) => ({
                id: a.id,
                url: a.url,
                tipo: a.tipo.toLowerCase() as 'imagen' | 'video',
                nombre: a.nombre,
              })),
            })),
          };

          this.hilos.update(list => [nuevoHilo, ...list]);
          this.nuevoTitulo = '';
          this.nuevoTexto = '';
          this.nuevaCategoria.set('apunte');
          this.nuevoAdjuntos.set([]);
          this.nuevoFiles.clear();
          this.vista.set('lista');
          this.creandoHilo.set(false);
        },
        error: (err) => {
          console.error("Error creando hilo:", err);
          this.creandoHilo.set(false);
        }
      });
  }

  responder(): void {
    const texto = this.textoRespuesta.trim();
    const hilo = this.hiloActivo();
    if ((!texto && !this.respuestaAdjuntos().length) || !hilo) return;

    this.respondiendo.set(true);

    // Preparar FormData
    const fd = new FormData();
    if (texto) {
      fd.append('texto', texto);
    }

    // Agregar archivos si existen
    this.respuestaFiles.forEach((file, id) => {
      fd.append('archivos', file, file.name);
    });

    // Enviar respuesta
    this.http.post<any>(`${this.API}/hilos/${hilo.id}/mensajes`, fd)
      .subscribe({
        next: (mensajeCreado) => {
          const msg: MensajeHilo = {
            id: mensajeCreado.id,
            texto: mensajeCreado.texto,
            de: 'atleta',
            fecha: new Date(mensajeCreado.fecha),
            adjuntos: mensajeCreado.adjuntos?.map((a: any) => ({
              id: a.id,
              url: a.url,
              tipo: a.tipo.toLowerCase() as 'imagen' | 'video',
              nombre: a.nombre,
            })),
          };

          const hiloActualizado: Hilo = { ...hilo, mensajes: [...hilo.mensajes, msg] };
          this.hilos.update(list => list.map(h => h.id === hilo.id ? hiloActualizado : h));
          this.hiloActivo.set(hiloActualizado);
          
          this.textoRespuesta = '';
          this.respuestaAdjuntos.set([]);
          this.respuestaFiles.clear();
          this.respondiendo.set(false);
        },
        error: (err) => {
          console.error("Error respondiendo:", err);
          this.respondiendo.set(false);
        }
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
}
