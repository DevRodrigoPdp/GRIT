import { Component, inject, input, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SeguimientoService, CheckInPeso, MediaAdjunto } from '../../services/seguimiento.service';

export interface Mensaje {
  id:     string;
  texto?: string;
  de:     'entrenador' | 'atleta';
  fecha:  Date;
  media?: MediaAdjunto[];
}

interface ChartPoint { x: number; y: number; peso: number; fecha: string; }

@Component({
  selector:    'app-comunicacion',
  standalone:  true,
  imports:     [FormsModule],
  templateUrl: './comunicacion.html',
})
export class ComunicacionComponent implements OnInit {
  private seg = inject(SeguimientoService);

  readonly atletaId     = input.required<string>();
  readonly atletaNombre = input.required<string>();
  /** 'ENTRENAMIENTO' | 'NUTRICION' | 'AMBOS' */
  readonly servicio     = input<string>('');

  // ── Chat ──────────────────────────────────────────────────────────────────
  mensajes = signal<Mensaje[]>([
    { id: '1', texto: '¡Hola! He revisado tu última sesión, muy buen trabajo en los ejercicios de fuerza.', de: 'entrenador', fecha: new Date(Date.now() - 1000 * 60 * 60 * 2) },
    { id: '2', texto: 'Gracias entrenador, aunque la última serie me costó bastante.',                       de: 'atleta',     fecha: new Date(Date.now() - 1000 * 60 * 45)       },
    { id: '3', texto: 'Normal, era el objetivo. Esta semana bajamos la carga un 10% y trabajamos técnica.', de: 'entrenador', fecha: new Date(Date.now() - 1000 * 60 * 30)       },
  ]);
  nuevoMensaje = '';

  // ── Seguimiento de peso ───────────────────────────────────────────────────
  historialPesos     = signal<CheckInPeso[]>([]);
  checkInPendiente   = signal(false);
  solicitando        = signal(false);

  readonly chartData = computed<{ points: ChartPoint[]; polyline: string } | null>(() => {
    const pesos = this.historialPesos();
    if (pesos.length < 2) return null;
    const W = 460, H = 60, padX = 20, padY = 8;
    const weights = pesos.map(p => p.pesoKg);
    const minW    = Math.min(...weights) - 1;
    const maxW    = Math.max(...weights) + 1;
    const toX     = (i: number) => padX + (i / (pesos.length - 1)) * (W - 2 * padX);
    const toY     = (w: number) => padY + H - ((w - minW) / (maxW - minW)) * H;
    const points: ChartPoint[] = pesos.map((p, i) => ({
      x: toX(i), y: toY(p.pesoKg), peso: p.pesoKg, fecha: p.fecha,
    }));
    return { points, polyline: points.map(p => `${p.x},${p.y}`).join(' ') };
  });

  ngOnInit(): void {
    const id = this.atletaId();
    this.seg.getHistorialPesos(id).subscribe(h => this.historialPesos.set(h));
    this.seg.tieneCheckInPendiente(id).subscribe(b => this.checkInPendiente.set(b));
  }

  // ── Chat ──────────────────────────────────────────────────────────────────

  enviar(): void {
    const texto = this.nuevoMensaje.trim();
    if (!texto) return;
    this.mensajes.update(msgs => [
      ...msgs,
      { id: crypto.randomUUID(), texto, de: 'entrenador', fecha: new Date() },
    ]);
    this.nuevoMensaje = '';
  }

  adjuntarMedia(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;

    this.seg.subirMedia(archivo).subscribe(media => {
      this.mensajes.update(msgs => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          de: 'entrenador',
          fecha: new Date(),
          media: [media]
        }
      ]);
      // Reset input
      input.value = '';
    });
  }

  formatearHora(fecha: Date): string {
    return fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
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
}
