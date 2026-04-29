import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-para-profesionales',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './para-profesionales.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ParaProfesionalesPage {}
