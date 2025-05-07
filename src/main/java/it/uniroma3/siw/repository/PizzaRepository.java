package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Pizza;
import org.springframework.data.repository.CrudRepository;

public interface PizzaRepository extends CrudRepository<Pizza, Long> {
    // Puoi aggiungere qui metodi personalizzati per la ricerca di pizze, se necessario
}