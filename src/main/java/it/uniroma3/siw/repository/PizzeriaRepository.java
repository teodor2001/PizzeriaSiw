package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Pizzeria;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface PizzeriaRepository extends CrudRepository<Pizzeria, Long> {
    Optional<Pizzeria> findByNome(String nome);
}