import { Component } from '@angular/core';
import { HeaderComponent } from '../../components/header/header';
import { HeroComponent } from '../../components/hero/hero';
import { ScrollVideoComponent } from '../../components/scroll-video/scroll-video';
import { VerificationComponent } from '../../components/verification/verification';
import { DashboardComponent } from '../../components/dashboard/dashboard';
import { FooterComponent } from '../../components/footer/footer';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [HeaderComponent, HeroComponent, ScrollVideoComponent, VerificationComponent, DashboardComponent, FooterComponent],
  template: `
    <app-header />
    <main>
      <app-hero />
      <app-scroll-video />
      <app-verification />
      <app-dashboard />
    </main>
    <app-footer />
  `,
})
export class LandingPage {}
