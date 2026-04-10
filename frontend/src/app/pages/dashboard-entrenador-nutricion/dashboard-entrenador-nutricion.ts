import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard-entrenador-nutricion',
  standalone: true,
  imports: [],
  templateUrl: './dashboard-entrenador-nutricion.html',
})
export class DashboardEntrenadorNutricionPage {
  readonly auth = inject(AuthService);
}
