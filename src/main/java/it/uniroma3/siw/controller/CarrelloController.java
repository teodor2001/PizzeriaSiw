package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Carrello;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.service.CarrelloService;
import it.uniroma3.siw.service.PizzaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/carrello")
public class CarrelloController {

    @Autowired
    private CarrelloService carrelloService;

    @Autowired
    private PizzaService pizzaService; 

    @GetMapping
    public String visualizzaCarrello(Model model) {
        Carrello carrello = carrelloService.getCarrelloCorrente();
        model.addAttribute("carrello", carrello);
        if (carrello != null) {
            model.addAttribute("totaleCarrello", carrello.getTotaleComplessivo());
        } else {
            model.addAttribute("totaleCarrello", 0.0);
        }
        return "carrello"; 
    }

    @PostMapping("/aggiungi")
    public String aggiungiAlCarrello(@RequestParam Long pizzaId,
                                     @RequestParam(required = false) List<Long> ingredientiExtraIds,
                                     @RequestParam(defaultValue = "1") int quantita,
                                     RedirectAttributes redirectAttributes) {
        try {
            carrelloService.aggiungiPizzaAlCarrello(pizzaId, ingredientiExtraIds, quantita);
            redirectAttributes.addFlashAttribute("successoCarrello", "Articolo aggiunto al carrello!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erroreCarrello", e.getMessage());
            Pizza pizza = pizzaService.findById(pizzaId);
            if (pizza != null && pizza.getScontoApplicato() != null && pizza.getScontoApplicato().getPercentuale() > 0) {
                return "redirect:/pizze_scontate";
            }
            return "redirect:/";
        }
        return "redirect:/carrello";
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
                 redirectAttributes.addFlashAttribute("successoCarrello", "Articolo rimosso dal carrello.");
            } else {
                carrelloService.aggiornaQuantitaElemento(elementoCarrelloId, quantita);
                redirectAttributes.addFlashAttribute("successoCarrello", "Quantità aggiornata.");
            }
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("erroreCarrello", "Impossibile aggiornare l'elemento: " + e.getMessage());
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
}