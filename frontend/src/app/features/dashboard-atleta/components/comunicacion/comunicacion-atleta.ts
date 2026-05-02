import { Component, inject, input, output, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AtletaService, SolicitudCheckIn } from '../../services/atleta.service';

export type CategoriaHilo = 'tecnica' | 'duda' | 'apunte';

export interface Adjunto {
  id:     string;
  url:    string;
  tipo:   'imagen' | 'video';
  nombre: string;
}

export interface MensajeHilo {
  id:        string;
  texto:     string;
  de:        'entrenador' | 'atleta';
  fecha:     Date;
  adjuntos?: Adjunto[];
}

export interface Hilo {
  id:           string;
  titulo:       string;
  categoria:    CategoriaHilo;
  de:           'entrenador' | 'atleta';
  fechaAbierto: Date;
  mensajes:     MensajeHilo[];
  leido:        boolean;
}

type Vista = 'lista' | 'detalle' | 'nuevo';

@Component({
  selector:    'app-comunicacion-atleta',
  standalone:  true,
  imports:     [FormsModule],
  templateUrl: './comunicacion-atleta.html',
})
export class ComunicacionAtletaComponent implements OnInit {
  private atleta = inject(AtletaService);

  readonly atletaId          = input.required<string>();
  readonly contexto          = input<'ENTRENAMIENTO' | 'NUTRICION'>('ENTRENAMIENTO');
  readonly profesionalNombre = input<string>('Tu profesional');
  readonly solicitudCheckIn  = input<SolicitudCheckIn | null>(null);
  readonly checkInCompletado = output<void>();

  // ── Check-in de peso ──────────────────────────────────────────────────────
  readonly pesoInput      = signal('');
  readonly enviandoPeso   = signal(false);
  readonly checkInHecho   = signal(false);

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
  readonly vista      = signal<Vista>('lista');
  readonly hiloActivo = signal<Hilo | null>(null);
  readonly hilos      = signal<Hilo[]>([]);

  // ── Formulario nuevo hilo ─────────────────────────────────────────────────
  nuevoTitulo    = '';
  nuevaCategoria = signal<CategoriaHilo>('apunte');
  nuevoTexto     = '';
  readonly nuevoAdjuntos = signal<Adjunto[]>([]);

  // ── Formulario respuesta ──────────────────────────────────────────────────
  textoRespuesta = '';
  readonly respuestaAdjuntos = signal<Adjunto[]>([]);

  readonly categorias: { value: CategoriaHilo; label: string }[] = [
    { value: 'tecnica', label: 'TÉCNICA' },
    { value: 'duda',    label: 'DUDA'    },
    { value: 'apunte',  label: 'APUNTE'  },
  ];

  busqueda        = '';
  filtroCategoria = signal<CategoriaHilo | 'TODOS'>('TODOS');

  readonly hilosNoLeidos = computed(() => this.hilos().filter(h => !h.leido).length);
  readonly ultimoMensaje = (hilo: Hilo): MensajeHilo => hilo.mensajes[hilo.mensajes.length - 1];

  readonly hilosFiltrados = computed(() => {
    const q      = this.busqueda.trim().toLowerCase();
    const filtro = this.filtroCategoria();
    return this.hilos().filter(h => {
      const coincideCategoria = filtro === 'TODOS' || h.categoria === filtro;
      const coincideBusqueda  = !q ||
        h.titulo.toLowerCase().includes(q) ||
        h.mensajes.some(m => m.texto.toLowerCase().includes(q));
      return coincideCategoria && coincideBusqueda;
    });
  });

  ngOnInit(): void {
    // ── Descomentar para cargar hilos desde la API ──────────────────────────
    // const id  = this.atletaId();
    // const ctx = this.contexto();
    // this.http.get<{ ok: boolean; data: any[] }>(
    //   `${this.API}/hilos?atletaId=${id}&contexto=${ctx}`
    // ).subscribe(r => this.hilos.set(r.data.map((h: any) => ({
    //   id:           h.id,
    //   titulo:       h.titulo,
    //   categoria:    h.categoria.toLowerCase() as CategoriaHilo,
    //   de:           h.creadoPor === 'ATLETA' ? 'atleta' : 'entrenador',
    //   fechaAbierto: new Date(h.fechaAbierto),
    //   leido:        h.leidoPorMi,
    //   mensajes:     [],
    // }))));
  }

