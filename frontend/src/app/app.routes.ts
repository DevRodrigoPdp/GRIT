import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/landing/landing').then(m => m.LandingPage),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then(m => m.LoginPage),
  },
  {
    path: 'pendiente',
    loadComponent: () =>
      import('./pages/pendiente/pendiente').then(m => m.PendientePage),
  },
  {
    path: 'empezar',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/onboarding/onboarding').then(m => m.OnboardingPage),
      },
      {
        path: 'entrenador',
        loadComponent: () =>
          import('./pages/entrenador/entrenador').then(m => m.EntrenadorPage),
      },
      {
        path: 'atleta',
        loadComponent: () =>
          import('./pages/atleta/atleta').then(m => m.AtletaPage),
      },
    ],
  },
  {
    path: 'dashboard',
    children: [
      {
        path: 'atleta',
        loadComponent: () =>
          import('./pages/dashboard-atleta/dashboard-atleta').then(m => m.DashboardAtletaPage),
      },
      {
        path: 'entrenador',
        loadComponent: () =>
          import('./pages/dashboard-entrenador/dashboard-entrenador').then(m => m.DashboardEntrenadorPage),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
