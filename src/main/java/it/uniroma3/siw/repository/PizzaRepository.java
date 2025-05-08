package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Pizza;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PizzaRepository extends CrudRepository<Pizza, Long> {

    List<Pizza> findByNome(String nome);

    List<Pizza> findByPrezzoBase(double prezzoBase);

}