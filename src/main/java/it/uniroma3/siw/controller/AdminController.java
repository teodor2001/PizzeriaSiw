package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Menu;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Sconto;
import it.uniroma3.siw.service.BevandaService;
import it.uniroma3.siw.service.IngredienteService;
import it.uniroma3.siw.service.MenuService;
import it.uniroma3.siw.service.PizzaService;
import it.uniroma3.siw.service.ScontoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PizzaService pizzaService;

    @Autowired
    private IngredienteService ingredienteService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private BevandaService bevandaService;

    @Autowired
    private ScontoService scontoService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Pizza> tutteLePizze = pizzaService.findAll();
        model.addAttribute("pizze", tutteLePizze);

        List<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();
        model.addAttribute("ingredienti", tuttiGliIngredienti);

        List<Bevanda> tutteLeBevande = bevandaService.findAll();
        model.addAttribute("bevande", tutteLeBevande);

        return "admin/dashboard";
    }

    @GetMapping("/aggiungiPizza")
    public String aggiungiPizzaForm(Model model) {
        List<Ingrediente> ingredientiExtraDisponibili = ingredienteService.findAll();
        List<Ingrediente> ingredientiBaseDisponibili = ingredienteService.findAll();

        model.addAttribute("pizza", new Pizza());
        model.addAttribute("ingredientiExtraDisponibili", ingredientiExtraDisponibili);
        model.addAttribute("ingredientiBaseDisponibili", ingredientiBaseDisponibili);

        return "admin/aggiungi_pizza";
    }

    @PostMapping("/salvaPizza")
    public String salvaPizza(@Valid @ModelAttribute("pizza") Pizza pizza, BindingResult bindingResult,
                             @RequestParam(value = "ingredientiBase", required = false) List<Long> ingredientiBaseIds,
                             @RequestParam(value = "ingredientiExtra", required = false) List<Long> ingredientiExtraIds,
                             @RequestParam(value = "percentualeSconto", required = false) Integer percentualeSconto, Model model) {

        if (bindingResult.hasErrors()) {
            List<Ingrediente> ingredientiExtraDisponibili = ingredienteService.findAll();
            List<Ingrediente> ingredientiBaseDisponibili = ingredienteService.findAll();
            model.addAttribute("ingredientiExtraDisponibili", ingredientiExtraDisponibili);
            model.addAttribute("ingredientiBaseDisponibili", ingredientiBaseDisponibili);
            return "admin/aggiungi_pizza";
        }

        if (ingredientiBaseIds != null && !ingredientiBaseIds.isEmpty()) {
            List<Ingrediente> ingredientiBase = ingredienteService.findAllById(ingredientiBaseIds);
            pizza.setIngredientiBase(ingredientiBase);
        } else {
            pizza.setIngredientiBase(new ArrayList<>());
        }

        if (ingredientiExtraIds != null && !ingredientiExtraIds.isEmpty()) {
            List<Ingrediente> ingredientiExtra = ingredienteService.findAllById(ingredientiExtraIds);
            pizza.setIngredientiExtra(new ArrayList<>());
        } else {
            pizza.setIngredientiExtra(new ArrayList<>());
        }

        if (percentualeSconto != null && percentualeSconto > 0) {
            Sconto sconto = new Sconto();
            sconto.setPercentuale(percentualeSconto);
            scontoService.creaSconto(sconto);
            pizza.setScontoApplicato(sconto);
        } else {
            pizza.setScontoApplicato(null);
        }

        Menu menu = menuService.findFirstMenu();
        if (menu != null) {
            pizza.setMenu(menu);
            pizzaService.save(pizza);
            menu.aggiungiPizza(pizza);
            menuService.save(menu);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("errorMessage", "Impossibile trovare il menu per aggiungere la pizza.");
            return "admin/aggiungi_pizza";
        }
    }

    @GetMapping("/modificaPizza/{id}")
    public String modificaPizzaForm(@PathVariable("id") Long id, Model model) {
        Pizza pizzaDaModificare = pizzaService.findById(id);
        if (pizzaDaModificare != null) {
            List<Ingrediente> ingredientiExtraDisponibili = ingredienteService.findAll();
            List<Ingrediente> ingredientiBaseDisponibili = ingredienteService.findAll();
            model.addAttribute("pizza", pizzaDaModificare);
            model.addAttribute("ingredientiExtraDisponibili", ingredientiExtraDisponibili);
            model.addAttribute("ingredientiBaseDisponibili", ingredientiBaseDisponibili);
            return "admin/modifica_pizza";
        } else {
            // Gestisci il caso in cui la pizza non viene trovata
            return "redirect:/admin/dashboard"; // Oppure una pagina di errore
        }
    }

    @PostMapping("/salvaModifichePizza")
    public String salvaModifichePizza(@Valid @ModelAttribute("pizza") Pizza pizza, BindingResult bindingResult,
                                     @RequestParam(value = "ingredientiBase", required = false) List<Long> ingredientiBaseIds,
                                     @RequestParam(value = "ingredientiExtra", required = false) List<Long> ingredientiExtraIds,
                                     @RequestParam(value = "percentualeSconto", required = false) Float percentualeSconto, Model model) {

        if (bindingResult.hasErrors()) {
            List<Ingrediente> ingredientiExtraDisponibili = ingredienteService.findAll();
            List<Ingrediente> ingredientiBaseDisponibili = ingredienteService.findAll();
            model.addAttribute("ingredientiExtraDisponibili", ingredientiExtraDisponibili);
            model.addAttribute("ingredientiBaseDisponibili", ingredientiBaseDisponibili);
            return "admin/modifica_pizza";
        }

        if (ingredientiBaseIds != null && !ingredientiBaseIds.isEmpty()) {
            List<Ingrediente> ingredientiBase = ingredienteService.findAllById(ingredientiBaseIds);
            pizza.setIngredientiBase(ingredientiBase);
        } else {
            pizza.setIngredientiBase(new ArrayList<>());
        }

        if (ingredientiExtraIds != null && !ingredientiExtraIds.isEmpty()) {
            List<Ingrediente> ingredientiExtra = ingredienteService.findAllById(ingredientiExtraIds);
            pizza.setIngredientiExtra(new ArrayList<>());
        } else {
            pizza.setIngredientiExtra(new ArrayList<>());
        }

        if (percentualeSconto != null && percentualeSconto > 0) {
            Sconto sconto = new Sconto();
            sconto.setPercentuale(Math.round(percentualeSconto)); // Arrotonda a un intero
            scontoService.creaSconto(sconto);
            pizza.setScontoApplicato(sconto);
        } else {
            pizza.setScontoApplicato(null);
        }

        Menu menu = menuService.findFirstMenu();
        if (menu != null) {
            pizza.setMenu(menu);
            pizzaService.save(pizza); // Il metodo save aggiornerà l'entità esistente
            menu.aggiornaPizza(pizza); // Assicurati che questo metodo sia implementato nel tuo Menu model o MenuService
            menuService.save(menu);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("errorMessage", "Impossibile trovare il menu.");
            return "admin/modifica_pizza";
        }
    }

    @GetMapping("/aggiungiIngrediente")
    public String aggiungiIngredienteForm(Model model) {
        model.addAttribute("ingrediente", new Ingrediente());
        return "admin/aggiungi_ingrediente";
    }

    @PostMapping("/salvaIngrediente")
    public String salvaIngrediente(@Valid @ModelAttribute("ingrediente") Ingrediente ingrediente,
                                    BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/aggiungi_ingrediente";
        }
        ingredienteService.save(ingrediente);
        return "redirect:/admin/dashboard";
    }
    
    @GetMapping("/modificaIngrediente/{id}")
    public String modificaIngredienteForm(@PathVariable("id") Long id, Model model) {
        Ingrediente ingredienteDaModificare = ingredienteService.findById(id);
        if (ingredienteDaModificare != null) {
            model.addAttribute("ingrediente", ingredienteDaModificare);
            return "admin/modifica_ingrediente";
        } else {
            // Gestisci il caso in cui l'ingrediente non viene trovato
            return "redirect:/admin/dashboard"; // Oppure una pagina di errore
        }
    }

    @PostMapping("/salvaModificheIngrediente")
    public String salvaModificheIngrediente(@Valid @ModelAttribute("ingrediente") Ingrediente ingrediente,
                                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/modifica_ingrediente";
        }
        ingredienteService.save(ingrediente); // Il metodo save aggiornerà l'entità esistente
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/aggiungiBevanda")
    public String aggiungiBevandaForm(Model model) {
        model.addAttribute("bevanda", new Bevanda());
        return "admin/aggiungi_bevanda";
    }

    @PostMapping("/salvaBevanda")
    public String salvaBevanda(@Valid @ModelAttribute("bevanda") Bevanda bevanda, BindingResult bindingResult,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/aggiungi_bevanda";
        }

        Menu menu = menuService.findFirstMenu();
        if (menu != null) {
            bevanda.setMenu(menu);
            bevandaService.save(bevanda);
            menu.aggiungiBevanda(bevanda);
            menuService.save(menu);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("errorMessage", "Impossibile trovare il menu per aggiungere la bevanda.");
            return "admin/aggiungi_bevanda";
        }
    }
    
    @GetMapping("/modificaBevanda/{id}")
    public String modificaBevandaForm(@PathVariable("id") Long id, Model model) {
        Optional<Bevanda> bevandaDaModificareOptional = bevandaService.findById(id);
        if (bevandaDaModificareOptional.isPresent()) {
            Bevanda bevandaDaModificare = bevandaDaModificareOptional.get();
            model.addAttribute("bevanda", bevandaDaModificare);
            return "admin/modifica_bevanda";
        } else {
            // Gestisci il caso in cui la bevanda non viene trovata
            model.addAttribute("errorMessage", "Bevanda non trovata con ID: " + id);
            return "redirect:/admin/dashboard"; // Oppure una pagina di errore più specifica
        }
    }

    @PostMapping("/salvaModificheBevanda")
    public String salvaModificheBevanda(@Valid @ModelAttribute("bevanda") Bevanda bevanda,
                                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/modifica_bevanda";
        }
        bevandaService.save(bevanda); // Il metodo save aggiornerà l'entità esistente
        return "redirect:/admin/dashboard";
    }
}