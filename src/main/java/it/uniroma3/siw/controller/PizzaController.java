package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Pizzeria;
import it.uniroma3.siw.repository.PizzaRepository;
import it.uniroma3.siw.service.BevandaService;
import it.uniroma3.siw.service.IngredienteService;
import it.uniroma3.siw.service.PizzeriaService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional; // Import per Optional

@Controller
public class PizzaController {

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private BevandaService bevandaService;

    @Autowired
    private IngredienteService ingredienteService;

    @Autowired
    private PizzeriaService pizzeriaService; // Autowired del servizio Pizzeria (corretto)

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<Pizza> pizzeClassiche = pizzaRepository.findAll();
        List<Bevanda> bevande = bevandaService.findAll();
        List<Ingrediente> ingredientiExtra = ingredienteService.findAll();
        // Recupera la pizzeria usando il PizzeriaService
        Optional<Pizzeria> pizzeriaOptional = pizzeriaService.findById(1L);
        pizzeriaOptional.ifPresent(pizzeriaInfo -> model.addAttribute("pizzeria", pizzeriaInfo));
        model.addAttribute("pizzeClassiche", pizzeClassiche);
        model.addAttribute("bevande", bevande);
        model.addAttribute("ingredientiExtra", ingredientiExtra);
        return "index";
    }

    @GetMapping("/pizze_scontate")
    public String showTutteLePizze(Model model) {
        List<Pizza> tutteLePizze = pizzaRepository.findAll();
        List<Bevanda> bevande = bevandaService.findAll();
        List<Ingrediente> ingredientiExtra = ingredienteService.findAll();
        // Recupera la pizzeria usando il PizzeriaService
        Optional<Pizzeria> pizzeriaOptional = pizzeriaService.findById(1L);
        pizzeriaOptional.ifPresent(pizzeriaInfo -> model.addAttribute("pizzeria", pizzeriaInfo));
        model.addAttribute("pizzeScontate", tutteLePizze);
        model.addAttribute("bevande", bevande);
        model.addAttribute("ingredientiExtra", ingredientiExtra);
        return "pizze_scontate";
    }
}