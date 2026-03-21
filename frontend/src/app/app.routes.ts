import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/landing/landing').then(m => m.LandingPage),
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
    path: '**',
    redirectTo: '',
  },
];
