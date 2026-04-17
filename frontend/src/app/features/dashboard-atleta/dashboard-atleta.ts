import { Component, inject, signal, computed, OnInit, ViewChild, ElementRef } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import {
  AtletaService,
  PerfilAtleta,
  PlanEntrenamiento,
  PlanNutricion,
  SolicitudCheckIn,
  CheckInPeso,
  NotaNutricionista,
  ApunteEntrenador,
  HiloEjercicio,
  Chat,
  ProfesionalAsignado,
  HiloComida,
  MensajeHiloComida,
} from './services/atleta.service';

type Vista = 'entrenamiento' | 'dieta' | 'cuaderno' | 'perfil' | 'ajustes';

interface ChartPoint { x: number; y: number; peso: number; fecha: string; }
interface ItemCompra  { nombre: string; cantidad: string; }

@Component({
  selector: 'app-dashboard-atleta',
  standalone: true,
  templateUrl: './dashboard-atleta.html',
})
export class DashboardAtletaPage implements OnInit {
  readonly auth   = inject(AuthService);
  readonly atleta = inject(AtletaService);

  @ViewChild('hiloRef') hiloRef?: ElementRef<HTMLDivElement>;


  readonly cargando = signal(true);

  // ── Datos ─────────────────────────────────────────────────────────────────
  readonly perfilAtleta      = signal<PerfilAtleta | null>(null);
  readonly profesionales     = signal<ProfesionalAsignado[]>([]);
  readonly planEntrenamiento = signal<PlanEntrenamiento | null>(null);
  readonly planNutricion     = signal<PlanNutricion | null>(null);
  readonly solicitudCheckIn  = signal<SolicitudCheckIn | null>(null);
  readonly historialPesos    = signal<CheckInPeso[]>([]);
  readonly notasNutricionista = signal<NotaNutricionista[]>([]);
  readonly apuntes           = signal<ApunteEntrenador[]>([]);

  // ── Navegación ────────────────────────────────────────────────────────────
  readonly vistaActual = signal<Vista>('entrenamiento');

  readonly navItems = computed(() => {
    const s = this.auth.servicio();
    const items: { id: Vista; label: string }[] = [];
    if (s === 'ENTRENAMIENTO' || s === 'AMBOS') items.push({ id: 'entrenamiento', label: 'ENTRENAMIENTO' });
    if (s === 'NUTRICION'     || s === 'AMBOS') items.push({ id: 'dieta',         label: 'DIETA' });
    items.push({ id: 'cuaderno', label: 'CUADERNO' });
    items.push({ id: 'perfil',   label: 'MI PERFIL' });
    items.push({ id: 'ajustes',  label: 'AJUSTES' });
    return items;
  });

  // ── Registro de peso ──────────────────────────────────────────────────────
  readonly pesoInputValor = signal('');
  readonly enviandoPeso   = signal(false);

  // ── Hilo de ejercicio ─────────────────────────────────────────────────────
  readonly ejercicioActivo   = signal<{ dia: string; nombre: string } | null>(null);
  readonly hilosCache        = signal<Map<string, HiloEjercicio>>(new Map());
  readonly cargandoHilo      = signal(false);
  readonly mensajeHiloInput  = signal('');
  readonly enviandoMensajeHilo = signal(false);

  readonly hiloActivo = computed<HiloEjercicio | null>(() => {
    const ej = this.ejercicioActivo();
    if (!ej) return null;
    return this.hilosCache().get(`${ej.dia}-${ej.nombre}`) ?? null;
  });

  // ── Hilo de comida ────────────────────────────────────────────────────────
  readonly comidaActiva        = signal<string | null>(null);
  readonly hilosComidaCache    = signal<Map<string, HiloComida>>(new Map());
  readonly cargandoHiloComida  = signal(false);
  readonly mensajeHiloComidaInput    = signal('');
  readonly enviandoMensajeHiloComida = signal(false);

  readonly hiloComidaActivo = computed<HiloComida | null>(() => {
    const nombre = this.comidaActiva();
    if (!nombre) return null;
    return this.hilosComidaCache().get(nombre) ?? null;
  });

