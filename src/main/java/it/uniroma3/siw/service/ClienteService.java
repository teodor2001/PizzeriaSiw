package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Cliente save(Cliente cliente) {
        String encodedPassword = passwordEncoder.encode(cliente.getPassword());
        cliente.setPassword(encodedPassword);
        return clienteRepository.save(cliente);
    }
    @Transactional
    public Cliente create(Cliente cliente) {
        String encodedPassword = passwordEncoder.encode(cliente.getPassword());
        cliente.setPassword(encodedPassword);
        return clienteRepository.save(cliente);
    }

    public Cliente findById(Long id) {
        Optional<Cliente> result = clienteRepository.findById(id);
        return result.orElse(null);
    }

    public Cliente findByEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    public List<Cliente> findAll() {
        return (List<Cliente>) clienteRepository.findAll();
    }
    public boolean emailAlreadyExists(String email) {
        return clienteRepository.findByEmail(email) != null;
    }

    @Transactional
    public void delete(Cliente cliente) {
        clienteRepository.delete(cliente);
    }

    @Transactional
    public void deleteById(Long id) {
		clienteRepository.deleteById(id);
	}

    @Transactional
    public Cliente getClienteByEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    @Transactional
    public Cliente saveCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Iterable<Cliente> getAllClienti() {
        return clienteRepository.findAll();
    }

    @Transactional
    public Cliente getCliente(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }
}