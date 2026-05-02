import { Component, inject, signal, input, output, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AtletaService, PerfilAtleta, ProfesionalAsignado } from '../../services/atleta.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-vista-perfil-atleta',
  standalone: true,
  imports: [],
  templateUrl: './vista-perfil-atleta.html',
})
export class VistaPerfilAtletaComponent {
  readonly auth  = inject(AuthService);
  private atleta = inject(AtletaService);

  readonly perfilAtleta  = input<PerfilAtleta | null>(null);
  readonly profesionales = input<ProfesionalAsignado[]>([]);

  readonly perfilActualizado         = output<PerfilAtleta>();
  readonly profesionalesActualizados = output<ProfesionalAsignado[]>();

  @ViewChild('fileInputFoto') fileInputFoto?: ElementRef<HTMLInputElement>;

  readonly nuevaAlergia     = signal('');
  readonly nuevaLesion      = signal('');
  readonly codigoEntrenador = signal('');
  readonly enviandoCodigo   = signal(false);
  readonly codigoError      = signal('');
  readonly codigoExito      = signal(false);
  readonly subiendoFoto     = signal(false);

  agregarAlergia(): void {
    const texto = this.nuevaAlergia().trim();
    const p = this.perfilAtleta();
    if (!texto || !p) return;
    this.perfilActualizado.emit({ ...p, alergias: [...p.alergias, texto] });
    this.nuevaAlergia.set('');
  }

  eliminarAlergia(idx: number): void {
    const p = this.perfilAtleta();
    if (!p) return;
    this.perfilActualizado.emit({ ...p, alergias: p.alergias.filter((_, i) => i !== idx) });
  }

  agregarLesion(): void {
    const texto = this.nuevaLesion().trim();
    const p = this.perfilAtleta();
    if (!texto || !p) return;
    this.perfilActualizado.emit({ ...p, lesiones: [...p.lesiones, texto] });
    this.nuevaLesion.set('');
  }

  eliminarLesion(idx: number): void {
    const p = this.perfilAtleta();
    if (!p) return;
    this.perfilActualizado.emit({ ...p, lesiones: p.lesiones.filter((_, i) => i !== idx) });
  }

  seleccionarFotoPerfil(event: Event): void {
    const archivo = (event.target as HTMLInputElement).files?.[0];
    if (!archivo) return;
    this.subiendoFoto.set(true);
    this.atleta.subirFotoPerfil(archivo).subscribe(url => {
      const p = this.perfilAtleta();
      if (p) this.perfilActualizado.emit({ ...p, fotoUrl: url });
      this.subiendoFoto.set(false);
      if (this.fileInputFoto) this.fileInputFoto.nativeElement.value = '';
    });
  }

  conectarConCodigo(): void {
    const codigo = this.codigoEntrenador().trim().toUpperCase();
    if (!codigo) return;
    this.enviandoCodigo.set(true);
    this.codigoError.set('');
    this.codigoExito.set(false);
    this.atleta.conectarConEntrenador(codigo).subscribe({
      next: () => {
        this.codigoExito.set(true);
        this.codigoEntrenador.set('');
        this.enviandoCodigo.set(false);
        this.atleta.getProfesionalesAsignados().subscribe(p => this.profesionalesActualizados.emit(p));
      },
      error: (err: HttpErrorResponse) => {
        this.codigoError.set(
          err.status === 409
            ? 'Ya tienes un profesional asignado para este servicio.'
            : 'Código no válido. Comprueba que lo has introducido correctamente.'
        );
        this.enviandoCodigo.set(false);
      },
    });
  }

  formatFecha(fecha: string): string {
    const [year, month, day] = fecha.split('-');
    const meses = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic'];
    return `${parseInt(day)} ${meses[parseInt(month) - 1]} ${year}`;
  }

  labelNivel(nivel: PerfilAtleta['nivel']): string {
    const map: Record<PerfilAtleta['nivel'], string> = {
      PRINCIPIANTE: 'Principiante', INTERMEDIO: 'Intermedio', AVANZADO: 'Avanzado', ELITE: 'Élite',
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
