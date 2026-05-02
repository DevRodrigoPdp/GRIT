import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface AlimentoOFF {
  codigo: string;
  nombre: string;
  marca: string;
  categoria?: string;
  kcalPor100g: number;
  proteinasPor100g: number;
  carbsPor100g: number;
  grasasPor100g: number;
}

interface PagedResponse<T> { content: T[]; page: { size: number; number: number; totalElements: number; totalPages: number; }; }
interface ApiResponse<T> { ok: boolean; data: T; }

@Injectable({ providedIn: 'root' })
export class AlimentosService {
  private http = inject(HttpClient);
  private readonly API = '/api/v1/alimentos';

  buscarPorNombre(query: string): Observable<AlimentoOFF[]> {
    return this.http
      .get<ApiResponse<PagedResponse<AlimentoOFF>>>(`${this.API}/search`, {
        params: { q: query.trim(), page: 0 },
        withCredentials: true,
      })
      .pipe(
        map(r => r.data?.content ?? []),
        catchError(() => of([])),
      );
  }
}
