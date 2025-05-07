package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Amministratore;
import org.springframework.data.repository.CrudRepository;

public interface AmministratoreRepository extends CrudRepository<Amministratore, Long> {
    Amministratore findByEmail(String email);
}
