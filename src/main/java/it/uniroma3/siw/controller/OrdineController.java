package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.model.Ordine;
import it.uniroma3.siw.service.ClienteService;
import it.uniroma3.siw.service.OrdineService;
import it.uniroma3.siw.service.PizzeriaService;

@Controller
@RequestMapping("/ordine")
public class OrdineController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private OrdineService ordineService;

    @Autowired
    private PizzeriaService pizzeriaService;

    @GetMapping("/conferma")
    public String mostraConfermaOrdine(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Cliente cliente = clienteService.getClienteByEmail(email);
        
        model.addAttribute("cliente", cliente);
        model.addAttribute("pizzeria", pizzeriaService.getOrCreateDefaultPizzeria());
        return "confermaOrdine";
    }

    @PostMapping("/conferma")
    public String confermaOrdine(@ModelAttribute Cliente clienteForm,
                               @RequestParam String orario,
                               @RequestParam boolean salvaDati,
                               Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Cliente cliente = clienteService.getClienteByEmail(email);

        // Aggiorna i dati del cliente se richiesto
        if (salvaDati) {
            cliente.setIndirizzo(clienteForm.getIndirizzo());
            cliente.setTelefono(clienteForm.getTelefono());
            clienteService.saveCliente(cliente);
        }

        // Crea e salva l'ordine
        Ordine ordine = ordineService.creaOrdine(cliente, clienteForm.getIndirizzo(), orario);
        
        // Aggiungi l'ordine e la pizzeria al model
        model.addAttribute("ordine", ordine);
        model.addAttribute("pizzeria", pizzeriaService.getOrCreateDefaultPizzeria());
        
        return "ordineConfermato";
    }
} 