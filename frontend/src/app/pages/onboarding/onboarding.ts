import { Component } from '@angular/core';
import { RoleSelectorComponent } from '../../components/role-selector/role-selector';

@Component({
  selector: 'app-onboarding-page',
  standalone: true,
  imports: [RoleSelectorComponent],
  template: `<app-role-selector />`,
})
export class OnboardingPage {}
