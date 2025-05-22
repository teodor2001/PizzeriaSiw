package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.repository.IngredienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class IngredienteService {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Transactional
    public Ingrediente save(Ingrediente ingrediente) {
        return ingredienteRepository.save(ingrediente);
    }

    public Ingrediente findById(Long id) {
        Optional<Ingrediente> result = ingredienteRepository.findById(id);
        return result.orElse(null);
    }

    public Set<Ingrediente> findAll() {
        List<Ingrediente> ingredienteList = ingredienteRepository.findAllByActiveTrue();
        return new HashSet<>(ingredienteList);
    }

    public Set<Ingrediente> findAllById(Iterable<Long> ids) {
        List<Ingrediente> ingredienteList = ingredienteRepository.findAllById(ids);
        return new HashSet<>(ingredienteList);
    }

    @Transactional
    public void delete(Ingrediente ingrediente) {
        if (ingrediente != null) {
            ingrediente.setActive(false);
            ingredienteRepository.save(ingrediente);
        }
    }

    @Transactional
    public void deleteById(Long id) {
        Ingrediente ingrediente = findById(id);
        if (ingrediente != null) {
            ingrediente.setActive(false);
            ingredienteRepository.save(ingrediente);
        }
    }
}