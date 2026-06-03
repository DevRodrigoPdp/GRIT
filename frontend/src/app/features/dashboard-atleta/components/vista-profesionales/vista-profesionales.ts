import { Component, inject, signal, input, output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AtletaService, ProfesionalAsignado } from '../../services/atleta.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ThemeService } from '../../../../core/services/theme.service';

const TITULACION_LABEL: Record<string, string> = {
  GRADO_CAFYD:               'Grado en CAFYD — Ciencias de la Actividad Física y del Deporte',
  TSAF_TSEAS:                'TSAF / TSEAS — Técnico Superior en Animación de Actividades Físicas',
  CERT_AFDA0210:             'Certificado de Profesionalidad AFDA0210',
  GRADO_NUTRICION_DIETETICA: 'Grado en Nutrición Humana y Dietética',
  TSD:                       'TSD — Técnico Superior en Dietética',
};

@Component({
  selector: 'app-vista-profesionales',
  standalone: true,
  imports: [],
  templateUrl: './vista-profesionales.html',
})
export class VistaProfesionalesComponent {
  readonly auth  = inject(AuthService);
  readonly theme = inject(ThemeService);
  private atleta = inject(AtletaService);

  readonly profesionales             = input<ProfesionalAsignado[]>([]);
  readonly profesionalesActualizados = output<ProfesionalAsignado[]>();

  readonly codigoEntrenador = signal('');
  readonly rolConectar      = signal<'ENTRENAMIENTO' | 'NUTRICION'>('ENTRENAMIENTO');
  readonly enviandoCodigo   = signal(false);
  readonly codigoError      = signal('');
  readonly codigoExito      = signal(false);
  readonly desconectandoId        = signal<string | null>(null);
  readonly expandidoId            = signal<string | null>(null);
  readonly pendienteDesconectar   = signal<ProfesionalAsignado | null>(null);

  toggleExpandido(id: string): void {
    this.expandidoId.set(this.expandidoId() === id ? null : id);
  }

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

  iniciales(nombre: string): string {
    return nombre.split(' ').slice(0, 2).map(p => p[0]).join('').toUpperCase() || '?';
  }

  titulacionLabel(raw: string): string {
    return TITULACION_LABEL[raw] ?? raw;
  }

  confirmarDesconexion(): void {
    const prof = this.pendienteDesconectar();
    if (!prof) return;
    this.pendienteDesconectar.set(null);
    this.desconectandoId.set(prof.id);
    this.atleta.desconectarProfesional(prof.id).subscribe({
      next: () => {
        this.desconectandoId.set(null);
        this.atleta.getProfesionalesAsignados().subscribe(p => this.profesionalesActualizados.emit(p));
      },
      error: () => this.desconectandoId.set(null),
    });
  }

  desconectar(id: string): void {
    const prof = this.profesionales().find(p => p.id === id) ?? null;
    this.pendienteDesconectar.set(prof);
  }
}
