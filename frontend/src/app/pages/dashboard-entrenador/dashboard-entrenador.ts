import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard-entrenador',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard-entrenador.html',
})
export class DashboardEntrenadorPage {
  readonly auth = inject(AuthService);
}
