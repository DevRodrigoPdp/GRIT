import { Component, inject, signal, input, output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AtletaService, ProfesionalAsignado } from '../../services/atleta.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-vista-profesionales',
  standalone: true,
  imports: [],
  templateUrl: './vista-profesionales.html',
})
export class VistaProfesionalesComponent {
  readonly auth  = inject(AuthService);
  private atleta = inject(AtletaService);

  readonly profesionales             = input<ProfesionalAsignado[]>([]);
  readonly profesionalesActualizados = output<ProfesionalAsignado[]>();

  readonly codigoEntrenador = signal('');
  readonly rolConectar      = signal<'ENTRENAMIENTO' | 'NUTRICION'>('ENTRENAMIENTO');
  readonly enviandoCodigo   = signal(false);
  readonly codigoError      = signal('');
  readonly codigoExito      = signal(false);
  readonly desconectandoId  = signal<string | null>(null);

  conectarConCodigo(): void {
    const codigo = this.codigoEntrenador().trim().toUpperCase();
    if (!codigo) return;
    const servicio = this.auth.servicio();
    const rol: 'ENTRENAMIENTO' | 'NUTRICION' =
      servicio === 'AMBOS' ? this.rolConectar() :
      servicio === 'NUTRICION' ? 'NUTRICION' : 'ENTRENAMIENTO';
    this.enviandoCodigo.set(true);
    this.codigoError.set('');
    this.codigoExito.set(false);
    this.atleta.conectarConEntrenador(codigo, rol).subscribe({
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

  desconectar(id: string): void {
    this.desconectandoId.set(id);
    this.atleta.desconectarProfesional(id).subscribe({
      next: () => {
        this.desconectandoId.set(null);
        this.atleta.getProfesionalesAsignados().subscribe(p => this.profesionalesActualizados.emit(p));
      },
      error: () => this.desconectandoId.set(null),
    });
  }
}
