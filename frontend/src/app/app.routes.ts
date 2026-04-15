import { Routes } from '@angular/router';
import { rolGuard } from './guards/auth.guard';

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
    canActivate: [rolGuard('ENTRENADOR')],
    loadComponent: () =>
      import('./pages/pendiente/pendiente').then(m => m.PendientePage),
  },
  {
    path: 'registro',
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
        canActivate: [rolGuard('ATLETA')],
        loadComponent: () =>
          import('./pages/dashboard-atleta/dashboard-atleta').then(m => m.DashboardAtletaPage),
      },
      {
        path: 'entrenador',
        canActivate: [rolGuard('ENTRENADOR')],
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./pages/dashboard-entrenador/dashboard-entrenador').then(m => m.DashboardEntrenadorPage),
          },
          {
            path: 'nutricion',
            loadComponent: () =>
              import('./pages/dashboard-entrenador-nutricion/dashboard-entrenador-nutricion').then(m => m.DashboardEntrenadorNutricionPage),
          },
          {
            path: 'solo-nutricion',
            loadComponent: () =>
              import('./pages/dashboard-entrenador-solo-nutricion/dashboard-entrenador-solo-nutricion').then(m => m.DashboardEntrenadorSoloNutricionPage),
          },
        ],
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
