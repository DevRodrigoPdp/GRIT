import { Component, signal, computed, inject, ElementRef, HostListener } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

export type TipoTitulacionEntrenamiento =
  | 'GRADO_CAFYD'
  | 'TSAF_TSEAS'
  | 'CERT_AFDA0210';

export type TipoTitulacionNutricion =
  | 'GRADO_NUTRICION'
  | 'TSD';

interface ArchivoSubido {
  nombre: string;
  size: string;
  tipo: string;
}

@Component({
  selector: 'app-entrenador-page',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, CommonModule],
  templateUrl: './entrenador.html',
})
export class EntrenadorPage {
  private el = inject(ElementRef);
  readonly form: FormGroup;
  readonly mostrarScrollTop = signal(false);

  @HostListener('window:scroll')
  onScroll(): void {
    this.mostrarScrollTop.set(window.scrollY > 300);
  }

  scrollTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  readonly titulacionesEntrenamiento: { value: TipoTitulacionEntrenamiento; label: string }[] = [
    { value: 'GRADO_CAFYD',     label: 'Grado en CAFYD — Ciencias de la Actividad Física y del Deporte' },
    { value: 'TSAF_TSEAS',      label: 'TSAF / TSEAS — Técnico Superior en Animación de Actividades Físicas' },
    { value: 'CERT_AFDA0210',   label: 'Certificado de Profesionalidad AFDA0210' },
  ];

  readonly titulacionesNutricion: { value: TipoTitulacionNutricion; label: string }[] = [
    { value: 'GRADO_NUTRICION', label: 'Grado en Nutrición Humana y Dietética' },
    { value: 'TSD',             label: 'TSD — Técnico Superior en Dietética' },
  ];

  readonly archivos = signal<ArchivoSubido[]>([]);
  readonly dragOver = signal(false);
  readonly submitted = signal(false);

  readonly archivoError = computed(() => {
    if (this.submitted() && this.archivos().length === 0) {
      return 'Adjunta al menos un documento acreditativo';
    }
    return null;
  });

  readonly camposConError = computed(() => {
    if (!this.submitted()) return 0;
    const requeridos = ['nombre', 'correo', 'codigoColegiado'];
    const formErrors = requeridos.filter(k => this.form.get(k)?.invalid).length;
    const archivoErr = this.archivos().length === 0 ? 1 : 0;
    const titulacionErr = this.sinTitulacion() ? 1 : 0;
    return formErrors + archivoErr + titulacionErr;
  });

  // true si el usuario ha intentado enviar y no tiene ninguna titulación seleccionada
  readonly sinTitulacion = computed(() => {
    if (!this.submitted()) return false;
    const ent  = this.form.get('titulacionEntrenamiento')?.value;
    const nutr = this.form.get('titulacionNutricion')?.value;
    return !ent && !nutr;
  });

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      nombre:                    ['', [Validators.required, Validators.minLength(3)]],
      correo:                    ['', [Validators.required, Validators.email]],
      codigoColegiado:           ['', [Validators.required, Validators.pattern(/^[A-Z0-9\-]{4,20}$/i)]],
      titulacionEntrenamiento:   [null],   // opcional — null si es puramente nutricionista
      titulacionNutricion:       [null],   // opcional — null si no tiene
    });
  }

  onDragOver(e: DragEvent): void {
    e.preventDefault();
    this.dragOver.set(true);
  }

  onDragLeave(): void {
    this.dragOver.set(false);
  }

  onDrop(e: DragEvent): void {
    e.preventDefault();
    this.dragOver.set(false);
    if (e.dataTransfer?.files) this.procesarArchivos(e.dataTransfer.files);
  }

  onFileInput(e: Event): void {
    const input = e.target as HTMLInputElement;
    if (input.files) this.procesarArchivos(input.files);
  }

  private procesarArchivos(files: FileList): void {
    const permitidos = ['application/pdf', 'image/jpeg', 'image/png'];
    Array.from(files).forEach(file => {
      if (!permitidos.includes(file.type)) return;
      if (file.size > 10 * 1024 * 1024) return;
      const kb = file.size / 1024;
      const tamaño = kb > 1024 ? `${(kb / 1024).toFixed(1)} MB` : `${kb.toFixed(0)} KB`;
      this.archivos.update(list => [...list, { nombre: file.name, size: tamaño, tipo: file.type }]);
    });
  }

  eliminarArchivo(index: number): void {
    this.archivos.update(list => list.filter((_, i) => i !== index));
  }

  iconoArchivo(tipo: string): string {
    if (tipo === 'application/pdf') return 'PDF';
    if (tipo === 'image/jpeg') return 'JPG';
    return 'PNG';
  }

  onSubmit(): void {
    this.submitted.set(true);
    this.form.markAllAsTouched();

    if (this.form.invalid || this.archivos().length === 0 || this.sinTitulacion()) {
      setTimeout(() => {
        const firstError = this.el.nativeElement.querySelector('.error-field');
        firstError?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 50);
      return;
    }
    // TODO: enviar al backend
    console.log({ ...this.form.value, archivos: this.archivos() });
  }

  fieldError(campo: string): boolean {
    const ctrl = this.form.get(campo);
    return !!(ctrl?.invalid && (ctrl.touched || this.submitted()));
  }
}
