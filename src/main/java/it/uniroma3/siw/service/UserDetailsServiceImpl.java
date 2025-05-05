package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(email);
        if (cliente == null) {
            throw new UsernameNotFoundException("Utente non trovato con email: " + email);
        }
        // Aggiungi un log per verificare l'utente caricato
        System.out.println("Utente caricato: " + cliente.getEmail());
        return new User(cliente.getEmail(), cliente.getPassword(), new ArrayList<>()); // Gestisci i ruoli se ne hai
    }
}