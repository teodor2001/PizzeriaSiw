package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Amministratore;
import it.uniroma3.siw.repository.AmministratoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AmministratoreRepository amministratoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Amministratore save(Amministratore amministratore) {
        String encodedPassword = passwordEncoder.encode(amministratore.getPassword());
        amministratore.setPassword(encodedPassword);
        return amministratoreRepository.save(amministratore);
    }

    public Amministratore findByEmail(String email) {
        return amministratoreRepository.findByEmail(email);
    }
}