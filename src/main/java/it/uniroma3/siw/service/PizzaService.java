package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Sconto;
import it.uniroma3.siw.repository.PizzaRepository;
import it.uniroma3.siw.repository.ScontoRepository;

import org.hibernate.Hibernate;
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

    @Transactional(readOnly = true)
	public Pizza findById(Long id) {
		Optional<Pizza> result = pizzaRepository.findById(id);
        result.ifPresent(p -> {
            Hibernate.initialize(p.getNomiIngredientiBase()); 
            if (p.getScontoApplicato() != null) {
                Hibernate.initialize(p.getScontoApplicato());
            }
        });
		return result.orElse(null);
	}

    @Transactional(readOnly = true) 
	public List<Pizza> findAll() {
		List<Pizza> pizze = (List<Pizza>) pizzaRepository.findAll();
        for (Pizza p : pizze) {
            Hibernate.initialize(p.getNomiIngredientiBase()); 
            if (p.getScontoApplicato() != null) {
                 Hibernate.initialize(p.getScontoApplicato());
            }
        }
		return pizze;
	}

	@Transactional
	public void delete(Pizza pizza) {
		pizzaRepository.delete(pizza);
	}

	@Transactional
	public void deleteById(Long id) {
		pizzaRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public List<Pizza> findByNome(String nome) {
        List<Pizza> pizze = pizzaRepository.findByNome(nome);
        for (Pizza p : pizze) {
            Hibernate.initialize(p.getNomiIngredientiBase()); 
             if (p.getScontoApplicato() != null) {
                 Hibernate.initialize(p.getScontoApplicato());
            }
        }
		return pizze;
	}

	@Transactional(readOnly = true)
	public List<Pizza> findByPrezzoBase(Double prezzoBase) {
        List<Pizza> pizze = pizzaRepository.findByPrezzoBase(prezzoBase);
        for (Pizza p : pizze) {
            Hibernate.initialize(p.getNomiIngredientiBase());
            if (p.getScontoApplicato() != null) {
                 Hibernate.initialize(p.getScontoApplicato());
            }
        }
		return pizze;
	}

	@Transactional
	public void aggiungiScontoAPizza(Long pizzaId, Long scontoId) {
		Pizza pizza = this.findById(pizzaId);
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
		Pizza pizza = this.findById(pizzaId);
		if (pizza != null) {
			pizza.setScontoApplicato(null);
			pizzaRepository.save(pizza);
		} else {
			throw new IllegalArgumentException("Pizza non trovata");
		}
	}

	@Transactional(readOnly = true)
	public List<Pizza> getPizzeConSconto(Long scontoId) {
		Sconto sconto = scontoRepository.findById(scontoId).orElse(null);
		if (sconto != null) {
            Hibernate.initialize(sconto.getPizze());
            for(Pizza p : sconto.getPizze()){
                Hibernate.initialize(p.getNomiIngredientiBase());
                if (p.getScontoApplicato() != null) {
                     Hibernate.initialize(p.getScontoApplicato());
                }
            }
			return sconto.getPizze();
		}
		return List.of();
	}
}