import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { LoginPage } from './login';
import { AuthService } from '../../../../core/services/auth.service';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  let component: LoginPage;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authSpy = {
      login: jasmine.createSpy('login').and.returnValue(of({} as any)),
      loginError: signal<string | null>(null),
      loading: signal(false)
    } as Partial<AuthService> as jasmine.SpyObj<AuthService>;

    await TestBed.configureTestingModule({
      imports: [LoginPage, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call auth.login when form is invalid', () => {
    component.form.setValue({ correo: '', password: '' });
    component.onSubmit();

    expect(authSpy.login).not.toHaveBeenCalled();
    expect(component.form.touched).toBeTrue();
  });

  it('should call auth.login with valid credentials', () => {
    component.form.setValue({ correo: 'test@example.com', password: 'Password1!' });
    component.onSubmit();

    expect(authSpy.login).toHaveBeenCalledWith('test@example.com', 'Password1!');
  });

  it('should clear the login error on input', () => {
    authSpy.loginError.set('credenciales_invalidas');
    component.onInput();

    expect(component.auth.loginError()).toBeNull();
  });
});