  // ── Multimedia ────────────────────────────────────────────────────────────

  adjuntarArchivos(event: Event, destino: 'nuevo' | 'respuesta'): void {
    const files = (event.target as HTMLInputElement).files;
    if (!files) return;
    Array.from(files).forEach(file => {
      const tipo: 'imagen' | 'video' = file.type.startsWith('video/') ? 'video' : 'imagen';
      const adjunto: Adjunto = {
        id:     crypto.randomUUID(),
        url:    URL.createObjectURL(file),
        tipo,
        nombre: file.name,
      };
      if (destino === 'nuevo') {
        this.nuevoAdjuntos.update(l => [...l, adjunto]);
      } else {
        this.respuestaAdjuntos.update(l => [...l, adjunto]);
      }
    });
    (event.target as HTMLInputElement).value = '';
  }

  quitarAdjunto(id: string, destino: 'nuevo' | 'respuesta'): void {
    if (destino === 'nuevo') {
      this.nuevoAdjuntos.update(l => l.filter(a => a.id !== id));
    } else {
      this.respuestaAdjuntos.update(l => l.filter(a => a.id !== id));
    }
  }

  // ── Navegación ────────────────────────────────────────────────────────────

  abrirHilo(hilo: Hilo): void {
    this.hilos.update(list => list.map(h => h.id === hilo.id ? { ...h, leido: true } : h));
    this.hiloActivo.set(this.hilos().find(h => h.id === hilo.id) ?? hilo);
    this.textoRespuesta = '';
    this.respuestaAdjuntos.set([]);
    this.vista.set('detalle');

    // ── Descomentar para cargar mensajes completos desde la API ────────────
    // this.http.get<{ ok: boolean; data: any }>(`${this.API}/hilos/${hilo.id}`)
    //   .subscribe(r => {
    //     const h = r.data;
    //     const hiloCompleto: Hilo = {
    //       id: h.id, titulo: h.titulo,
    //       categoria: h.categoria.toLowerCase() as CategoriaHilo,
    //       de: h.creadoPor === 'ATLETA' ? 'atleta' : 'entrenador',
    //       fechaAbierto: new Date(h.fechaAbierto), leido: true,
    //       mensajes: h.mensajes.map((m: any) => ({
    //         id: m.id, texto: m.texto ?? '',
    //         de: m.de === 'ATLETA' ? 'atleta' : 'entrenador',
    //         fecha: new Date(m.fecha),
    //         adjuntos: m.adjuntos?.map((a: any) => ({
    //           id: a.id, url: a.url,
    //           tipo: a.tipo.toLowerCase() as 'imagen' | 'video',
    //           nombre: a.nombre,
    //         })),
    //       })),
    //     };
    //     this.hilos.update(list => list.map(x => x.id === hilo.id ? hiloCompleto : x));
    //     this.hiloActivo.set(hiloCompleto);
    //   });
  }

  volverALista(): void {
    this.hiloActivo.set(null);
    this.vista.set('lista');
  }

  // ── Acciones ──────────────────────────────────────────────────────────────

