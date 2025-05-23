package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.model.Ordine;
import it.uniroma3.siw.repository.OrdineRepository;

@Service
public class OrdineService {

    @Autowired
    private OrdineRepository ordineRepository;

    @Transactional
    public Ordine creaOrdine(Cliente cliente, String indirizzo, String orario) {
        Ordine ordine = new Ordine();
        ordine.setCliente(cliente);
        ordine.setIndirizzoConsegna(indirizzo);
        ordine.setOrarioConsegna(orario);
        
        // TODO: Aggiungere gli item dal carrello
        
        return ordineRepository.save(ordine);
    }

    @Transactional
    public Ordine getOrdine(Long id) {
        return ordineRepository.findById(id).orElse(null);
    }

    @Transactional
    public Iterable<Ordine> getAllOrdini() {
        return ordineRepository.findAll();
    }

    @Transactional
    public Iterable<Ordine> getOrdiniByCliente(Cliente cliente) {
        return ordineRepository.findByCliente(cliente);
    }
} 