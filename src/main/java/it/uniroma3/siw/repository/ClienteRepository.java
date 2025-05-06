package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Cliente;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface ClienteRepository extends CrudRepository<Cliente, Long> { // Estende CrudRepository o JpaRepository
	Cliente findByEmail(String email); // Metodo per trovare un cliente tramite email

	Optional<Cliente> findById(Long id); // Esempio di altro metodo
}