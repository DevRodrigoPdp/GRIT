import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard-entrenador-nutricion',
  standalone: true,
  imports: [],
  templateUrl: './dashboard-entrenador-nutricion.html',
})
export class DashboardEntrenadorNutricionPage implements OnInit {
  readonly auth = inject(AuthService);

  ngOnInit(): void {
    this.auth.me().subscribe();
  }
}
