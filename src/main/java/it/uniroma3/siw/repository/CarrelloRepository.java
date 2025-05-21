package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Carrello;
import it.uniroma3.siw.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarrelloRepository extends JpaRepository<Carrello, Long> {
    Optional<Carrello> findByCliente(Cliente cliente);
    Optional<Carrello> findByClienteIdCliente(Long clienteId);
}