  // ── Chat general ──────────────────────────────────────────────────────────
  @ViewChild('chatMessagesRef') chatMessagesRef?: ElementRef<HTMLDivElement>;

  readonly chatEntrenador     = signal<Chat | null>(null);
  readonly chatNutricionista  = signal<Chat | null>(null);
  readonly chatActivo         = signal<'entrenador' | 'nutricionista'>('entrenador');
  readonly mensajeChatInput   = signal('');
  readonly enviandoMensajeChat = signal(false);

  readonly chatMostrado = computed<Chat | null>(() =>
    this.chatActivo() === 'entrenador' ? this.chatEntrenador() : this.chatNutricionista()
  );


  // ── Cuaderno (apuntes — pendiente de integración) ─────────────────────────
  readonly apunteRespondiendo = signal<string | null>(null);
  readonly textoRespuesta     = signal('');
  readonly enviandoRespuesta  = signal(false);

  // ── Ajustes ───────────────────────────────────────────────────────────────
  readonly passActual   = signal('');
  readonly passNueva    = signal('');
  readonly passConfirm  = signal('');
  readonly cambiandoPass = signal(false);
  readonly passCambiada  = signal(false);
  readonly passError     = signal('');

  // ── Computeds ─────────────────────────────────────────────────────────────
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

  readonly listaCompra = computed<ItemCompra[]>(() => {
    const plan = this.planNutricion();
    if (!plan) return [];
    const mapa = new Map<string, string>();
    for (const comida of plan.comidas)
      for (const alimento of comida.alimentos)
        if (!mapa.has(alimento.nombre)) mapa.set(alimento.nombre, alimento.cantidad);
    return Array.from(mapa.entries()).map(([nombre, cantidad]) => ({ nombre, cantidad }));
  });

