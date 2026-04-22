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
  Chat,
  ProfesionalAsignado,
  AdjuntoChat,
} from './services/atleta.service';

type Vista = 'entrenamiento' | 'dieta' | 'cuaderno' | 'perfil' | 'ajustes';

interface ChartPoint { x: number; y: number; peso: number; fecha: string; }
interface ItemCompra { nombre: string; cantidad: string; }

@Component({
  selector: 'app-dashboard-atleta',
  standalone: true,
  templateUrl: './dashboard-atleta.html',
})
export class DashboardAtletaPage implements OnInit {
  readonly auth = inject(AuthService);
  readonly atleta = inject(AtletaService);

  readonly cargando = signal(true);

  // ── Datos ─────────────────────────────────────────────────────────────────
  readonly perfilAtleta = signal<PerfilAtleta | null>(null);
  readonly profesionales = signal<ProfesionalAsignado[]>([]);
  readonly planEntrenamiento = signal<PlanEntrenamiento | null>(null);
  readonly planNutricion = signal<PlanNutricion | null>(null);
  readonly solicitudCheckIn = signal<SolicitudCheckIn | null>(null);
  readonly historialPesos = signal<CheckInPeso[]>([]);
  readonly notasNutricionista = signal<NotaNutricionista[]>([]);

  // ── Navegación ────────────────────────────────────────────────────────────
  readonly vistaActual = signal<Vista>('entrenamiento');

  readonly navItems = computed(() => {
    const s = this.auth.servicio();
    const items: { id: Vista; label: string }[] = [];
    if (s === 'ENTRENAMIENTO' || s === 'AMBOS') items.push({ id: 'entrenamiento', label: 'ENTRENAMIENTO' });
    if (s === 'NUTRICION' || s === 'AMBOS') items.push({ id: 'dieta', label: 'DIETA' });
    items.push({ id: 'cuaderno', label: 'CHAT' });
    items.push({ id: 'perfil', label: 'MI PERFIL' });
    items.push({ id: 'ajustes', label: 'AJUSTES' });
    return items;
  });

  // ── Registro de peso ──────────────────────────────────────────────────────
  readonly pesoInputValor = signal('');
  readonly enviandoPeso = signal(false);


  // ── Comida expandida ─────────────────────────────────────────────────────
  readonly comidaActiva = signal<string | null>(null);

  // ── Chat general ──────────────────────────────────────────────────────────
  @ViewChild('chatMessagesRef') chatMessagesRef?: ElementRef<HTMLDivElement>;
  @ViewChild('fileInputChat') fileInputChat?: ElementRef<HTMLInputElement>;
  @ViewChild('fileInputFoto') fileInputFoto?: ElementRef<HTMLInputElement>;

  readonly subiendoFoto = signal(false);

  readonly chatEntrenador = signal<Chat | null>(null);
  readonly chatNutricionista = signal<Chat | null>(null);
  readonly chatActivo = signal<'entrenador' | 'nutricionista'>('entrenador');
  readonly mensajeChatInput = signal('');
  readonly enviandoMensajeChat = signal(false);
  readonly adjuntoPendiente = signal<AdjuntoChat | null>(null);
  readonly subiendoAdjunto = signal(false);

  readonly chatMostrado = computed<Chat | null>(() =>
    this.chatActivo() === 'entrenador' ? this.chatEntrenador() : this.chatNutricionista()
  );



  // ── Alergias / lesiones ───────────────────────────────────────────────────
  readonly nuevaAlergia       = signal('');
  readonly nuevaLesion        = signal('');
  readonly codigoEntrenador   = signal('');
  readonly enviandoCodigo     = signal(false);
  readonly codigoError        = signal('');
  readonly codigoExito        = signal(false);

  conectarConCodigo(): void {
    const codigo = this.codigoEntrenador().trim().toUpperCase();
    if (!codigo) return;
    this.enviandoCodigo.set(true);
    this.codigoError.set('');
    this.codigoExito.set(false);
    // TODO: conectar con POST /api/v1/atleta/conectar { codigo }
    setTimeout(() => {
      this.enviandoCodigo.set(false);
      this.codigoError.set('Código no válido o ya utilizado.');
    }, 800);
  }

  agregarAlergia(): void {
    const texto = this.nuevaAlergia().trim();
    if (!texto) return;
    const p = this.perfilAtleta();
    if (!p) return;
    this.perfilAtleta.set({ ...p, alergias: [...p.alergias, texto] });
    this.nuevaAlergia.set('');
  }

