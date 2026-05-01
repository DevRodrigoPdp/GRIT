import { Component, signal } from '@angular/core';
import { HeaderComponent } from '../../shared/header/header';
import { HeroComponent } from './components/hero/hero';
import { VerificationComponent } from './components/verification/verification';
import { DashboardComponent } from './components/dashboard/dashboard';
import { PreciosComponent } from './components/precios/precios';
import { FooterComponent } from '../../shared/footer/footer';

type Vista = 'inicio' | 'como-funciona' | 'para-quien' | 'precios';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [HeaderComponent, HeroComponent, VerificationComponent, DashboardComponent, PreciosComponent, FooterComponent],
  template: `
    <app-header [vistaActual]="vistaActual()" (navClick)="irA($any($event))" />
    <main class="pt-16">
      @if (vistaActual() === 'inicio') {
        <app-hero />
      }
      @if (vistaActual() === 'como-funciona') {
        <app-verification />
        <app-dashboard />
      }
      @if (vistaActual() === 'para-quien') {
        <app-dashboard />
      }
      @if (vistaActual() === 'precios') {
        <app-precios />
      }
    </main>
    <app-footer />
  `,
})
export class LandingPage {
  readonly vistaActual = signal<Vista>('inicio');

  irA(vista: Vista): void {
    this.vistaActual.set(vista);
    window.scrollTo({ top: 0, behavior: 'instant' });
  }
}
