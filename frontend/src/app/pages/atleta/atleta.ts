import { Component, signal, computed, inject, ElementRef, HostListener } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, ValidationErrors } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

export type Objetivo = 'rendimiento' | 'masa_muscular' | 'perder_peso' | 'salud' | 'resistencia';
export type Nivel     = 'principiante' | 'intermedio' | 'avanzado' | 'elite';
export type Genero    = 'hombre' | 'mujer' | 'otro' | '';
export type Servicio  = 'entrenamiento' | 'nutricion' | 'ambos';

@Component({
  selector: 'app-atleta-page',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, CommonModule],
  templateUrl: './atleta.html',
})
export class AtletaPage {
  private el = inject(ElementRef);
  private auth = inject(AuthService);
  readonly form: FormGroup;
  readonly submitted = signal(false);
  readonly mostrarScrollTop = signal(false);
  readonly registroError = signal<string | null>(null);
  readonly loading = signal(false);

  @HostListener('window:scroll')
  onScroll(): void {
    this.mostrarScrollTop.set(window.scrollY > 300);
  }

  scrollTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  readonly objetivos: { value: Objetivo; label: string; desc: string }[] = [
    { value: 'rendimiento',    label: 'RENDIMIENTO',     desc: 'Maximizar marca y potencia' },
    { value: 'masa_muscular',  label: 'MASA MUSCULAR',   desc: 'Hipertrofia y fuerza' },
    { value: 'perder_peso',    label: 'PERDER PESO',     desc: 'Composición corporal' },
    { value: 'salud',          label: 'SALUD GENERAL',   desc: 'Bienestar y longevidad' },
    { value: 'resistencia',    label: 'RESISTENCIA',     desc: 'Cardio y fondo' },
  ];

  readonly servicios: { value: Servicio; label: string; desc: string }[] = [
    { value: 'entrenamiento', label: 'ENTRENAMIENTO',        desc: 'Planes de entreno personalizados' },
    { value: 'nutricion',     label: 'NUTRICIÓN',            desc: 'Dieta y seguimiento nutricional' },
    { value: 'ambos',         label: 'ENTRENAMIENTO + NUTRICIÓN', desc: 'Servicio completo' },
  ];

  readonly niveles: { value: Nivel; label: string }[] = [
    { value: 'principiante', label: 'PRINCIPIANTE — Menos de 1 año' },
    { value: 'intermedio',   label: 'INTERMEDIO — 1 a 3 años' },
    { value: 'avanzado',     label: 'AVANZADO — 3 a 6 años' },
    { value: 'elite',        label: 'ÉLITE — Más de 6 años / competición' },
  ];

  readonly camposConError = computed(() => {
    if (!this.submitted()) return 0;
    return Object.keys(this.form.controls).filter(k => this.form.get(k)?.invalid).length;
  });

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      nombre:    ['', [Validators.required, Validators.minLength(3)]],
      correo:    ['', [Validators.required, Validators.email]],
      password:  ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
      fechaNac:  ['', Validators.required],
      genero:    ['', Validators.required],
      peso:      ['', [Validators.required, Validators.min(30), Validators.max(300)]],
      altura:    ['', [Validators.required, Validators.min(100), Validators.max(250)]],
      deporte:   ['', [Validators.required, Validators.minLength(3)]],
      nivel:     ['', Validators.required],
      objetivo:  ['', Validators.required],
      servicio:  ['', Validators.required],
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup): ValidationErrors | null {
    const password = group.get('password');
    const confirmPassword = group.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ mismatch: true });
      return { mismatch: true };
    }
    return null;
  }

  selectObjetivo(value: Objetivo): void {
    this.form.get('objetivo')?.setValue(value);
  }

  get mostrarObjetivo(): boolean {
    const s = this.form.get('servicio')?.value;
    return s === 'entrenamiento' || s === 'ambos';
  }

  selectServicio(value: Servicio): void {
    this.form.get('servicio')?.setValue(value);
    const objetivo = this.form.get('objetivo')!;
    if (value === 'nutricion') {
      objetivo.clearValidators();
      objetivo.setValue('');
    } else {
      objetivo.setValidators(Validators.required);
    }
    objetivo.updateValueAndValidity();
  }

  fieldError(campo: string): boolean {
    const ctrl = this.form.get(campo);
    return !!(ctrl?.invalid && (ctrl.touched || this.submitted()));
  }

  onSubmit(): void {
    this.submitted.set(true);
    this.form.markAllAsTouched();
    this.registroError.set(null);

    if (this.form.invalid) {
      setTimeout(() => {
        const firstError = this.el.nativeElement.querySelector('.error-field');
        firstError?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 50);
      return;
    }

    this.loading.set(true);
    const v = this.form.value;
    this.auth.registroAtleta({
      nombre:   v.nombre,
      email:    v.correo,
      password: v.password,
      fechaNac: v.fechaNac,
      genero:   v.genero,
      pesoKg:   v.peso,
      alturaCm: v.altura,
      deporte:  v.deporte,
      nivel:    (v.nivel as string).toUpperCase(),
      servicio: (v.servicio as string).toUpperCase(),
      objetivo: v.objetivo ? (v.objetivo as string).toUpperCase() : null,
    }).subscribe({
      next: () => {
        this.loading.set(false);
        // Redirección ya manejada en auth.service.ts
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 409) {
          this.registroError.set('El correo electrónico ya está registrado.');
        } else if (err.status === 400) {
          this.registroError.set('Datos inválidos. Revisa los campos.');
        } else {
          this.registroError.set('Error al registrar. Inténtalo de nuevo.');
        }
      },
    });
  }
}