  readonly itemsCompraChecked = signal<string[]>([]);

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.auth.me().subscribe(() => {
      const s = this.auth.servicio();
      const incluyeEntrenamiento = s === 'ENTRENAMIENTO' || s === 'AMBOS';
      const incluyeNutricion     = s === 'NUTRICION'     || s === 'AMBOS';

      if (!incluyeEntrenamiento) this.vistaActual.set('dieta');

      this.atleta.getPerfil().subscribe(p => this.perfilAtleta.set(p));
      this.atleta.getProfesionalesAsignados().subscribe(p => this.profesionales.set(p));
      this.atleta.getApuntes().subscribe(a => this.apuntes.set(a));

      if (incluyeEntrenamiento) {
        this.atleta.getPlanEntrenamiento().subscribe(p => this.planEntrenamiento.set(p));
        this.atleta.getSolicitudCheckIn().subscribe(s => this.solicitudCheckIn.set(s));
        this.atleta.getHistorialPesos().subscribe(h => this.historialPesos.set(h));
        this.atleta.getChat('entrenador').subscribe(c => {
          this.chatEntrenador.set(c);
          this.scrollChatToBottom();
        });
      }

      if (incluyeNutricion) {
        this.atleta.getPlanNutricion().subscribe(p => this.planNutricion.set(p));
        this.atleta.getNotasNutricionista().subscribe(n => this.notasNutricionista.set(n));
        this.atleta.getChat('nutricionista').subscribe(c => this.chatNutricionista.set(c));
      }

      if (!incluyeEntrenamiento && incluyeNutricion) {
        this.chatActivo.set('nutricionista');
      }

      this.cargando.set(false);
    });
  }

  // ── Navegación ────────────────────────────────────────────────────────────
  navegarA(vista: Vista): void {
    this.vistaActual.set(vista);
  }

  // ── Chat general ──────────────────────────────────────────────────────────
  setChatActivo(tipo: 'entrenador' | 'nutricionista'): void {
    this.chatActivo.set(tipo);
    this.scrollChatToBottom();
  }

  enviarMensajeChat(): void {
    const texto = this.mensajeChatInput().trim();
    if (!texto) return;

    const tipo = this.chatActivo();
    this.enviandoMensajeChat.set(true);
    this.atleta.enviarMensajeChat(tipo, texto).subscribe(msg => {
      if (tipo === 'entrenador') {
        this.chatEntrenador.update(c => c ? { ...c, mensajes: [...c.mensajes, msg] } : c);
      } else {
        this.chatNutricionista.update(c => c ? { ...c, mensajes: [...c.mensajes, msg] } : c);
      }
      this.mensajeChatInput.set('');
      this.enviandoMensajeChat.set(false);
      this.scrollChatToBottom();
    });
  }

  private scrollChatToBottom(): void {
    setTimeout(() => {
      const el = this.chatMessagesRef?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 50);
  }

  // ── Hilo de comida ────────────────────────────────────────────────────────
  abrirComida(nombre: string): void {
    if (this.comidaActiva() === nombre) {
      this.comidaActiva.set(null);
      return;
    }
    this.comidaActiva.set(nombre);
    this.mensajeHiloComidaInput.set('');

    if (!this.hilosComidaCache().has(nombre)) {
      this.cargandoHiloComida.set(true);
      this.atleta.getHiloComida(nombre).subscribe(hilo => {
        this.hilosComidaCache.update(m => new Map(m).set(nombre, hilo));
        this.cargandoHiloComida.set(false);
      });
    }
  }

  enviarMensajeHiloComida(): void {
    const texto = this.mensajeHiloComidaInput().trim();
    const nombre = this.comidaActiva();
    if (!texto || !nombre) return;

    this.enviandoMensajeHiloComida.set(true);
    this.atleta.enviarMensajeHiloComida(nombre, texto).subscribe(msg => {
      this.hilosComidaCache.update(m => {
        const hilo = m.get(nombre)!;
        return new Map(m).set(nombre, { ...hilo, mensajes: [...hilo.mensajes, msg] });
      });
      this.mensajeHiloComidaInput.set('');
      this.enviandoMensajeHiloComida.set(false);
    });
  }

  // ── Hilo de ejercicio ─────────────────────────────────────────────────────
  abrirEjercicio(dia: string, nombre: string): void {
    const clave = `${dia}-${nombre}`;
    const actual = this.ejercicioActivo();

    if (actual?.dia === dia && actual?.nombre === nombre) {
      this.ejercicioActivo.set(null);
      return;
    }

    this.ejercicioActivo.set({ dia, nombre });
    this.mensajeHiloInput.set('');

    if (!this.hilosCache().has(clave)) {
      this.cargandoHilo.set(true);
      this.atleta.getHiloEjercicio(dia, nombre).subscribe(hilo => {
        this.hilosCache.update(m => new Map(m).set(clave, hilo));
        this.cargandoHilo.set(false);
      });
    }

    setTimeout(() => {
      this.hiloRef?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 50);
  }

  enviarMensajeHilo(): void {
    const texto = this.mensajeHiloInput().trim();
    const ej = this.ejercicioActivo();
    if (!texto || !ej) return;

    this.enviandoMensajeHilo.set(true);
    this.atleta.enviarMensajeHilo(ej.dia, ej.nombre, texto).subscribe(msg => {
      const clave = `${ej.dia}-${ej.nombre}`;
      this.hilosCache.update(m => {
        const hilo = m.get(clave)!;
        return new Map(m).set(clave, { ...hilo, mensajes: [...hilo.mensajes, msg] });
      });
      this.mensajeHiloInput.set('');
      this.enviandoMensajeHilo.set(false);
    });
  }

  adjuntarMediaHilo(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    const ej = this.ejercicioActivo();
    if (!archivo || !ej) return;

    this.atleta.subirMediaHilo(ej.dia, ej.nombre, archivo).subscribe(media => {
      const clave = `${ej.dia}-${ej.nombre}`;
      this.hilosCache.update(m => {
        const hilo = m.get(clave)!;
        return new Map(m).set(clave, { ...hilo, media: [...hilo.media, media] });
      });
      input.value = '';
    });
  }

  // ── Registro de peso ──────────────────────────────────────────────────────
  registrarPeso(): void {
    const valor = parseFloat(this.pesoInputValor().replace(',', '.'));
    const solicitud = this.solicitudCheckIn();
    if (!solicitud || isNaN(valor) || valor < 30 || valor > 300) return;

    this.enviandoPeso.set(true);
    this.atleta.registrarPeso(solicitud.id, valor).subscribe(entrada => {
      this.historialPesos.update(h => [...h, entrada].sort((a, b) => a.fecha.localeCompare(b.fecha)));
      this.solicitudCheckIn.set(null);
      this.pesoInputValor.set('');
      this.enviandoPeso.set(false);
    });
  }

  // ── Cuaderno ──────────────────────────────────────────────────────────────
  abrirRespuesta(apunteId: string): void {
    this.apunteRespondiendo.set(apunteId);
    this.textoRespuesta.set('');
  }

  cerrarRespuesta(): void {
    this.apunteRespondiendo.set(null);
    this.textoRespuesta.set('');
  }

  enviarRespuesta(apunteId: string): void {
    const texto = this.textoRespuesta().trim();
    if (!texto) return;

    this.enviandoRespuesta.set(true);
    this.atleta.responderApunte(apunteId, texto).subscribe(respuesta => {
      this.apuntes.update(lista =>
        lista.map(a => a.id !== apunteId ? a : { ...a, respuestas: [...a.respuestas, respuesta] })
      );
      this.cerrarRespuesta();
      this.enviandoRespuesta.set(false);
    });
  }

  // ── Ajustes ───────────────────────────────────────────────────────────────
  cambiarPassword(): void {
    this.passError.set('');
    if (this.passNueva() !== this.passConfirm()) {
      this.passError.set('Las contraseñas nuevas no coinciden.');
      return;
    }
    if (this.passNueva().length < 8) {
      this.passError.set('La contraseña debe tener al menos 8 caracteres.');
      return;
    }

    this.cambiandoPass.set(true);
    this.atleta.cambiarPassword(this.passActual(), this.passNueva()).subscribe({
      next: () => {
        this.passCambiada.set(true);
        this.passActual.set('');
        this.passNueva.set('');
        this.passConfirm.set('');
        this.cambiandoPass.set(false);
      },
      error: () => {
        this.passError.set('Contraseña actual incorrecta.');
        this.cambiandoPass.set(false);
      },
    });
  }

  // ── Lista de la compra ────────────────────────────────────────────────────
  toggleItemCompra(nombre: string): void {
    this.itemsCompraChecked.update(items =>
      items.includes(nombre) ? items.filter(i => i !== nombre) : [...items, nombre]
    );
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  totalKcalComida(alimentos: PlanNutricion['comidas'][0]['alimentos']): number {
    return alimentos.reduce((sum, a) => sum + (a.kcal ?? 0), 0);
  }

  formatFecha(fecha: string): string {
    const [year, month, day] = fecha.split('-');
    const meses = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];
    return `${parseInt(day)} ${meses[parseInt(month) - 1]} ${year}`;
  }

  pesoInputValido(): boolean {
    const v = parseFloat(this.pesoInputValor().replace(',', '.'));
    return !isNaN(v) && v >= 30 && v <= 300;
  }

  labelNivel(nivel: PerfilAtleta['nivel']): string {
    const map: Record<PerfilAtleta['nivel'], string> = {
      PRINCIPIANTE: 'Principiante', INTERMEDIO: 'Intermedio',
      AVANZADO: 'Avanzado', ELITE: 'Élite',
    };
    return map[nivel];
  }

  labelObjetivo(obj: PerfilAtleta['objetivo']): string {
    if (!obj) return '—';
    const map: Record<NonNullable<PerfilAtleta['objetivo']>, string> = {
      RENDIMIENTO: 'Rendimiento deportivo', MASA_MUSCULAR: 'Ganancia muscular',
      PERDER_PESO: 'Pérdida de peso', SALUD: 'Salud general', RESISTENCIA: 'Resistencia',
    };
    return map[obj];
  }

  labelGenero(genero: PerfilAtleta['genero']): string {
    return { HOMBRE: 'Hombre', MUJER: 'Mujer', OTRO: 'Otro' }[genero];
  }
}
