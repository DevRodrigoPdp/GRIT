import { Routes } from '@angular/router';
import { rolGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/landing/landing').then(m => m.LandingPage),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login/login').then(m => m.LoginPage),
  },
  {
    path: 'pendiente',
    canActivate: [rolGuard('ENTRENADOR')],
    loadComponent: () =>
      import('./features/auth/pages/pendiente/pendiente').then(m => m.PendientePage),
  },
  {
    path: 'registro',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/auth/pages/onboarding/onboarding').then(m => m.OnboardingPage),
      },
      {
        path: 'entrenador',
        loadComponent: () =>
          import('./features/auth/pages/registro-entrenador/entrenador').then(m => m.EntrenadorPage),
      },
      {
        path: 'atleta',
        loadComponent: () =>
          import('./features/auth/pages/registro-atleta/atleta').then(m => m.AtletaPage),
      },
    ],
  },
  {
    path: 'dashboard',
    children: [
      {
        path: 'atleta',
        canActivate: [rolGuard('ATLETA')],
        loadComponent: () =>
          import('./features/dashboard-atleta/dashboard-atleta').then(m => m.DashboardAtletaPage),
      },
      {
        path: 'entrenador',
        canActivate: [rolGuard('ENTRENADOR')],
        loadComponent: () =>
          import('./features/dashboard-entrenador/dashboard-entrenador').then(m => m.DashboardEntrenadorPage),
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [rolGuard('ADMIN')],
    loadComponent: () =>
      import('./features/admin/admin').then(m => m.AdminPage),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
