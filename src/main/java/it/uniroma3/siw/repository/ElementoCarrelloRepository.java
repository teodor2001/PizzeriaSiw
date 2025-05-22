package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.model.ElementoCarrello;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ElementoCarrelloRepository extends JpaRepository<ElementoCarrello, Long> {
	
	List<ElementoCarrello> findByBevanda(Bevanda bevanda);
    List<ElementoCarrello> findByPizzaIdPizza(Long pizzaId);
    void deleteByCarrelloId(Long carrelloId);
}