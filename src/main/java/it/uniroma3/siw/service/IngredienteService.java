package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.repository.IngredienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public List<Ingrediente> findAll() {
        return (List<Ingrediente>) ingredienteRepository.findAll();
    }

    public List<Ingrediente> findAllById(Iterable<Long> ids) {
        return ingredienteRepository.findAllById(ids);
    }

    @Transactional
    public void delete(Ingrediente ingrediente) {
        ingredienteRepository.delete(ingrediente);
    }

    @Transactional
    public void deleteById(Long id) {
        ingredienteRepository.deleteById(id);
    }
}