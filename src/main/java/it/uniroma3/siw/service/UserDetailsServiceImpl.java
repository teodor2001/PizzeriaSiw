package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Amministratore;
import it.uniroma3.siw.model.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AdminService adminService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteService.findByEmail(email);
        if (cliente != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            return new User(cliente.getEmail(), cliente.getPassword(), authorities);
        }

        Amministratore amministratore = adminService.findByEmail(email);
        if (amministratore != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return new User(amministratore.getEmail(), amministratore.getPassword(), authorities);
        }

        throw new UsernameNotFoundException("Utente non trovato con email: " + email);
    }
}