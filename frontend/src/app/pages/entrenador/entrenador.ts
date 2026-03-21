import { Component, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

export type TipoTitulacion =
  | 'tafad'
  | 'cafyd_grado'
  | 'cafyd_licenciatura'
  | 'master_rendimiento'
  | 'master_salud'
  | 'otro';

interface ArchivoSubido {
  nombre: string;
  size: string;
  tipo: string;
}

@Component({
  selector: 'app-entrenador-page',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './entrenador.html',
})
export class EntrenadorPage {
  readonly form: FormGroup;

  readonly titulacionOpciones: { value: TipoTitulacion; label: string }[] = [
    { value: 'tafad',               label: 'TAFAD — Técnico Superior en AFAD' },
    { value: 'cafyd_grado',         label: 'Grado en CAFYD' },
    { value: 'cafyd_licenciatura',  label: 'Licenciatura en CAFYD / INEF' },
    { value: 'master_rendimiento',  label: 'Máster en Rendimiento Deportivo' },
    { value: 'master_salud',        label: 'Máster en Actividad Física y Salud' },
    { value: 'otro',                label: 'Otra titulación' },
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

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      nombre:           ['', [Validators.required, Validators.minLength(3)]],
      correo:           ['', [Validators.required, Validators.email]],
      codigoColegiado:  ['', [Validators.required, Validators.pattern(/^[A-Z0-9\-]{4,20}$/i)]],
      titulacion:       ['', Validators.required],
      tituloNutricion:  [false],
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
    if (this.form.invalid || this.archivos().length === 0) return;
    // TODO: enviar datos al backend
    console.log({ ...this.form.value, archivos: this.archivos() });
  }

  fieldError(campo: string): boolean {
    const ctrl = this.form.get(campo);
    return !!(ctrl?.invalid && (ctrl.touched || this.submitted()));
  }
}
