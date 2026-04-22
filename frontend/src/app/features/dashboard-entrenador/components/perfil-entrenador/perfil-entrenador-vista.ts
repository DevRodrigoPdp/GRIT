import { Component, inject, computed, output, input, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { EntrenadorService, PerfilEntrenador, TitulacionEntrenamiento, TitulacionNutricion } from '../../services/entrenador.service';

const TITULACION_ENT_LABEL: Record<string, string> = {
  GRADO_CAFYD:   'Grado en CAFYD — Ciencias de la Actividad Física y del Deporte',
  TSAF_TSEAS:    'TSAF / TSEAS — Técnico Superior en Animación de Actividades Físicas',
  CERT_AFDA0210: 'Certificado de Profesionalidad AFDA0210',
};

const TITULACION_NUTR_LABEL: Record<string, string> = {
  GRADO_NUTRICION_DIETETICA: 'Grado en Nutrición Humana y Dietética',
  TSD:                       'TSD — Técnico Superior en Dietética',
};

interface ArchivoSubido { file: File; nombre: string; size: string; }

@Component({
  selector: 'app-perfil-entrenador-vista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './perfil-entrenador-vista.html',
})
export class PerfilEntrenadorVistaComponent {
  readonly auth    = inject(AuthService);
  private entSvc   = inject(EntrenadorService);
  readonly volver  = output<void>();
  readonly perfil  = input<PerfilEntrenador | null>(null);

  readonly nombre = computed(() => this.perfil()?.nombre ?? this.auth.nombre() ?? '');

  readonly iniciales = computed(() => {
    return this.nombre()
      .split(' ')
      .slice(0, 2)
      .map(p => p[0])
      .join('')
      .toUpperCase() || '?';
  });

  readonly titulacionEntLabel = computed(() => {
    const t = this.perfil()?.titulacionEntrenamiento;
    return t ? TITULACION_ENT_LABEL[t] ?? t : null;
  });

  readonly titulacionNutrLabel = computed(() => {
    const t = this.perfil()?.titulacionNutricion;
    return t ? TITULACION_NUTR_LABEL[t] ?? t : null;
  });

  readonly experiencia = computed(() => this.perfil()?.experienciaAnos ?? null);
  readonly descripcion = computed(() => this.perfil()?.descripcion ?? null);
  readonly correo      = computed(() => this.perfil()?.correo ?? null);

  // ── Ampliación de formación ──────────────────────────────────────────────

  readonly puedeAmpliarNutricion     = computed(() => !!this.auth.tituloEntrenamiento() && !this.auth.tituloNutricion());
  readonly puedeAmpliarEntrenamiento = computed(() => !!this.auth.tituloNutricion() && !this.auth.tituloEntrenamiento());
  readonly puedeAmpliar              = computed(() => this.puedeAmpliarNutricion() || this.puedeAmpliarEntrenamiento());

  readonly moduloAAmpliar = computed<'NUTRICION' | 'ENTRENAMIENTO' | null>(() => {
    if (this.puedeAmpliarNutricion()) return 'NUTRICION';
    if (this.puedeAmpliarEntrenamiento()) return 'ENTRENAMIENTO';
    return null;
  });

  readonly titulacionesDisponibles = computed<{ value: string; label: string }[]>(() => {
    if (this.puedeAmpliarNutricion()) {
      return Object.entries(TITULACION_NUTR_LABEL).map(([value, label]) => ({ value, label }));
    }
    return Object.entries(TITULACION_ENT_LABEL).map(([value, label]) => ({ value, label }));
  });

  // ── Invitar atleta ─────────────────────────────────────────────────────────
  // ── Foto de perfil ──────────────────────────────────────────────────────
  @ViewChild('fileInputFoto') fileInputFoto?: ElementRef<HTMLInputElement>;
  readonly subiendoFoto = signal(false);
  readonly fotoUrl      = computed(() => this.perfil()?.fotoUrl ?? null);

  seleccionarFoto(event: Event): void {
    const archivo = (event.target as HTMLInputElement).files?.[0];
    if (!archivo) return;
    this.subiendoFoto.set(true);
    this.entSvc.subirFotoPerfil(archivo).subscribe(url => {
      // Actualizar localmente mientras llega la respuesta real del servidor
      const p = this.perfil();
      if (p) (p as any).fotoUrl = url;
      this._fotoLocal.set(url);
      this.subiendoFoto.set(false);
      if (this.fileInputFoto) this.fileInputFoto.nativeElement.value = '';
    });
  }

  readonly _fotoLocal = signal<string | null>(null);
  readonly fotoMostrada = computed(() => this._fotoLocal() ?? this.fotoUrl());

  // ── Invitar atleta ─────────────────────────────────────────────────────────
  readonly emailInvitacion    = signal('');
  readonly enviandoInvitacion = signal(false);
  readonly invitacionEnviada  = signal(false);
  readonly codigoCopiado      = signal(false);

  copiarCodigo(): void {
    const codigo = this.perfil()?.codigoInvitacion;
    if (!codigo) return;
    navigator.clipboard.writeText(codigo).then(() => {
      this.codigoCopiado.set(true);
      setTimeout(() => this.codigoCopiado.set(false), 2000);
    }).catch(() => {});
  }

  enviarInvitacion(): void {
    const email = this.emailInvitacion().trim();
    if (!email || this.enviandoInvitacion()) return;
    this.enviandoInvitacion.set(true);
    this.entSvc.invitarAtleta(email).subscribe(() => {
      this.enviandoInvitacion.set(false);
      this.invitacionEnviada.set(true);
      this.emailInvitacion.set('');
      setTimeout(() => this.invitacionEnviada.set(false), 4000);
    });
  }

  // ── Ampliación de formación ──────────────────────────────────────────────
  readonly expandiendo          = signal(false);
  readonly titulacionAmpliacion = signal('');
  readonly archivosAmpliacion   = signal<ArchivoSubido[]>([]);
  readonly dragOverAmpliacion   = signal(false);
  readonly estadoSolicitud      = signal<'idle' | 'enviando' | 'enviada' | 'error'>('idle');
  readonly solicitudPendiente   = computed(() =>
    this.perfil()?.solicitudAmpliacionPendiente === this.moduloAAmpliar()
  );

  toggleExpanding(): void {
    this.expandiendo.update(v => !v);
    if (!this.expandiendo()) {
      this.titulacionAmpliacion.set('');
      this.archivosAmpliacion.set([]);
      this.estadoSolicitud.set('idle');
    }
  }

  onDragOverAmp(e: DragEvent): void { e.preventDefault(); this.dragOverAmpliacion.set(true); }
  onDragLeaveAmp(): void            { this.dragOverAmpliacion.set(false); }
  onDropAmp(e: DragEvent): void {
    e.preventDefault();
    this.dragOverAmpliacion.set(false);
    if (e.dataTransfer?.files) this.procesarArchivos(e.dataTransfer.files);
  }
  onFileInputAmp(e: Event): void {
    const input = e.target as HTMLInputElement;
    if (input.files) this.procesarArchivos(input.files);
    (e.target as HTMLInputElement).value = '';
  }

  private procesarArchivos(files: FileList): void {
    const permitidos = ['application/pdf', 'image/jpeg', 'image/png'];
    Array.from(files).forEach(file => {
      if (!permitidos.includes(file.type)) return;
      if (file.size > 10 * 1024 * 1024) return;
      const kb   = file.size / 1024;
      const size = kb > 1024 ? `${(kb / 1024).toFixed(1)} MB` : `${kb.toFixed(0)} KB`;
      this.archivosAmpliacion.update(list => [...list, { file, nombre: file.name, size }]);
    });
  }

  eliminarArchivoAmp(idx: number): void {
    this.archivosAmpliacion.update(list => list.filter((_, i) => i !== idx));
  }

  readonly puedeEnviar = computed(() =>
    !!this.titulacionAmpliacion() && this.archivosAmpliacion().length > 0
  );

  enviarSolicitudAmpliacion(): void {
    const modulo    = this.moduloAAmpliar();
    const titulacion = this.titulacionAmpliacion();
    if (!modulo || !titulacion || this.archivosAmpliacion().length === 0) return;

    this.estadoSolicitud.set('enviando');
    this.entSvc.solicitarAmpliacionFormacion(
      modulo, titulacion, this.archivosAmpliacion().map(a => a.file)
    ).subscribe({
      next: () => this.estadoSolicitud.set('enviada'),
      error: () => this.estadoSolicitud.set('error'),
    });
  }
}
