package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Sconto;
import it.uniroma3.siw.repository.PizzaRepository;
import it.uniroma3.siw.repository.ScontoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PizzaService {

	@Autowired
	private PizzaRepository pizzaRepository;
	@Autowired
	private ScontoRepository scontoRepository;

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

	@Transactional
	public void aggiungiScontoAPizza(Long pizzaId, Long scontoId) {
		Pizza pizza = pizzaRepository.findById(pizzaId).orElse(null);
		Sconto sconto = scontoRepository.findById(scontoId).orElse(null);

		if (pizza != null && sconto != null) {
			pizza.setScontoApplicato(sconto);
			pizzaRepository.save(pizza);
		} else {
			throw new IllegalArgumentException("Pizza o Sconto non trovato");
		}
	}

	@Transactional
	public void rimuoviScontoDaPizza(Long pizzaId) {
		Pizza pizza = pizzaRepository.findById(pizzaId).orElse(null);
		if (pizza != null) {
			pizza.setScontoApplicato(null);
			pizzaRepository.save(pizza);
		} else {
			throw new IllegalArgumentException("Pizza non trovata");
		}
	}

	public List<Pizza> getPizzeConSconto(Long scontoId) {
		Sconto sconto = scontoRepository.findById(scontoId).orElse(null);
		if (sconto != null) {
			return sconto.getPizze();
		}
		return null;
	}

}