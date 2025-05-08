package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Sconto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScontoRepository extends CrudRepository<Sconto, Long> {
}