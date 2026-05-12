import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-pendiente',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './pendiente.html',
})
export class PendientePage implements OnInit {
  protected auth = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    this.auth.me().subscribe(() => {
      if (this.auth.estadoRevision() === 'APROBADO') {
        this.router.navigate(['/dashboard/entrenador']);
      }
    });
  }
}
