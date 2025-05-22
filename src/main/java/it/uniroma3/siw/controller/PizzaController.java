package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Pizzeria;
import it.uniroma3.siw.repository.PizzaRepository;
import it.uniroma3.siw.service.BevandaService;
import it.uniroma3.siw.service.ClienteService;
import it.uniroma3.siw.service.IngredienteService;
import it.uniroma3.siw.service.PizzeriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
public class PizzaController {

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private BevandaService bevandaService;

    @Autowired
    private IngredienteService ingredienteService;

    @Autowired
    private PizzeriaService pizzeriaService;

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<Pizza> pizzeClassiche = pizzaRepository.findAll();
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        for (Pizza pizza : pizzeClassiche) {
            pizza.setIngredientiExtraDisponibili(tuttiGliIngredienti);
        }

        List<Bevanda> bevande = bevandaService.findAll();
        Pizzeria pizzeria = pizzeriaService.getOrCreateDefaultPizzeria();
        model.addAttribute("pizzeria", pizzeria);
        
        model.addAttribute("pizzeClassiche", pizzeClassiche);
        model.addAttribute("bevande", bevande);
        model.addAttribute("tuttiGliIngredientiExtra", tuttiGliIngredienti);
        return "index";
    }

    @GetMapping("/pizze_scontate")
    public String showTutteLePizze(Model model) {
        List<Pizza> tutteLePizze = pizzaRepository.findAll();
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        for (Pizza pizza : tutteLePizze) {
            pizza.setIngredientiExtraDisponibili(tuttiGliIngredienti);
        }
        
        List<Bevanda> bevande = bevandaService.findAll();
        Pizzeria pizzeria = pizzeriaService.getOrCreateDefaultPizzeria();
        model.addAttribute("pizzeria", pizzeria);

        model.addAttribute("elencoPizze", tutteLePizze);
        model.addAttribute("bevande", bevande);
        model.addAttribute("tuttiGliIngredientiExtra", tuttiGliIngredienti);
        return "pizze_scontate";
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
        }
    }
}