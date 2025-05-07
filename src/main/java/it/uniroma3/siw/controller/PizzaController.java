package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Sconto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PizzaController {

    // SOLO DI ESEMPIO, SONO DA IMPLEMENTARE I SERVICE
    private List<Pizza> getPizzeDiEsempio() {
        return Arrays.asList(
                new Pizza("Margherita", "Pomodoro, mozzarella, basilico", 7.5,
                        Arrays.asList(
                                new Ingrediente("pomodoro", 0.0),
                                new Ingrediente("mozzarella", 0.0),
                                new Ingrediente("basilico", 0.0)
                        )),
                new Pizza("Marinara", "Pomodoro, aglio, origano, olio extravergine", 6.0,
                        Arrays.asList(
                                new Ingrediente("pomodoro", 0.0),
                                new Ingrediente("aglio", 0.0),
                                new Ingrediente("origano", 0.0),
                                new Ingrediente("olio extravergine", 0.0)
                        )),
                new Pizza("Napoli", "Pomodoro, mozzarella, acciughe, capperi, origano", 8.5,
                        Arrays.asList(
                                new Ingrediente("pomodoro", 0.0),
                                new Ingrediente("mozzarella", 0.0),
                                new Ingrediente("acciughe", 1.0),
                                new Ingrediente("capperi", 0.5),
                                new Ingrediente("origano", 0.0)
                        )),
                new Pizza("Diavola", "Pomodoro, mozzarella, salame piccante", 9.0,
                        Arrays.asList(
                                new Ingrediente("pomodoro", 0.0),
                                new Ingrediente("mozzarella", 0.0),
                                new Ingrediente("salame piccante", 1.5)
                        )),
                new Pizza("Quattro Formaggi", "Mozzarella, gorgonzola, fontina, parmigiano", 10.0,
                        Arrays.asList(
                                new Ingrediente("mozzarella", 0.0),
                                new Ingrediente("gorgonzola", 1.2),
                                new Ingrediente("fontina", 1.0),
                                new Ingrediente("parmigiano", 1.8)
                        ))
        );
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<Pizza> pizzeClassiche = getPizzeDiEsempio().stream().collect(Collectors.toList());
        model.addAttribute("pizzeClassiche", pizzeClassiche);
        return "index";
    }

    @GetMapping("/pizze_scontate")
    public String showPizzeScontate(Model model) {
        Sconto scontoQuindiciPercento = new Sconto(15.0);

        List<Pizza> pizzeScontate = getPizzeDiEsempio().stream().map(pizza -> {
            Pizza pizzaScontata = new Pizza(pizza.getNome(), pizza.getDescrizione(), pizza.getPrezzoBase(),
                    pizza.getIngredientiBase());
            pizzaScontata.setScontoApplicato(scontoQuindiciPercento);
            return pizzaScontata;
        }).collect(Collectors.toList());
        model.addAttribute("pizzeScontate", pizzeScontate);
        return "pizze_scontate";
    }
}