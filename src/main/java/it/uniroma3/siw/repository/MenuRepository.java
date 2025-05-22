package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Menu;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface MenuRepository extends CrudRepository<Menu, Long> {
    Optional<Menu> findByNome(String nome);
}