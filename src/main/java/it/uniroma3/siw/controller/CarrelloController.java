package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Carrello;
import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.model.ElementoCarrello;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Pizzeria;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.service.CarrelloService;
import it.uniroma3.siw.service.ClienteService;
import it.uniroma3.siw.service.PizzaService;
import it.uniroma3.siw.service.PizzeriaService;
import it.uniroma3.siw.service.IngredienteService;
import jakarta.servlet.http.HttpServletRequest;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/carrello")
public class CarrelloController {

    @Autowired
    private CarrelloService carrelloService;

    @Autowired
    private PizzaService pizzaService;
    
    @Autowired
    private ClienteService clienteService;

    @Autowired 
    private PizzeriaService pizzeriaService;

    @Autowired 
    private IngredienteService ingredienteService;


    @ModelAttribute("pizzeria")
    public Pizzeria getPizzeria() {
        return pizzeriaService.getOrCreateDefaultPizzeria();
    }

    @GetMapping
    public String visualizzaCarrello(Model model) {
        Carrello carrello = carrelloService.getCarrelloCorrente();
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        model.addAttribute("carrello", carrello);
        if (carrello != null) {
            for (ElementoCarrello el : carrello.getElementi()) {
                if (el.getPizza() != null) {
                    el.getPizza().setIngredientiExtraDisponibili(tuttiGliIngredienti);
                }
            }
            model.addAttribute("totaleCarrello", carrello.getTotaleComplessivo());
        } else {
            model.addAttribute("totaleCarrello", 0.0);
        }
        return "carrello"; 
    }

    @PostMapping("/aggiungiPizza")
    public String aggiungiPizzaAlCarrello(@RequestParam Long pizzaId,
                                     @RequestParam(required = false) List<Long> ingredientiExtraIds,
                                     @RequestParam(defaultValue = "1") int quantita,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        try {
            carrelloService.aggiungiPizzaAlCarrello(pizzaId, ingredientiExtraIds, quantita);
            redirectAttributes.addFlashAttribute("successoCarrello", "Pizza aggiunta al carrello!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erroreCarrello", e.getMessage());
            Pizza pizza = pizzaService.findById(pizzaId); 
            if (pizza != null && pizza.getScontoApplicato() != null && pizza.getScontoApplicato().getPercentuale() > 0) {
                return "redirect:/pizze_scontate";
            }
            return "redirect:/"; 
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty() && (referer.contains("/pizze_scontate") || referer.contains("/"))) {
            return "redirect:" + referer;
        }
        Pizza pizzaAggiunta = pizzaService.findById(pizzaId);
         if (pizzaAggiunta != null && pizzaAggiunta.getScontoApplicato() != null && pizzaAggiunta.getScontoApplicato().getPercentuale() > 0) {
            return "redirect:/pizze_scontate";
        }
        return "redirect:/";
    }

    @PostMapping("/aggiungiBevanda")
    public String aggiungiBevandaAlCarrello(@RequestParam Long bevandaId,
                                           @RequestParam(defaultValue = "1") int quantita,
                                           RedirectAttributes redirectAttributes,
                                           HttpServletRequest request) {
        try {
            carrelloService.aggiungiBevandaAlCarrello(bevandaId, quantita);
            redirectAttributes.addFlashAttribute("successoCarrello", "Bevanda aggiunta al carrello!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erroreCarrello", e.getMessage());
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/"; 
    }

    
    @PostMapping("/rimuovi/{elementoCarrelloId}")
    public String rimuoviDalCarrello(@PathVariable Long elementoCarrelloId, RedirectAttributes redirectAttributes) {
        try {
            carrelloService.rimuoviElementoDalCarrello(elementoCarrelloId);
            redirectAttributes.addFlashAttribute("successoCarrello", "Articolo rimosso dal carrello.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroreCarrello", "Impossibile rimuovere l'elemento: " + e.getMessage());
        }
        return "redirect:/carrello";
    }

    @PostMapping("/aggiorna/{elementoCarrelloId}")
    public String aggiornaQuantita(@PathVariable Long elementoCarrelloId, 
                                 @RequestParam int quantita,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (quantita <= 0) {
                 carrelloService.rimuoviElementoDalCarrello(elementoCarrelloId);
                 redirectAttributes.addFlashAttribute("successoCarrello", "Articolo rimosso dal carrello per quantità zero.");
            } else {
                carrelloService.aggiornaQuantitaElemento(elementoCarrelloId, quantita);
                redirectAttributes.addFlashAttribute("successoCarrello", "Quantità aggiornata.");
            }
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("erroreCarrello", "Impossibile aggiornare l'elemento: " + e.getMessage());
        }
        return "redirect:/carrello";
    }
    
