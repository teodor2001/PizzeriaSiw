package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.repository.BevandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BevandaService {

    @Autowired
    private BevandaRepository bevandaRepository;

    @Transactional
    public Bevanda save(Bevanda bevanda) {
        return bevandaRepository.save(bevanda);
    }

    public Optional<Bevanda> findById(Long id) {
        return bevandaRepository.findById(id);
    }

    public List<Bevanda> findAll() {
        return (List<Bevanda>) bevandaRepository.findAll();
    }

    @Transactional
    public void delete(Bevanda bevanda) {
        bevandaRepository.delete(bevanda);	
    }

    @Transactional
    public void deleteById(Long id) {
        bevandaRepository.deleteById(id);
    }

    public List<Bevanda> findByNome(String nome) {
        return bevandaRepository.findByNome(nome);
    }

    public List<Bevanda> findByPrezzo(Double prezzo) {
        return bevandaRepository.findByPrezzo(prezzo);
    }
}
