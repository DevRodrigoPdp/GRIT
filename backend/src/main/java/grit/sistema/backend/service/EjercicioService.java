package grit.sistema.backend.service;

import grit.sistema.backend.model.Ejercicio;
import grit.sistema.backend.repositories.EjercicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EjercicioService {
    private final EjercicioRepository ejercicioRepository;

    public List<Ejercicio> guardarVarios(List<Ejercicio> ejercicios) {
        return ejercicioRepository.saveAll(ejercicios);
    }

    public void borrarEjercicio(Ejercicio ejercicio) {
        ejercicioRepository.delete(ejercicio);
    }

    public void borrarVarios(List<Ejercicio> ejercicios) {
        ejercicioRepository.deleteAll(ejercicios);
    }
}