    @PostMapping("/aggiornaExtra/{elementoCarrelloId}")
    public String aggiornaExtraElemento(@PathVariable Long elementoCarrelloId,
                                        @RequestParam(required = false) List<Long> selectedExtraIds,
                                        RedirectAttributes redirectAttributes) {
        try {
             carrelloService.aggiornaExtraElementoCarrello(elementoCarrelloId, selectedExtraIds);
            redirectAttributes.addFlashAttribute("successoCarrello", "Extra aggiornati con successo!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erroreCarrello", "Errore nell'aggiornamento degli extra: " + e.getMessage());
        }
        return "redirect:/carrello";
    }


    @PostMapping("/svuota")
    public String svuotaCarrello(RedirectAttributes redirectAttributes) {
        try {
            carrelloService.svuotaCarrello();
            redirectAttributes.addFlashAttribute("successoCarrello", "Carrello svuotato con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroreCarrello", "Impossibile svuotare il carrello: " + e.getMessage());
        }
        return "redirect:/carrello";
    }
  
    @PostMapping("/aggiorna-anon")
    public String aggiornaQuantitaAnon(@RequestParam int itemIndex,
                                     @RequestParam int quantita,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (quantita <= 0) {
                 carrelloService.rimuoviElementoDalCarrelloAnon(itemIndex);
                 redirectAttributes.addFlashAttribute("successoCarrello", "Articolo rimosso dal carrello.");
            } else {
                carrelloService.aggiornaQuantitaElementoAnon(itemIndex, quantita);
                redirectAttributes.addFlashAttribute("successoCarrello", "Quantità aggiornata.");
            }
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("erroreCarrello", "Impossibile aggiornare l'elemento anonimo: " + e.getMessage());
        }
        return "redirect:/carrello";
    }

    @PostMapping("/rimuovi-anon")
    public String rimuoviDalCarrelloAnon(@RequestParam int itemIndex, RedirectAttributes redirectAttributes) {
        try {
            carrelloService.rimuoviElementoDalCarrelloAnon(itemIndex);
            redirectAttributes.addFlashAttribute("successoCarrello", "Articolo rimosso dal carrello.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroreCarrello", "Impossibile rimuovere l'elemento anonimo: " + e.getMessage());
        }
        return "redirect:/carrello";
    }
    
    @PostMapping("/aggiornaExtra-anon")
    public String aggiornaExtraElementoAnon(@RequestParam int itemIndex,
                                        @RequestParam(required = false) List<Long> selectedExtraIds,
                                        RedirectAttributes redirectAttributes) {
         try {
            carrelloService.aggiornaExtraElementoCarrelloAnon(itemIndex, selectedExtraIds);
            redirectAttributes.addFlashAttribute("successoCarrello", "Extra aggiornati con successo!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erroreCarrello", "Errore nell'aggiornamento degli extra: " + e.getMessage());
        }
        return "redirect:/carrello";
    }

    @ModelAttribute
    public void addUserDetailsToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            Object principal = authentication.getPrincipal();
            Cliente cliente = null;
            String oauthUserName = null;

            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                cliente = clienteService.findByEmail(userDetails.getUsername());
            } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
                String email = oauth2User.getAttribute("email"); 
                if (email != null) {
                    cliente = clienteService.findByEmail(email);
                }
                if (cliente == null) { 
                    oauthUserName = oauth2User.getAttribute("name"); 
                    if (oauthUserName == null) {
                        oauthUserName = oauth2User.getAttribute("given_name"); 
                    }
                }
            }

            if (cliente != null) {
                model.addAttribute("clienteAttuale", cliente);
            } else if (oauthUserName != null) {
                model.addAttribute("oauthUserDisplayName", oauthUserName);
            }
            model.addAttribute("isAuthenticated", true);
        } else {
             model.addAttribute("isAuthenticated", false);
        }
    }
}