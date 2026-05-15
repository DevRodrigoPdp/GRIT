import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
  APP_INITIALIZER,
} from '@angular/core';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/services/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideAnimations(),
    provideRouter(routes, withComponentInputBinding(), withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
    
    // CONFIGURACIÓN CORRECTA DEL CLIENTE HTTP
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor]),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',      // Nombre de la cookie que envía tu CsrfCookieFilter 
        headerName: 'X-XSRF-TOKEN',    // Cabecera que espera tu SecurityConfig 
      })
    ),
    
    {
      provide: APP_INITIALIZER,
      useFactory: (auth: AuthService) => () => auth.initSession(),
      deps: [AuthService],
      multi: true,
    },
  ],
};
