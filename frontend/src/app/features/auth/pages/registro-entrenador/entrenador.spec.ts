import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { EntrenadorPage } from './entrenador';
import { AuthService } from '../../../../core/services/auth.service';

describe('EntrenadorPage', () => {
  let fixture: ComponentFixture<EntrenadorPage>;
  let component: EntrenadorPage;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['registroEntrenador']) as jasmine.SpyObj<AuthService>;
    authSpy.registroEntrenador.and.returnValue(of({ data: { id: '1', rol: 'ENTRENADOR', estado: 'PENDIENTE_REVISION' }, ok: true, message: 'ok' } as any));

    await TestBed.configureTestingModule({
      imports: [EntrenadorPage, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(EntrenadorPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should manage master list correctly', () => {
    component.masterPersonalizado.set('Coach avanzado');
    component.agregarMasterPersonalizado();

    expect(component.mastersSeleccionados().has('Coach avanzado')).toBeTrue();
    expect(component.masterPersonalizado()).toBe('');

    component.eliminarMaster('Coach avanzado');

    expect(component.mastersSeleccionados().has('Coach avanzado')).toBeFalse();
  });

  it('should not call registroEntrenador when form is invalid', () => {
    component.onSubmit();

    expect(authSpy.registroEntrenador).not.toHaveBeenCalled();
    expect(component.submitted()).toBeTrue();
  });

  it('should submit a valid entrenador registration payload', () => {
    component.form.setValue({
      nombre: 'Test Entrenador',
      correo: 'coach@example.com',
      password: 'Password1!',
      confirmPassword: 'Password1!',
      codigoColegiado: 'CAT-00456',
      titulacionEntrenamiento: 'GRADO_CAFYD',
      titulacionNutricion: null,
      anosExperiencia: '5',
      sobreMi: 'Soy entrenador'
    });
    component.onTitulacionChange();
    component.archivos.set([{
      file: new File(['dummy'], 'cert.pdf', { type: 'application/pdf' }),
      nombre: 'cert.pdf',
      size: '1 KB',
      tipo: 'application/pdf'
    }]);
    component.onSubmit();

    expect(authSpy.registroEntrenador).toHaveBeenCalled();
    const payload = authSpy.registroEntrenador.calls.mostRecent().args[0];
    expect(payload.email).toBe('coach@example.com');
    expect(payload.nombre).toBe('Test Entrenador');
    expect(payload.codigoProfesional).toBe('CAT-00456');
  });
});
