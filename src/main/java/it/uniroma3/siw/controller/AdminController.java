package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Menu;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.service.BevandaService;
import it.uniroma3.siw.service.IngredienteService;
import it.uniroma3.siw.service.MenuService;
import it.uniroma3.siw.service.PizzaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Pizza> tutteLePizze = pizzaService.findAll();
        model.addAttribute("pizze", tutteLePizze);

        List<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();
        model.addAttribute("ingredienti", tuttiGliIngredienti);

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
    public String salvaPizza(@Valid @ModelAttribute("pizza") Pizza pizza,
                             BindingResult bindingResult,
                             @RequestParam(value = "ingredientiBase", required = false) List<Long> ingredientiBaseIds,
                             @RequestParam(value = "ingredientiExtra", required = false) List<Long> ingredientiExtraIds,
                             Model model) {

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
            pizza.setIngredientiExtra(ingredientiExtra);
        } else {
            pizza.setIngredientiExtra(new ArrayList<>());
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
    
    @GetMapping("/aggiungiIngrediente")
    public String aggiungiIngredienteForm(Model model) {
        model.addAttribute("ingrediente", new Ingrediente());
        return "admin/aggiungi_ingrediente";
    }

    @PostMapping("/salvaIngrediente")
    public String salvaIngrediente(@Valid @ModelAttribute("ingrediente") Ingrediente ingrediente,
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/aggiungi_ingrediente";
        }
        ingredienteService.save(ingrediente);
        return "redirect:/admin/dashboard";
    }
    
    @GetMapping("/aggiungiBevanda")
    public String aggiungiBevandaForm(Model model) {
        model.addAttribute("bevanda", new Bevanda());
        return "admin/aggiungi_bevanda";
    }

    @PostMapping("/salvaBevanda")
    public String salvaBevanda(@Valid @ModelAttribute("bevanda") Bevanda bevanda,
                                       BindingResult bindingResult,
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
}