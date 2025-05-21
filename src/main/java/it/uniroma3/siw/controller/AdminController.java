package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Menu;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Pizzeria;
import it.uniroma3.siw.model.Sconto;
import it.uniroma3.siw.service.BevandaService;
import it.uniroma3.siw.service.IngredienteService;
import it.uniroma3.siw.service.MenuService;
import it.uniroma3.siw.service.PizzaService;
import it.uniroma3.siw.service.PizzeriaService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
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
    
    @Autowired
    private PizzeriaService pizzeriaService;
    
    @Value("${upload.directory}")
    private String uploadDirectory;
    

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("pizze", pizzaService.findAll());
        model.addAttribute("ingredienti", ingredienteService.findAll());
        model.addAttribute("bevande", bevandaService.findAll());
        Optional<Pizzeria> pizzeriaOptional = pizzeriaService.findById(1L);
        pizzeriaOptional.ifPresent(pizzeria -> model.addAttribute("pizzeria", pizzeria));
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
    public String salvaPizza(@Valid @ModelAttribute Pizza pizza, BindingResult bindingResult,
                             @RequestParam String ingredientiBaseText,
                             @RequestParam(required = false, defaultValue = "") String ingredientiExtraText,
                             @RequestParam MultipartFile imageFile,
                             @RequestParam(required = false) Integer percentualeSconto, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ingredientiExtraDisponibili", ingredienteService.findAll());
            model.addAttribute("ingredientiBaseDisponibili", ingredienteService.findAll());
            return "admin/aggiungi_pizza";
        }

        List<Ingrediente> ingredientiBaseList = new ArrayList<>();
        Arrays.stream(ingredientiBaseText.split(","))
                .map(String::trim)
                .filter(nomeIngrediente -> !nomeIngrediente.isEmpty())
                .forEach(nomeIngrediente -> {
                    Ingrediente ingrediente = new Ingrediente();
                    ingrediente.setNome(nomeIngrediente);
                    ingrediente.setPrezzo(0.0);
                    ingredienteService.save(ingrediente);
                    ingredientiBaseList.add(ingrediente);
                });
        pizza.setIngredientiBase(ingredientiBaseList);
        List<Ingrediente> ingredientiExtraList = new ArrayList<>();
        Arrays.stream(ingredientiExtraText.split(","))
                .map(String::trim)
                .filter(nomeIngrediente -> !nomeIngrediente.isEmpty())
                .forEach(nomeIngrediente -> {
                    Ingrediente ingrediente = new Ingrediente();
                    ingrediente.setNome(nomeIngrediente);
                    ingrediente.setPrezzo(0.0); 
                    ingredienteService.save(ingrediente);
                    ingredientiExtraList.add(ingrediente);
                });
        pizza.setIngredientiExtra(ingredientiExtraList);

        if (percentualeSconto != null && percentualeSconto > 0) {
            Sconto sconto = new Sconto();
            sconto.setPercentuale(percentualeSconto);
            scontoService.creaSconto(sconto);
            pizza.setScontoApplicato(sconto);
        } else {
            pizza.setScontoApplicato(null);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = pizza.getNome().replaceAll("\\s+", "") + ".jpg";
                Path filePath = Paths.get(uploadDirectory, fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                pizza.setImageUrl("/images/pizze/" + fileName);
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Errore durante il caricamento dell'immagine.");
                model.addAttribute("ingredientiExtraDisponibili", ingredienteService.findAll());
                model.addAttribute("ingredientiBaseDisponibili", ingredienteService.findAll());
                return "admin/aggiungi_pizza";
            }
        } else {
            pizza.setImageUrl("/images/pizze/Margherita.jpg");
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
    public String modificaPizzaForm(@PathVariable Long id, Model model) {
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
    public String salvaModifichePizza(@Valid @ModelAttribute Pizza pizza, BindingResult bindingResult,
                                     @RequestParam(value = "ingredientiBase", required = false) List<Long> ingredientiBaseIds,
                                     @RequestParam(value = "ingredientiExtra", required = false) List<Long> ingredientiExtraIds,
                                     @RequestParam(required = false) Float percentualeSconto, Model model) {

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
            menu.aggiornaPizza(pizza);
            menuService.save(menu);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("errorMessage", "Impossibile trovare il menu.");
            return "admin/modifica_pizza";
        }
    }

    @PostMapping("/eliminaPizza/{id}")
    public String eliminaPizza(@PathVariable Long id) {
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
    public String salvaIngrediente(@Valid @ModelAttribute Ingrediente ingrediente,
                                    BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/aggiungi_ingrediente";
        }
        ingredienteService.save(ingrediente);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/modificaIngrediente/{id}")
    public String modificaIngredienteForm(@PathVariable Long id, Model model) {
        Ingrediente ingredienteDaModificare = ingredienteService.findById(id);
        if (ingredienteDaModificare != null) {
            model.addAttribute("ingrediente", ingredienteDaModificare);
            return "admin/modifica_ingrediente";
        } else {
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/salvaModificheIngrediente")
    public String salvaModificheIngrediente(@Valid @ModelAttribute Ingrediente ingrediente,
                                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/modifica_ingrediente";
        }
        ingredienteService.save(ingrediente);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/eliminaIngrediente/{id}")
    public String eliminaIngrediente(@PathVariable Long id) {
        Ingrediente ingrediente = ingredienteService.findById(id);
        if (ingrediente != null) {
            List<Pizza> pizzeConIngredienteBase = pizzaService.findAll().stream()
                    .filter(p -> p.getIngredientiBase().contains(ingrediente))
                    .toList();
            for (Pizza pizza : pizzeConIngredienteBase) {
                pizza.getIngredientiBase().remove(ingrediente);
                pizzaService.save(pizza);
            }

            List<Pizza> pizzeConIngredienteExtra = pizzaService.findAll().stream()
                    .filter(p -> p.getIngredientiExtra().contains(ingrediente))
                    .toList();
            for (Pizza pizza : pizzeConIngredienteExtra) {
                pizza.getIngredientiExtra().remove(ingrediente);
                pizzaService.save(pizza);
            }

            ingredienteService.deleteById(id);
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/aggiungiBevanda")
    public String aggiungiBevandaForm(Model model) {
        model.addAttribute("bevanda", new Bevanda());
        return "admin/aggiungi_bevanda";
    }

    @PostMapping("/salvaBevanda")
    public String salvaBevanda(@Valid @ModelAttribute Bevanda bevanda, BindingResult bindingResult,
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
    public String modificaBevandaForm(@PathVariable Long id, Model model) {
        Optional<Bevanda> bevandaDaModificareOptional = bevandaService.findById(id);
        if (bevandaDaModificareOptional.isPresent()) {
            model.addAttribute("bevanda", bevandaDaModificareOptional.get());
            return "admin/modifica_bevanda";
        } else {
            model.addAttribute("errorMessage", "Bevanda non trovata con ID: " + id);
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/salvaModificheBevanda")
    public String salvaModificheBevanda(@Valid @ModelAttribute Bevanda bevanda,
                                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/modifica_bevanda";
        }
        bevandaService.save(bevanda);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/eliminaBevanda/{id}")
    public String eliminaBevanda(@PathVariable Long id) {
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