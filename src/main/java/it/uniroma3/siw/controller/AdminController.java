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
        model.addAttribute("pizze", pizzaService.findAll());
        model.addAttribute("ingredienti", ingredienteService.findAll());
        model.addAttribute("bevande", bevandaService.findAll());
        return "admin/dashboard";
    }

    @GetMapping("/aggiungiPizza")
    public String aggiungiPizzaForm(Model model) {
        model.addAttribute("pizza", new Pizza());
        model.addAttribute("ingredientiExtraDisponibili", ingredienteService.findAll());
        model.addAttribute("ingredientiBaseDisponibili", ingredienteService.findAll());
        return "admin/aggiungi_pizza";
    }

    @PostMapping("/salvaPizza")
    public String salvaPizza(@Valid @ModelAttribute("pizza") Pizza pizza, BindingResult bindingResult,
                             @RequestParam(value = "ingredientiBase", required = false) List<Long> ingredientiBaseIds,
                             @RequestParam(value = "ingredientiExtra", required = false) List<Long> ingredientiExtraIds,
                             @RequestParam(value = "percentualeSconto", required = false) Integer percentualeSconto, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("ingredientiExtraDisponibili", ingredienteService.findAll());
            model.addAttribute("ingredientiBaseDisponibili", ingredienteService.findAll());
            return "admin/aggiungi_pizza";
        }

        pizza.setIngredientiBase(ingredienteService.findAllById(ingredientiBaseIds != null ? ingredientiBaseIds : new ArrayList<>()));
        pizza.setIngredientiExtra(ingredienteService.findAllById(ingredientiExtraIds != null ? ingredientiExtraIds : new ArrayList<>()));

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
            model.addAttribute("pizza", pizzaDaModificare);
            model.addAttribute("ingredientiExtraDisponibili", ingredienteService.findAll());
            model.addAttribute("ingredientiBaseDisponibili", ingredienteService.findAll());
            return "admin/modifica_pizza";
        } else {
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/salvaModifichePizza")
    public String salvaModifichePizza(@Valid @ModelAttribute("pizza") Pizza pizza, BindingResult bindingResult,
                                     @RequestParam(value = "ingredientiBase", required = false) List<Long> ingredientiBaseIds,
                                     @RequestParam(value = "ingredientiExtra", required = false) List<Long> ingredientiExtraIds,
                                     @RequestParam(value = "percentualeSconto", required = false) Float percentualeSconto, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("ingredientiExtraDisponibili", ingredienteService.findAll());
            model.addAttribute("ingredientiBaseDisponibili", ingredienteService.findAll());
            return "admin/modifica_pizza";
        }

        pizza.setIngredientiBase(ingredienteService.findAllById(ingredientiBaseIds != null ? ingredientiBaseIds : new ArrayList<>()));
        pizza.setIngredientiExtra(ingredienteService.findAllById(ingredientiExtraIds != null ? ingredientiExtraIds : new ArrayList<>()));

        if (percentualeSconto != null && percentualeSconto > 0) {
            Sconto sconto = new Sconto();
            sconto.setPercentuale(Math.round(percentualeSconto));
            scontoService.creaSconto(sconto);
            pizza.setScontoApplicato(sconto);
        } else {
            pizza.setScontoApplicato(null);
        }

        Menu menu = menuService.findFirstMenu();
        if (menu != null) {
            pizza.setMenu(menu);
            pizzaService.save(pizza);
            menu.aggiornaPizza(pizza); // Assicurati che questo metodo esista in Menu o MenuService
            menuService.save(menu);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("errorMessage", "Impossibile trovare il menu.");
            return "admin/modifica_pizza";
        }
    }

    @PostMapping("/eliminaPizza/{id}")
    public String eliminaPizza(@PathVariable("id") Long id) {
        Pizza pizza = pizzaService.findById(id);
        if (pizza != null) {
            Menu menu = menuService.findFirstMenu();
            if (menu != null) {
                menu.getPizze().remove(pizza);
                menuService.save(menu);
            }
            pizzaService.delete(pizza);
        }
        return "redirect:/admin/dashboard";
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
            return "redirect:/admin/dashboard";
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

    @PostMapping("/eliminaIngrediente/{id}")
    public String eliminaIngrediente(@PathVariable("id") Long id) {
        Ingrediente ingrediente = ingredienteService.findById(id);
        if (ingrediente != null) {
            // Rimuovi l'ingrediente dalle pizze che lo contengono come ingrediente base
            List<Pizza> pizzeConIngredienteBase = pizzaService.findAll().stream()
                    .filter(p -> p.getIngredientiBase().contains(ingrediente))
                    .toList();
            for (Pizza pizza : pizzeConIngredienteBase) {
                pizza.getIngredientiBase().remove(ingrediente);
                pizzaService.save(pizza);
            }

            // Rimuovi l'ingrediente dalle pizze che lo contengono come ingrediente extra
            List<Pizza> pizzeConIngredienteExtra = pizzaService.findAll().stream()
                    .filter(p -> p.getIngredientiExtra().contains(ingrediente))
                    .toList();
            for (Pizza pizza : pizzeConIngredienteExtra) {
                pizza.getIngredientiExtra().remove(ingrediente);
                pizzaService.save(pizza);
            }

            ingredienteService.deleteById(id); // Elimina l'ingrediente dal database
        }
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
            model.addAttribute("bevanda", bevandaDaModificareOptional.get());
            return "admin/modifica_bevanda";
        } else {
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

    @PostMapping("/eliminaBevanda/{id}")
    public String eliminaBevanda(@PathVariable("id") Long id) {
        Optional<Bevanda> bevandaOptional = bevandaService.findById(id);
        if (bevandaOptional.isPresent()) {
            Bevanda bevanda = bevandaOptional.get();
            Menu menu = menuService.findFirstMenu();
            if (menu != null) {
                menu.getBevande().remove(bevanda);
                menuService.save(menu);
            }
            bevandaService.delete(bevanda);
        }
        return "redirect:/admin/dashboard";
    }
}