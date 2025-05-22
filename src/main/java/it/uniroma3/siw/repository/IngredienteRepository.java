package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Ingrediente;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface IngredienteRepository extends CrudRepository<Ingrediente, Long> {
    List<Ingrediente> findAll();
    List<Ingrediente> findAllByActiveTrue();
    List<Ingrediente> findAllById(Iterable<Long> ids);
}