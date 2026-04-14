import { Component, signal } from '@angular/core';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';

export type Role = 'atleta' | 'entrenador' | null;

@Component({
  selector: 'app-role-selector',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './role-selector.html',
})
export class RoleSelectorComponent {
  readonly hoveredRole = signal<Role>(null);
  readonly selectedRole = signal<Role>(null);

  constructor(private router: Router) {}

  select(role: Role): void {
    this.selectedRole.set(role);
  }

  continuar(): void {
    const role = this.selectedRole();
    if (!role) return;
    this.router.navigate(['registro', role]);
  }
}
