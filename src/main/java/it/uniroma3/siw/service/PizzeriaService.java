package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Pizzeria;
import it.uniroma3.siw.repository.PizzeriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PizzeriaService {

    @Autowired
    private PizzeriaRepository pizzeriaRepository;

    @Transactional
    public Pizzeria save(Pizzeria pizzeria) {
        return pizzeriaRepository.save(pizzeria);
    }

    public Optional<Pizzeria> findById(Long id) {
        return pizzeriaRepository.findById(id);
    }

    public Iterable<Pizzeria> findAll() {
        return pizzeriaRepository.findAll();
    }

    @Transactional
    public void delete(Pizzeria pizzeria) {
        pizzeriaRepository.delete(pizzeria);
    }

    @Transactional
    public void deleteById(Long id) {
        pizzeriaRepository.deleteById(id);
    }

    // Puoi aggiungere qui altri metodi di business logic se necessario
}