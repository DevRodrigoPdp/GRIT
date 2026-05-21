import { Component, inject, signal, input, output } from '@angular/core';
import { AtletaService, PlanEntrenamiento, SolicitudCheckIn, CheckInPeso } from '../../services/atleta.service';

@Component({
  selector: 'app-vista-entrenamiento',
  standalone: true,
  imports: [],
  templateUrl: './vista-entrenamiento.html',
})
export class VistaEntrenamientoComponent {
  private atleta = inject(AtletaService);

  readonly plan             = input<PlanEntrenamiento | null>(null);
  readonly historialPesos   = input<CheckInPeso[]>([]);
  readonly solicitudCheckIn = input<SolicitudCheckIn | null>(null);

  readonly pesoRegistrado = output<CheckInPeso>();

  readonly ejercicioActivo = signal<{ sesion: string; nombre: string } | null>(null);
  readonly pesoInputValor  = signal('');
  readonly enviandoPeso    = signal(false);

  readonly hoyFormateado = (() => {
    const d = new Date();
    const dias   = ['DOM','LUN','MAR','MIÉ','JUE','VIE','SÁB'];
    const meses  = ['ENE','FEB','MAR','ABR','MAY','JUN','JUL','AGO','SEP','OCT','NOV','DIC'];
    return `${dias[d.getDay()]} · ${d.getDate()} ${meses[d.getMonth()]}`;
  })();


  toggleEjercicio(sesion: string, nombre: string): void {
    const actual = this.ejercicioActivo();
    this.ejercicioActivo.set(actual?.sesion === sesion && actual?.nombre === nombre ? null : { sesion, nombre });
  }

  pesoInputValido(): boolean {
    const v = Number(this.pesoInputValor());
    return !isNaN(v) && v >= 30 && v <= 300;
  }

  registrarPeso(): void {
    const valor = Number(this.pesoInputValor());
    const solicitud = this.solicitudCheckIn();
    if (!solicitud || isNaN(valor) || valor < 30 || valor > 300) return;
    this.enviandoPeso.set(true);
    this.atleta.registrarPeso(solicitud.id, valor).subscribe(entrada => {
      this.pesoRegistrado.emit(entrada);
      this.pesoInputValor.set('');
      this.enviandoPeso.set(false);
    });
  }

  formatFecha(fecha: string): string {
    const [year, month, day] = fecha.split('-');
    const meses = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic'];
    return `${parseInt(day)} ${meses[parseInt(month) - 1]} ${year}`;
  }
}
