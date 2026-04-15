import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard-entrenador',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard-entrenador.html',
})
export class DashboardEntrenadorPage implements OnInit {
  readonly auth = inject(AuthService);

  ngOnInit(): void {
    // Restaura la sesión tras recarga usando la cookie HttpOnly.
    // Si la cookie expiró, AuthService redirige automáticamente a /login.
    this.auth.me().subscribe();
  }
}
