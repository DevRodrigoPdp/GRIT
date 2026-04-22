import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface AlimentoOFF {
  codigo: string;
  nombre: string;
  marca: string;
  kcalPor100g: number;
  proteinasPor100g: number;
  carbsPor100g: number;
  grasasPor100g: number;
}

interface AlimentosResponse {
  ok: boolean;
  data: AlimentoOFF[];
}

@Injectable({ providedIn: 'root' })
export class AlimentosService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/v1/alimentos';

  buscarPorNombre(query: string): Observable<AlimentoOFF[]> {
    // ── REAL ──────────────────────────────────────────────────────────────
    // const params = new HttpParams().set('q', query);
    // return this.http
    //   .get<AlimentosResponse>(this.API, { params, withCredentials: true })
    //   .pipe(map(r => r.data), catchError(() => of([])));
    // ── MOCK ──────────────────────────────────────────────────────────────
    const q = query.toLowerCase();
    return of(MOCK_ALIMENTOS.filter(a =>
      a.nombre.toLowerCase().includes(q) || a.marca.toLowerCase().includes(q)
    ).slice(0, 15));
  }
}

const MOCK_ALIMENTOS: AlimentoOFF[] = [
  { codigo: '1', nombre: 'Pechuga de pollo', marca: '', kcalPor100g: 165, proteinasPor100g: 31, carbsPor100g: 0, grasasPor100g: 3.6 },
  { codigo: '2', nombre: 'Arroz blanco cocido', marca: '', kcalPor100g: 130, proteinasPor100g: 2.7, carbsPor100g: 28.2, grasasPor100g: 0.3 },
  { codigo: '3', nombre: 'Avena', marca: 'Quaker', kcalPor100g: 366, proteinasPor100g: 13.2, carbsPor100g: 58.7, grasasPor100g: 6.9 },
  { codigo: '4', nombre: 'Huevo entero', marca: '', kcalPor100g: 143, proteinasPor100g: 12.6, carbsPor100g: 0.7, grasasPor100g: 9.5 },
  { codigo: '5', nombre: 'Salmón', marca: '', kcalPor100g: 208, proteinasPor100g: 20, carbsPor100g: 0, grasasPor100g: 13 },
  { codigo: '6', nombre: 'Brócoli', marca: '', kcalPor100g: 34, proteinasPor100g: 2.8, carbsPor100g: 6.6, grasasPor100g: 0.4 },
  { codigo: '7', nombre: 'Plátano', marca: '', kcalPor100g: 89, proteinasPor100g: 1.1, carbsPor100g: 22.8, grasasPor100g: 0.3 },
  { codigo: '8', nombre: 'Yogur griego natural', marca: 'Fage', kcalPor100g: 97, proteinasPor100g: 9, carbsPor100g: 3.6, grasasPor100g: 5 },
  { codigo: '9', nombre: 'Almendras', marca: '', kcalPor100g: 579, proteinasPor100g: 21, carbsPor100g: 21.6, grasasPor100g: 49.9 },
  { codigo: '10', nombre: 'Leche entera', marca: '', kcalPor100g: 61, proteinasPor100g: 3.2, carbsPor100g: 4.8, grasasPor100g: 3.3 },
];
