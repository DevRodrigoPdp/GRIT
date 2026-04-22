import { Component, signal, computed, inject, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, ValidationErrors } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService, ServicioAtleta } from '../../../../core/services/auth.service';
import { AtletaService } from '../../../dashboard-atleta/services/atleta.service';

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
export class AtletaPage implements OnInit {
  private el   = inject(ElementRef);
  private auth  = inject(AuthService);
  private atleta = inject(AtletaService);
  readonly form: FormGroup;

  @ViewChild('inputFoto') inputFoto?: ElementRef<HTMLInputElement>;
  readonly fotoFile    = signal<File | null>(null);
  readonly fotoPreview = signal<string | null>(null);

  seleccionarFoto(event: Event): void {
    const archivo = (event.target as HTMLInputElement).files?.[0];
    if (!archivo) return;
    this.fotoFile.set(archivo);
    this.fotoPreview.set(URL.createObjectURL(archivo));
    (event.target as HTMLInputElement).value = '';
  }
  readonly submitted        = signal(false);
  readonly showPassword     = signal(false);
  readonly showConfirmPass  = signal(false);
  readonly mostrarScrollTop = signal(false);
  readonly registroError = signal<string | null>(null);
  readonly loading = signal(false);
  private readonly _passwordValue = signal('');

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

  readonly passwordReglas = computed(() => {
    const v = this._passwordValue();
    return [
      { label: 'Mínimo 8 caracteres',  ok: v.length >= 8 },
      { label: 'Una mayúscula',         ok: /[A-Z]/.test(v) },
      { label: 'Una minúscula',         ok: /[a-z]/.test(v) },
      { label: 'Un número',            ok: /\d/.test(v) },
      { label: 'Un carácter especial', ok: /[^a-zA-Z\d]/.test(v) },
    ];
  });

  readonly camposConError = computed(() => {
    if (!this.submitted()) return 0;
    return Object.keys(this.form.controls).filter(k => this.form.get(k)?.invalid).length;
  });

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      nombre:    ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]+$/)]],
      correo:    ['', [Validators.required, Validators.email, Validators.maxLength(100), Validators.pattern(/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/)]],
      password:  ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{8,}$/)]],
      confirmPassword: ['', Validators.required],
      fechaNac:  ['', [Validators.required, this.fechaNacValidator]],
      genero:    ['', Validators.required],
      peso:      ['', [Validators.required, Validators.min(40), Validators.max(300), Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
      altura:    ['', [Validators.required, Validators.min(130), Validators.max(240), Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
      deporte:   ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]+$/)]],
      nivel:     ['', Validators.required],
      objetivo:  ['', Validators.required],
      servicio:  ['', Validators.required],
      alergias:       [''],
      lesiones:       [''],
      codigoEntrenador: [''],
    }, { validators: [this.passwordMatchValidator, this.imcValidator] });
  }

  ngOnInit(): void {
    this.form.get('password')!.valueChanges.subscribe(v => this._passwordValue.set(v ?? ''));
  }

  private fechaNacValidator(control: any): ValidationErrors | null {
    if (!control.value) return null;
    const fecha = new Date(control.value);
    if (isNaN(fecha.getTime())) return { invalidDate: true };
    const hoy = new Date();
    if (fecha > hoy) return { futureDate: true };
    const edad = hoy.getFullYear() - fecha.getFullYear();
    const mes = hoy.getMonth() - fecha.getMonth();
    if (mes < 0 || (mes === 0 && hoy.getDate() < fecha.getDate())) {
      if (edad - 1 < 13) return { tooYoung: true };
      if (edad - 1 > 120) return { tooOld: true };
    } else {
      if (edad < 13) return { tooYoung: true };
      if (edad > 120) return { tooOld: true };
    }
    return null;
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

  imcValidator(group: FormGroup): ValidationErrors | null {
    const peso = group.get('peso');
    const altura = group.get('altura');
    if (peso && altura && peso.value && altura.value) {
      const p = parseFloat(peso.value);
      const a = parseFloat(altura.value) / 100; // en metros
      if (!isNaN(p) && !isNaN(a) && a > 0) {
        const imc = p / (a * a);
        if (imc < 15 || imc > 40) {
          return { imcInvalid: true };
        }
      }
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

  get mostrarAlergias(): boolean {
    const s = this.form.get('servicio')?.value;
    return s === 'nutricion' || s === 'ambos';
  }

  get mostrarLesiones(): boolean {
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
    return !!(ctrl?.invalid && (ctrl.dirty || this.submitted()));
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
      servicio: (v.servicio as Servicio).toUpperCase() as ServicioAtleta,
      objetivo: v.objetivo ? (v.objetivo as string).toUpperCase() : null,
    }).subscribe({
      next: () => {
        const foto = this.fotoFile();
        if (foto) {
          this.atleta.subirFotoPerfil(foto).subscribe();
        }
        this.loading.set(false);
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
