package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Pizzeria;
import org.springframework.data.repository.CrudRepository;

public interface PizzeriaRepository extends CrudRepository<Pizzeria, Long> {
    Pizzeria findByNome(String nome);
}