package it.uniroma3.siw.repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.model.Ordine;

public interface OrdineRepository extends CrudRepository<Ordine, Long> {
    Iterable<Ordine> findByCliente(Cliente cliente);
} 