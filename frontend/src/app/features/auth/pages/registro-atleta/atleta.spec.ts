import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { AtletaPage } from './atleta';
import { AuthService } from '../../../../core/services/auth.service';

describe('AtletaPage', () => {
  let fixture: ComponentFixture<AtletaPage>;
  let component: AtletaPage;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['registroAtleta']) as jasmine.SpyObj<AuthService>;
    authSpy.registroAtleta.and.returnValue(of({ data: { id: '1', rol: 'ATLETA', estado: 'ACTIVO' }, ok: true, message: 'ok' } as any));

    await TestBed.configureTestingModule({
      imports: [AtletaPage, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(AtletaPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should keep registroAtleta from being called when form is invalid', () => {
    component.onSubmit();

    expect(authSpy.registroAtleta).not.toHaveBeenCalled();
    expect(component.submitted()).toBeTrue();
  });

  it('should validate password mismatch with passwordMatchValidator', () => {
    component.form.get('password')?.setValue('Password1!');
    component.form.get('confirmPassword')?.setValue('Different1!');

    const validation = component.passwordMatchValidator(component.form);

    expect(validation).toEqual({ mismatch: true });
    expect(component.form.get('confirmPassword')?.errors).toEqual({ mismatch: true });
  });

  it('should submit a valid atleta registration payload', () => {
    component.form.setValue({
      nombre: 'Test Atleta',
      correo: 'atleta@example.com',
      password: 'Password1!',
      confirmPassword: 'Password1!',
      fechaNac: '2000-01-01',
      genero: 'hombre',
      peso: '70',
      altura: '175',
      deporte: 'Running',
      nivel: 'intermedio',
      objetivo: 'rendimiento',
      servicio: 'entrenamiento',
      alergias: '',
      lesiones: '',
      codigoEntrenador: ''
    });

    component.selectServicio('entrenamiento');
    component.selectObjetivo('rendimiento');
    component.onSubmit();

    expect(authSpy.registroAtleta).toHaveBeenCalled();
    const payload = authSpy.registroAtleta.calls.mostRecent().args[0];
    expect(payload.email).toBe('atleta@example.com');
    expect(payload.nombre).toBe('Test Atleta');
    expect(payload.servicio).toBe('ENTRENAMIENTO');
    expect(payload.objetivo).toBe('RENDIMIENTO');
  });
});
