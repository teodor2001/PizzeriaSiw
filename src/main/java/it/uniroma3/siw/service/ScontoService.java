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
public class ScontoService {

	@Autowired
	private PizzaRepository pizzaRepository;
	@Autowired
	private ScontoRepository scontoRepository;

	@Transactional
	public Pizza applicaSconto(Long pizzaId, Long scontoId) {
		Pizza pizza = pizzaRepository.findById(pizzaId).orElse(null);
		Sconto sconto = scontoRepository.findById(scontoId).orElse(null);

		if (pizza == null) {
			throw new IllegalArgumentException("Pizza con ID " + pizzaId + " non trovata.");
		}
		if (sconto == null) {
			throw new IllegalArgumentException("Sconto con ID " + scontoId + " non trovato.");
		}

		pizza.setScontoApplicato(sconto);
		return pizzaRepository.save(pizza);
	}

	@Transactional
	public Pizza rimuoviSconto(Long pizzaId) {
		Pizza pizza = pizzaRepository.findById(pizzaId).orElse(null);
		if (pizza == null) {
			throw new IllegalArgumentException("Pizza con ID " + pizzaId + " non trovata.");
		}
		pizza.setScontoApplicato(null);
		return pizzaRepository.save(pizza);
	}

	@Transactional
	public Sconto creaSconto(Sconto sconto) {
		return scontoRepository.save(sconto);
	}

	@Transactional
	public void eliminaSconto(Long scontoId) {
		Optional<Sconto> sconto = scontoRepository.findById(scontoId);
		if (sconto.isEmpty()) {
			throw new IllegalArgumentException("Sconto con ID " + scontoId + " non trovato.");
		}
		scontoRepository.deleteById(scontoId);
	}

	public List<Sconto> getAllSconti() {
		return (List<Sconto>) scontoRepository.findAll();
	}

	public Sconto getSconto(Long id) {
		return scontoRepository.findById(id).orElse(null);
	}
}
