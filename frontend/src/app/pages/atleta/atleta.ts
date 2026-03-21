import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

export type Objetivo = 'rendimiento' | 'masa_muscular' | 'perder_peso' | 'salud' | 'resistencia';
export type Nivel     = 'principiante' | 'intermedio' | 'avanzado' | 'elite';
export type Genero    = 'hombre' | 'mujer' | 'otro' | '';

@Component({
  selector: 'app-atleta-page',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './atleta.html',
})
export class AtletaPage {
  readonly form: FormGroup;
  readonly submitted = signal(false);

  readonly objetivos: { value: Objetivo; label: string; desc: string }[] = [
    { value: 'rendimiento',    label: 'RENDIMIENTO',     desc: 'Maximizar marca y potencia' },
    { value: 'masa_muscular',  label: 'MASA MUSCULAR',   desc: 'Hipertrofia y fuerza' },
    { value: 'perder_peso',    label: 'PERDER PESO',     desc: 'Composición corporal' },
    { value: 'salud',          label: 'SALUD GENERAL',   desc: 'Bienestar y longevidad' },
    { value: 'resistencia',    label: 'RESISTENCIA',     desc: 'Cardio y fondo' },
  ];

  readonly niveles: { value: Nivel; label: string }[] = [
    { value: 'principiante', label: 'PRINCIPIANTE — Menos de 1 año' },
    { value: 'intermedio',   label: 'INTERMEDIO — 1 a 3 años' },
    { value: 'avanzado',     label: 'AVANZADO — 3 a 6 años' },
    { value: 'elite',        label: 'ÉLITE — Más de 6 años / competición' },
  ];

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      nombre:        ['', [Validators.required, Validators.minLength(3)]],
      correo:        ['', [Validators.required, Validators.email]],
      fechaNac:      ['', Validators.required],
      genero:        ['', Validators.required],
      peso:          ['', [Validators.required, Validators.min(30), Validators.max(300)]],
      altura:        ['', [Validators.required, Validators.min(100), Validators.max(250)]],
      deporte:       ['', Validators.required],
      nivel:         ['', Validators.required],
      objetivo:      ['', Validators.required],
    });
  }

  selectObjetivo(value: Objetivo): void {
    this.form.get('objetivo')?.setValue(value);
  }

  fieldError(campo: string): boolean {
    const ctrl = this.form.get(campo);
    return !!(ctrl?.invalid && (ctrl.touched || this.submitted()));
  }

  onSubmit(): void {
    this.submitted.set(true);
    if (this.form.invalid) return;
    console.log(this.form.value);
  }
}
