import { Component, signal, computed, inject, ElementRef, HostListener, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, ValidationErrors, AbstractControl, AsyncValidatorFn } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../core/services/auth.service';
import { EntrenadorService } from '../../../dashboard-entrenador/services/entrenador.service';
import { Observable, map, catchError, of } from 'rxjs';

export type TipoTitulacionEntrenamiento =
  | 'GRADO_CAFYD'
  | 'TSAF_TSEAS'
  | 'CERT_AFDA0210';

export type TipoTitulacionNutricion =
  | 'GRADO_NUTRICION_DIETETICA'
  | 'TSD';

interface ArchivoSubido {
  file: File;
  nombre: string;
  size: string;
  tipo: string;
}

interface InfoCampoNumero {
  label:       string;
  placeholder: string;
  hint:        string;
  obligatorio: boolean;
}

@Component({
  selector: 'app-entrenador-page',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, CommonModule],
  templateUrl: './entrenador.html',
})
export class EntrenadorPage implements OnInit {
  private el = inject(ElementRef);
  private auth = inject(AuthService);
  private entrenadorService = inject(EntrenadorService);
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
    { value: 'GRADO_CAFYD',   label: 'Grado en CAFYD — Ciencias de la Actividad Física y del Deporte' },
    { value: 'TSAF_TSEAS',    label: 'TSAF / TSEAS — Técnico Superior en Animación de Actividades Físicas' },
    { value: 'CERT_AFDA0210', label: 'Certificado de Profesionalidad AFDA0210' },
  ];

  readonly titulacionesNutricion: { value: TipoTitulacionNutricion; label: string }[] = [
    { value: 'GRADO_NUTRICION_DIETETICA', label: 'Grado en Nutrición Humana y Dietética' },
    { value: 'TSD',                       label: 'TSD — Técnico Superior en Dietética' },
  ];

  readonly archivos            = signal<ArchivoSubido[]>([]);
  readonly dragOver            = signal(false);
  readonly submitted           = signal(false);
  readonly showPassword        = signal(false);
  readonly showConfirmPassword = signal(false);
  private readonly _passwordValue = signal('');

  // Signals que reflejan las titulaciones seleccionadas para poder usar computed()
  private readonly _titEnt  = signal<string | null>(null);
  private readonly _titNutr = signal<string | null>(null);

  // true si tiene al menos una titulación universitaria (da acceso a colegio profesional)
  readonly tieneUniversitaria = computed(() =>
    this._titEnt() === 'GRADO_CAFYD' || this._titNutr() === 'GRADO_NUTRICION_DIETETICA'
  );

  // Información dinámica del campo de número profesional según las titulaciones elegidas
  readonly infoCampoNumero = computed((): InfoCampoNumero => {
    const ent  = this._titEnt();
    const nutr = this._titNutr();

    if (ent === 'GRADO_CAFYD' && nutr === 'GRADO_NUTRICION_DIETETICA') {
      return {
        label:       'Número de colegiado',
        placeholder: 'Ej. MAD-12345',
        hint:        'Colegiado en el COLEF y en el Colegio de Dietistas-Nutricionistas de tu comunidad.',
        obligatorio: true,
      };
    }
    if (ent === 'GRADO_CAFYD') {
      return {
        label:       'Número de colegiado',
        placeholder: 'Ej. MAD-12345',
        hint:        'Número de colegiado en el COLEF (Colegio Oficial de Licenciados en Educación Física) de tu comunidad.',
        obligatorio: true,
      };
    }
    if (nutr === 'GRADO_NUTRICION_DIETETICA') {
      return {
        label:       'Número de colegiado',
        placeholder: 'Ej. AND-00123',
        hint:        'Número de colegiado en el Colegio de Dietistas-Nutricionistas de tu comunidad.',
        obligatorio: true,
      };
    }
    // FP / Certificado: no hay colegio profesional, solo registro en algunas CCAA
    return {
      label:       'Número de registro',
      placeholder: 'Ej. CAT-00456',
      hint:        'Número del Registro Oficial de Profesionales del Deporte de tu comunidad, si tu CCAA dispone de él (opcional).',
      obligatorio: false,
    };
  });

  readonly passwordReglas = computed(() => {
    const v = this._passwordValue();
    return [
      { label: 'Mínimo 8 caracteres',     ok: v.length >= 8 },
      { label: 'Una mayúscula',            ok: /[A-Z]/.test(v) },
      { label: 'Una minúscula',            ok: /[a-z]/.test(v) },
      { label: 'Un número',               ok: /\d/.test(v) },
      { label: 'Un carácter especial',    ok: /[^a-zA-Z\d]/.test(v) },
    ];
  });

  readonly archivoError = computed(() => {
    if (this.submitted() && this.archivos().length === 0) {
      return 'Adjunta al menos un documento acreditativo';
    }
    return null;
  });

  readonly camposConError = computed(() => {
    if (!this.submitted()) return 0;
    const campos = ['nombre', 'correo', 'codigoColegiado'];
    const formErrors    = campos.filter(k => this.form.get(k)?.invalid).length;
    const archivoErr    = this.archivos().length === 0 ? 1 : 0;
    const titulacionErr = this.sinTitulacion() ? 1 : 0;
    return formErrors + archivoErr + titulacionErr;
  });

  readonly sinTitulacion = computed(() => {
    if (!this.submitted()) return false;
    return !this._titEnt() && !this._titNutr();
  });

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      nombre:                  ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]+$/)]],
      correo:                  ['', [Validators.required, Validators.email, Validators.pattern(/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/)]],
      password:                ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{8,}$/)]],
      confirmPassword:         ['', Validators.required],
      codigoColegiado:         ['', [Validators.pattern(/^[A-Z0-9\-]{4,20}$/i)], [this.codigoColegiadoValidator()]],
      titulacionEntrenamiento: [null],
      titulacionNutricion:     [null],
    }, { validators: this.passwordMatchValidator });
  }

  private codigoColegiadoValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value || control.value.trim() === '') {
        return of(null);
      }
      return this.entrenadorService.checkCodigoColegiadoExists(control.value).pipe(
        map(exists => (exists ? { codigoExists: true } : null)),
        catchError(() => of(null)) // En caso de error, no mostrar error
      );
    };
  }

  ngOnInit(): void {
    this.form.get('password')!.valueChanges.subscribe(v => this._passwordValue.set(v ?? ''));
  }

  passwordMatchValidator(group: FormGroup): ValidationErrors | null {
    const password        = group.get('password');
    const confirmPassword = group.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ mismatch: true });
      return { mismatch: true };
    }
    return null;
  }

  /**
   * Sincroniza los signals de titulación con el valor del formulario
   * y actualiza el validator de codigoColegiado según corresponda.
   * Llamar desde (change) en ambos selects de titulación.
   */
  onTitulacionChange(): void {
    const ent  = this.form.get('titulacionEntrenamiento')?.value ?? null;
    const nutr = this.form.get('titulacionNutricion')?.value ?? null;
    this._titEnt.set(ent);
    this._titNutr.set(nutr);

    const campo = this.form.get('codigoColegiado')!;
    const esUniversitaria = ent === 'GRADO_CAFYD' || nutr === 'GRADO_NUTRICION_DIETETICA';
    const syncValidators = esUniversitaria
      ? [Validators.required, Validators.pattern(/^[A-Z0-9\-]{4,20}$/i)]
      : [Validators.pattern(/^[A-Z0-9\-]{4,20}$/i)];
    const asyncValidators = esUniversitaria ? [this.codigoColegiadoValidator()] : [];
    campo.setValidators(syncValidators);
    campo.setAsyncValidators(asyncValidators);
    campo.updateValueAndValidity();
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
      const kb     = file.size / 1024;
      const tamaño = kb > 1024 ? `${(kb / 1024).toFixed(1)} MB` : `${kb.toFixed(0)} KB`;
      this.archivos.update(list => [...list, { file, nombre: file.name, size: tamaño, tipo: file.type }]);
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

    const v = this.form.value;
    this.auth.registroEntrenador({
      nombre: v.nombre,
      email: v.correo,
      password: v.password,
      codigoProfesional: v.codigoColegiado || null,
      titulacionEntrenamiento: v.titulacionEntrenamiento || null,
      titulacionNutricion: v.titulacionNutricion || null,
      documentos: this.archivos().map(a => a.file),
    })
    .subscribe({
      error: () => {
        // El servicio maneja el loading; en caso de error la validación del formulario ya está activa.
      },
    });
  }

  fieldError(campo: string): boolean {
    const ctrl = this.form.get(campo);
    return !!(ctrl?.invalid && (ctrl.dirty || this.submitted()));
  }
}
