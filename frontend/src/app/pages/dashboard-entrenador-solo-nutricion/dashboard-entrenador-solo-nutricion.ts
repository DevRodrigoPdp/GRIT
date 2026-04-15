import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard-entrenador-solo-nutricion',
  standalone: true,
  imports: [],
  templateUrl: './dashboard-entrenador-solo-nutricion.html',
})
export class DashboardEntrenadorSoloNutricionPage implements OnInit {
  readonly auth = inject(AuthService);

  ngOnInit(): void {
    this.auth.me().subscribe();
  }
}
