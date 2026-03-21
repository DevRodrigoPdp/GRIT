import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard-atleta',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard-atleta.html',
})
export class DashboardAtletaPage {
  readonly auth = inject(AuthService);
}
