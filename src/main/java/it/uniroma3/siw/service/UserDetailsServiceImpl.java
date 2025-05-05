package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(email);
        if (cliente == null) {
            throw new UsernameNotFoundException("Utente non trovato con email: " + email);
        }
        // La password recuperata dal cliente.getPassword() DOVREBBE essere già codificata
        // se il processo di registrazione la codifica correttamente.
        // Spring Security si occuperà di confrontare la password inserita (non codificata)
        // con questa password codificata utilizzando il PasswordEncoder.
        return new User(cliente.getEmail(), cliente.getPassword(), new ArrayList<>());
    }
}