  crearHilo(): void {
    const titulo = this.nuevoTitulo.trim();
    const texto  = this.nuevoTexto.trim();
    if (!titulo || !texto) return;

    const hilo: Hilo = {
      id:           crypto.randomUUID(),
      titulo,
      categoria:    this.nuevaCategoria(),
      de:           'atleta',
      fechaAbierto: new Date(),
      leido:        true,
      mensajes: [{
        id:       crypto.randomUUID(),
        texto,
        de:       'atleta',
        fecha:    new Date(),
        adjuntos: this.nuevoAdjuntos().length ? [...this.nuevoAdjuntos()] : undefined,
      }],
    };
    this.hilos.update(list => [hilo, ...list]);
    this.nuevoTitulo = '';
    this.nuevoTexto  = '';
    this.nuevaCategoria.set('apunte');
    this.nuevoAdjuntos.set([]);
    this.vista.set('lista');

    // ── Descomentar para persistir en la API ──────────────────────────────
    // const fd = new FormData();
    // fd.append('atletaId',  this.atletaId());
    // fd.append('titulo',    titulo);
    // fd.append('categoria', this.nuevaCategoria().toUpperCase());
    // fd.append('contexto',  this.contexto());
    // fd.append('texto',     texto);
    // this.http.post<{ ok: boolean; data: { id: string; fechaAbierto: string } }>(
    //   `${this.API}/hilos`, fd
    // ).subscribe(r => {
    //   this.hilos.update(list => list.map(h =>
    //     h.id === hilo.id ? { ...h, id: r.data.id, fechaAbierto: new Date(r.data.fechaAbierto) } : h
    //   ));
    // });
  }

  responder(): void {
    const texto = this.textoRespuesta.trim();
    const hilo  = this.hiloActivo();
    if ((!texto && !this.respuestaAdjuntos().length) || !hilo) return;

    const msg: MensajeHilo = {
      id:       crypto.randomUUID(),
      texto,
      de:       'atleta',
      fecha:    new Date(),
      adjuntos: this.respuestaAdjuntos().length ? [...this.respuestaAdjuntos()] : undefined,
    };
    const hiloActualizado: Hilo = { ...hilo, mensajes: [...hilo.mensajes, msg] };
    this.hilos.update(list => list.map(h => h.id === hilo.id ? hiloActualizado : h));
    this.hiloActivo.set(hiloActualizado);
    this.textoRespuesta = '';
    this.respuestaAdjuntos.set([]);

    // ── Descomentar para persistir en la API ──────────────────────────────
    // const fd = new FormData();
    // if (texto) fd.append('texto', texto);
    // this.http.post<{ ok: boolean; data: { id: string; fecha: string } }>(
    //   `${this.API}/hilos/${hilo.id}/mensajes`, fd
    // ).subscribe(r => {
    //   this.hilos.update(list => list.map(h => {
    //     if (h.id !== hilo.id) return h;
    //     return { ...h, mensajes: h.mensajes.map(m =>
    //       m.id === msg.id ? { ...m, id: r.data.id, fecha: new Date(r.data.fecha) } : m
    //     )};
    //   }));
    //   const actualizado = this.hilos().find(h => h.id === hilo.id)!;
    //   this.hiloActivo.set(actualizado);
    // });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  labelCategoria(cat: CategoriaHilo): string {
    return { tecnica: 'TÉCNICA', duda: 'DUDA', apunte: 'APUNTE' }[cat];
  }

  categoriaEstilo(cat: CategoriaHilo): { borde: string; texto: string; fondo: string } {
    const map: Record<CategoriaHilo, { borde: string; texto: string; fondo: string }> = {
      tecnica: { borde: '#2ED38D', texto: '#2ED38D', fondo: 'rgba(46,211,141,0.06)'  },
      duda:    { borde: '#F97316', texto: '#F97316', fondo: 'rgba(249,115,22,0.06)'  },
      apunte:  { borde: '#000000', texto: '#000000', fondo: 'rgba(0,0,0,0.02)'       },
    };
    return map[cat];
  }

  formatearFecha(fecha: Date): string {
    const hoy  = new Date();
    const ayer = new Date(hoy); ayer.setDate(hoy.getDate() - 1);
    const hora = fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
    if (fecha.toDateString() === hoy.toDateString())  return `Hoy · ${hora}`;
    if (fecha.toDateString() === ayer.toDateString()) return `Ayer · ${hora}`;
    return fecha.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }) + ` · ${hora}`;
  }
}
