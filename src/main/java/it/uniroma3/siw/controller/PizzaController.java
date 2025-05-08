package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.repository.PizzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PizzaController {

    @Autowired
    private PizzaRepository pizzaRepository;

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<Pizza> pizzeClassiche = pizzaRepository.findAll();
        model.addAttribute("pizzeClassiche", pizzeClassiche);
        return "index";
    }

    @GetMapping("/pizze_scontate")
    public String showPizzeScontate(Model model) {
        List<Pizza> pizzeScontate = pizzaRepository.findByScontoApplicatoIsNotNull();
        model.addAttribute("pizzeScontate", pizzeScontate);
        return "pizze_scontate";
    }
}