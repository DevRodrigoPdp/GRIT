import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';

interface CachedItem {
  response: HttpResponse<any>;
  timestamp: number;
  ttl: number; // en milisegundos
}

@Injectable({ providedIn: 'root' })
export class CacheService {
  private cache = new Map<string, CachedItem>();
  private maxEntries = 100; // Límite máximo de entradas

  put(url: string, response: HttpResponse<any>, ttl: number = 5 * 60 * 1000): void { // TTL por defecto: 5 minutos
    // Limpiar entradas expiradas antes de agregar
    this.cleanExpired();

    // Si alcanzamos el límite, eliminar la entrada más antigua
    if (this.cache.size >= this.maxEntries) {
      const oldestKey = this.getOldestKey();
      if (oldestKey) this.cache.delete(oldestKey);
    }

    this.cache.set(url, {
      response,
      timestamp: Date.now(),
      ttl
    });
  }

  get(url: string): HttpResponse<any> | undefined {
    const item = this.cache.get(url);
    if (!item) return undefined;

    // Verificar si ha expirado
    if (Date.now() - item.timestamp > item.ttl) {
      this.cache.delete(url);
      return undefined;
    }

    return item.response;
  }

  invalidate(url: string): void {
    this.cache.delete(url);
  }

  // Invalidar por patrón (ej. todas las URLs que contienen '/hilos')
  invalidatePattern(pattern: string): void {
    for (const key of this.cache.keys()) {
      if (key.includes(pattern)) {
        this.cache.delete(key);
      }
    }
  }

  private cleanExpired(): void {
    const now = Date.now();
    for (const [key, item] of this.cache.entries()) {
      if (now - item.timestamp > item.ttl) {
        this.cache.delete(key);
      }
    }
  }

  private getOldestKey(): string | undefined {
    let oldestKey: string | undefined;
    let oldestTime = Date.now();

    for (const [key, item] of this.cache.entries()) {
      if (item.timestamp < oldestTime) {
        oldestTime = item.timestamp;
        oldestKey = key;
      }
    }

    return oldestKey;
  }
}