  eliminarAlergia(idx: number): void {
    const p = this.perfilAtleta();
    if (!p) return;
    this.perfilAtleta.set({ ...p, alergias: p.alergias.filter((_, i) => i !== idx) });
  }

  agregarLesion(): void {
    const texto = this.nuevaLesion().trim();
    if (!texto) return;
    const p = this.perfilAtleta();
    if (!p) return;
    this.perfilAtleta.set({ ...p, lesiones: [...p.lesiones, texto] });
    this.nuevaLesion.set('');
  }

  eliminarLesion(idx: number): void {
    const p = this.perfilAtleta();
    if (!p) return;
    this.perfilAtleta.set({ ...p, lesiones: p.lesiones.filter((_, i) => i !== idx) });
  }

  // ── Ajustes ───────────────────────────────────────────────────────────────
  readonly passAbierto = signal(false);
  readonly passActual = signal('');
  readonly passNueva = signal('');
  readonly passConfirm = signal('');
  readonly cambiandoPass = signal(false);
  readonly passCambiada = signal(false);
  readonly passError = signal('');

  readonly bajaAbierta = signal(false);
  readonly confirmarBaja = signal(false);
  readonly textoConfirmaBaja = signal('');
  readonly eliminandoCuenta = signal(false);

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
      const incluyeNutricion = s === 'NUTRICION' || s === 'AMBOS';

      if (!incluyeEntrenamiento) this.vistaActual.set('dieta');

      this.atleta.getPerfil().subscribe(p => this.perfilAtleta.set(p));
      this.atleta.getProfesionalesAsignados().subscribe(p => this.profesionales.set(p));

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
    this.adjuntoPendiente.set(null);
    this.scrollChatToBottom();
  }

  seleccionarArchivoChat(event: Event): void {
    const archivo = (event.target as HTMLInputElement).files?.[0];
    if (!archivo) return;
    this.subiendoAdjunto.set(true);
    this.atleta.subirArchivoChat(this.chatActivo(), archivo).subscribe(adjunto => {
      this.adjuntoPendiente.set(adjunto);
      this.subiendoAdjunto.set(false);
      if (this.fileInputChat) this.fileInputChat.nativeElement.value = '';
    });
  }

  quitarAdjunto(): void {
    this.adjuntoPendiente.set(null);
    if (this.fileInputChat) this.fileInputChat.nativeElement.value = '';
  }

  enviarMensajeChat(): void {
    const texto = this.mensajeChatInput().trim();
    const adjunto = this.adjuntoPendiente();
    if (!texto && !adjunto) return;

    const tipo = this.chatActivo();
    this.enviandoMensajeChat.set(true);
    this.atleta.enviarMensajeChat(tipo, texto, adjunto ?? undefined).subscribe(msg => {
      if (tipo === 'entrenador') {
        this.chatEntrenador.update(c => c ? { ...c, mensajes: [...c.mensajes, msg] } : c);
      } else {
        this.chatNutricionista.update(c => c ? { ...c, mensajes: [...c.mensajes, msg] } : c);
      }
      this.mensajeChatInput.set('');
      this.adjuntoPendiente.set(null);
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

  // ── Ejercicio activo (mostrar notas) ─────────────────────────────────────
  readonly ejercicioActivo = signal<{ sesion: string; nombre: string } | null>(null);

  toggleEjercicio(sesion: string, nombre: string): void {
    const actual = this.ejercicioActivo();
    this.ejercicioActivo.set(actual?.sesion === sesion && actual?.nombre === nombre ? null : { sesion, nombre });
  }

  // ── Comida expandida ─────────────────────────────────────────────────────
  abrirComida(nombre: string): void {
    this.comidaActiva.set(this.comidaActiva() === nombre ? null : nombre);
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

  seleccionarFotoPerfil(event: Event): void {
    const archivo = (event.target as HTMLInputElement).files?.[0];
    if (!archivo) return;
    this.subiendoFoto.set(true);
    this.atleta.subirFotoPerfil(archivo).subscribe(url => {
      this.perfilAtleta.update(p => p ? { ...p, fotoUrl: url } : p);
      this.subiendoFoto.set(false);
      if (this.fileInputFoto) this.fileInputFoto.nativeElement.value = '';
    });
  }

  eliminarCuenta(): void {
    if (this.textoConfirmaBaja() !== 'ELIMINAR') return;
    this.eliminandoCuenta.set(true);
    this.atleta.eliminarCuenta().subscribe({
      next: () => {
        this.eliminandoCuenta.set(false);
        this.auth.logout();
      },
      error: () => this.eliminandoCuenta.set(false),
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
