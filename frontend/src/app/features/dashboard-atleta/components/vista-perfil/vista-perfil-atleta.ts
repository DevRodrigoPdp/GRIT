import { Component, inject, signal, input, output, ViewChild, ElementRef } from '@angular/core';
import { AtletaService, PerfilAtleta } from '../../services/atleta.service';
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

  readonly perfilAtleta      = input<PerfilAtleta | null>(null);
  readonly perfilActualizado = output<PerfilAtleta>();

  @ViewChild('fileInputFoto') fileInputFoto?: ElementRef<HTMLInputElement>;

  readonly subiendoFoto = signal(false);

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
