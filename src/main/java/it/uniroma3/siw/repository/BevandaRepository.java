package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Bevanda;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BevandaRepository extends CrudRepository<Bevanda, Long> {

    List<Bevanda> findByNome(String nome);

    List<Bevanda> findByPrezzo(Double prezzo);

    // Puoi aggiungere qui altri metodi personalizzati per la ricerca di bevande, se necessario
}