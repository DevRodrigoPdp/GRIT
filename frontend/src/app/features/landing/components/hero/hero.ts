import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './hero.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeroComponent {
  readonly dias = [
    { label: 'L', num: '28', hecho: true,  hoy: false },
    { label: 'M', num: '29', hecho: true,  hoy: false },
    { label: 'X', num: '30', hecho: true,  hoy: false },
    { label: 'J', num: '31', hecho: false, hoy: true  },
    { label: 'V', num: '1',  hecho: false, hoy: false },
    { label: 'S', num: '2',  hecho: false, hoy: false },
    { label: 'D', num: '3',  hecho: false, hoy: false },
  ];

  readonly ejercicios = [
    { nombre: 'Press de banca',    series: 4, reps: '8' },
    { nombre: 'Press inclinado',   series: 3, reps: '10' },
    { nombre: 'Remo con barra',    series: 4, reps: '8' },
    { nombre: 'Dominadas',         series: 3, reps: '6' },
  ];

  readonly macros = [
    { label: 'Prot',  pct: '72%',  valor: '182 g' },
    { label: 'Carbs', pct: '58%',  valor: '290 g' },
    { label: 'Grasa', pct: '40%',  valor: '64 g'  },
  ];
}
