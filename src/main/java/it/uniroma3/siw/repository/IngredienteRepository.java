package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Ingrediente;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface IngredienteRepository extends CrudRepository<Ingrediente, Long> {
    // Questo metodo recupera tutti gli ingredienti
    List<Ingrediente> findAll();

    // Puoi mantenere anche questo metodo se hai bisogno di recuperare ingredienti per ID
    List<Ingrediente> findAllById(Iterable<Long> ids);
}