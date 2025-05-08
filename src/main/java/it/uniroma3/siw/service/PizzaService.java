package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.repository.PizzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PizzaService {

    @Autowired
    private PizzaRepository pizzaRepository;

    @Transactional
    public Pizza save(Pizza pizza) {
        return pizzaRepository.save(pizza);
    }

    public Pizza findById(Long id) {
        Optional<Pizza> result = pizzaRepository.findById(id);
        return result.orElse(null);
    }

    public List<Pizza> findAll() {
        return (List<Pizza>) pizzaRepository.findAll();
    }

    @Transactional
    public void delete(Pizza pizza) {
        pizzaRepository.delete(pizza);
    }

    @Transactional
    public void deleteById(Long id) {
        pizzaRepository.deleteById(id);
    }

    public List<Pizza> findByNome(String nome) {
        return pizzaRepository.findByNome(nome);
    }

    public List<Pizza> findByPrezzo(Double prezzoBase) {
        return pizzaRepository.findByPrezzoBase(prezzoBase);
    }
}