import { Component, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { trigger, transition, style, animate } from '@angular/animations';

export type Role = 'atleta' | 'entrenador' | null;

@Component({
  selector: 'app-role-selector',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './role-selector.html',
  animations: [
    trigger('slideUp', [
      transition(':enter', [
        style({ transform: 'translateY(40px)', opacity: 0 }),
        animate('1100ms cubic-bezier(0.16, 1, 0.3, 1)', style({ transform: 'translateY(0)', opacity: 1 })),
      ]),
      transition(':leave', [
        animate('220ms cubic-bezier(0.4, 0, 1, 1)', style({ transform: 'translateY(16px)', opacity: 0 })),
      ]),
    ]),
  ],
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
