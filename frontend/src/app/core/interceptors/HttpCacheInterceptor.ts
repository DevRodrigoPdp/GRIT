import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable, tap, catchError, throwError, of } from 'rxjs';
import { CacheService } from '../services/CacheService.js';

@Injectable()
export class HttpCacheInterceptor implements HttpInterceptor {
  // Configuración de caché por endpoint
  private cacheConfig: { [key: string]: { ttl?: number; exclude?: boolean } } = {
    '/api/v1/comunicacion': { ttl: 10 * 60 * 1000 }, // 10 minutos para comunicación
    '/api/v1/perfil': { exclude: true }, // No cachear perfiles (datos sensibles)
    // Agrega más configuraciones según necesites
  };

  constructor(private cache: CacheService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const config = this.getConfigForUrl(req.url);

    // Invalidar caché en operaciones de escritura
    if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(req.method)) {
      this.invalidateRelatedCache(req.url);
      return next.handle(req);
    }

    // Solo cacheamos peticiones GET que no estén excluidas
    if (req.method !== 'GET' || config.exclude) {
      return next.handle(req);
    }

    const cachedResponse = this.cache.get(req.urlWithParams);

    // Si tenemos ETag guardado, lo añadimos a la cabecera automáticamente
    let headers = req.headers;
    if (cachedResponse && cachedResponse.headers.get('ETag')) {
      headers = headers.set('If-None-Match', cachedResponse.headers.get('ETag')!);
    }

    const authorizedReq = req.clone({ headers });

    return next.handle(authorizedReq).pipe(
      tap(event => {
        if (event instanceof HttpResponse && event.status === 200) {
          const ttl = config.ttl || 5 * 60 * 1000; // TTL por defecto: 5 minutos
          this.cache.put(req.urlWithParams, event, ttl);
        }
      }),
      catchError(error => {
        if (error.status === 304 && cachedResponse) {
          // Si el servidor dice 304, devolvemos lo que ya teníamos
          return of(cachedResponse);
        }
        return throwError(() => error);
      })
    );
  }

  private getConfigForUrl(url: string): { ttl?: number; exclude?: boolean } {
    // Buscar configuración exacta o por patrón
    for (const pattern in this.cacheConfig) {
      if (url.includes(pattern)) {
        return this.cacheConfig[pattern];
      }
    }
    return {}; // Configuración por defecto
  }

  private invalidateRelatedCache(url: string): void {
    // Invalidar patrones relacionados
    if (url.includes('/comunicacion')) {
      this.cache.invalidatePattern('/comunicacion');
    }
    if (url.includes('/entrenamiento')) {
      this.cache.invalidatePattern('/entrenamiento');
    }
    if (url.includes('/nutricion')) {
      this.cache.invalidatePattern('/nutricion');
    }
    // Agrega más patrones según tu API
  }
}