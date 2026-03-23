package com.sistema.gritfitprueba.service;

import com.sistema.gritfitprueba.model.Ejercicio;
import com.sistema.gritfitprueba.repositories.EjercicioRepository;
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
