import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-pendiente',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './pendiente.html',
})
export class PendientePage {
  protected auth = inject(AuthService);
  // TODO: cuando haya backend, cargar aquí el rejection_reason desde la sesión
  // o desde GET /api/v1/auth/me para mostrárselo al entrenador rechazado
}
