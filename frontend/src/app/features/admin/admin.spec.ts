import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { signal } from '@angular/core';
import { AdminPage } from './admin';
import { AdminService } from './services/admin.service';
import { AuthService } from '../../core/services/auth.service';

describe('AdminPage', () => {
  let fixture: ComponentFixture<AdminPage>;
  let component: AdminPage;
  let adminSpy: jasmine.SpyObj<AdminService>;
  let authSpy: Partial<AuthService>;

  beforeEach(async () => {
    adminSpy = jasmine.createSpyObj('AdminService', ['getPendingTrainers', 'buscarUsuarios', 'processReview', 'eliminarUsuario', 'buscarEntrenadores']) as jasmine.SpyObj<AdminService>;
    adminSpy.getPendingTrainers.and.returnValue(of({ content: [], totalPages: 0 } as any));
    adminSpy.buscarUsuarios.and.returnValue(of({ usuarios: [], totalPages: 0 } as any));
    adminSpy.processReview.and.returnValue(of(void 0));
    adminSpy.eliminarUsuario.and.returnValue(of(void 0));

    authSpy = {
      rol: signal<'ADMIN' | null>('ADMIN'),
      nombre: signal<string | null>('Administrador'),
      loading: signal(false),
      me: jasmine.createSpy('me').and.returnValue(of(void 0))
    } as Partial<AuthService>;

    await TestBed.configureTestingModule({
      imports: [AdminPage],
      providers: [
        { provide: AdminService, useValue: adminSpy },
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load initial admin data', () => {
    expect(component).toBeTruthy();
    expect(adminSpy.getPendingTrainers).toHaveBeenCalled();
    expect(adminSpy.buscarUsuarios).toHaveBeenCalled();
  });

  it('should change section to USUARIOS and trigger buscarUsuarios', () => {
    adminSpy.buscarUsuarios.calls.reset();

    component.cambiarSeccion('USUARIOS');

    expect(component.seccionActual()).toBe('USUARIOS');
    expect(adminSpy.buscarUsuarios).toHaveBeenCalled();
    expect(component.busqueda()).toBe('');
  });

  it('should approve a selected entrenador and remove it from the list', () => {
    const entrenador = { id: 'trainer1', nombre: 'Entrenador', correo: 'coach@test.com' } as any;
    component.solicitudes.set([entrenador]);
    component.entrenadorSeleccionado.set(entrenador);

    component.confirmarAprobacion();

    expect(adminSpy.processReview).toHaveBeenCalledWith('trainer1', true);
    expect(component.solicitudes().length).toBe(0);
    expect(component.mostrarModalExito()).toBeTrue();
  });
});
