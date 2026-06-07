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
        style({ maxHeight: '0', opacity: 0, transform: 'translateY(12px)', overflow: 'hidden' }),
        animate('400ms ease-out', style({ maxHeight: '120px', opacity: 1, transform: 'translateY(0)' })),
      ]),
      transition(':leave', [
        style({ overflow: 'hidden' }),
        animate('300ms ease-in', style({ maxHeight: '0', opacity: 0, transform: 'translateY(8px)' })),
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
