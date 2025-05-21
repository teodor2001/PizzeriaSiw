package it.uniroma3.siw.config;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.service.CarrelloService;
import it.uniroma3.siw.service.ClienteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private CarrelloService carrelloService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            Cliente cliente = clienteService.findByEmail(username);
            if (cliente != null && cliente.getIdCliente() != null) {
                Optional<it.uniroma3.siw.model.Carrello> carrelloOpt = carrelloService.getCarrelloByClienteId(cliente.getIdCliente());
                carrelloOpt.ifPresent(carrello -> {
                    if (carrello.getId() != null) {
                       carrelloService.svuotaCarrelloSpecifico(carrello.getId());
                       System.out.println("Carrello svuotato per l'utente: " + cliente.getEmail() + " al logout.");
                    }
                });
            }
        }
    }
}