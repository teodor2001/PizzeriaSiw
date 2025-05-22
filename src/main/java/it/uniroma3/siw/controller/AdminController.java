package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.model.Carrello;
import it.uniroma3.siw.model.ElementoCarrello;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Menu;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Pizzeria;
import it.uniroma3.siw.model.Sconto;
import it.uniroma3.siw.repository.CarrelloRepository;
import it.uniroma3.siw.repository.ElementoCarrelloRepository;
import it.uniroma3.siw.service.BevandaService;
import it.uniroma3.siw.service.IngredienteService;
import it.uniroma3.siw.service.MenuService;
import it.uniroma3.siw.service.PizzaService;
import it.uniroma3.siw.service.PizzeriaService;
import it.uniroma3.siw.service.ScontoService;
import it.uniroma3.siw.service.S3Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Autowired
    private ElementoCarrelloRepository elementoCarrelloRepository;

    @Autowired
    private CarrelloRepository carrelloRepository;

    @Autowired
    private S3Service s3Service;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Pizza> pizze = pizzaService.findAll();
        Set<Ingrediente> tuttiGliIngredientiAttivi = ingredienteService.findAll();
        for(Pizza pizza : pizze) {
            pizza.setIngredientiExtraDisponibili(tuttiGliIngredientiAttivi);
        }
        model.addAttribute("pizze", pizze);
        model.addAttribute("ingredienti", tuttiGliIngredientiAttivi);
        model.addAttribute("bevande", bevandaService.findAll());
        Pizzeria pizzeria = pizzeriaService.getOrCreateDefaultPizzeria();
        model.addAttribute("pizzeria", pizzeria);
        return "admin/dashboard";
    }

    @GetMapping("/aggiungiPizza")
    public String aggiungiPizzaForm(Model model) {
        model.addAttribute("pizza", new Pizza());
        model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
        return "admin/aggiungi_pizza";
    }

    @PostMapping("/salvaPizza")
    public String salvaPizza(@Valid @ModelAttribute Pizza pizza, BindingResult bindingResult,
                             @RequestParam(value = "ingredientiBaseIds", required = false) List<Long> ingredientiBaseIds,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam(value = "percentualeSconto", required = false) Integer percentualeSconto, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
            return "admin/aggiungi_pizza";
        }

        Set<String> nomiDegliIngredientiBaseSelezionati = new HashSet<>();
        if (ingredientiBaseIds != null && !ingredientiBaseIds.isEmpty()) {
            Set<Ingrediente> ingredientiSelezionatiOggetti = ingredienteService.findAllById(ingredientiBaseIds);
            for (Ingrediente ing : ingredientiSelezionatiOggetti) {
                nomiDegliIngredientiBaseSelezionati.add(ing.getNome());
            }
        }
        pizza.setNomiIngredientiBase(nomiDegliIngredientiBaseSelezionati);

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
                String s3FileUrl = s3Service.uploadFile("pizze", imageFile.getOriginalFilename(), imageFile);
                pizza.setImageUrl(s3FileUrl);
            } catch (IOException e) {
                bindingResult.rejectValue("imageUrl", "uploadError", "Errore upload immagine: " + e.getMessage());
                model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
                return "admin/aggiungi_pizza";
            }
        } else {
            pizza.setImageUrl(null); 
        }

        Menu menu = menuService.getOrCreateDefaultMenu();
        pizza.setMenu(menu);
        pizzaService.save(pizza);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/modificaPizza/{id}")
    public String modificaPizzaForm(@PathVariable Long id, Model model) {
        Pizza pizzaDaModificare = pizzaService.findById(id);
        if (pizzaDaModificare != null) {
            model.addAttribute("pizza", pizzaDaModificare);
            model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
            if (pizzaDaModificare.getScontoApplicato() != null) {
                model.addAttribute("percentualeScontoAttuale", pizzaDaModificare.getScontoApplicato().getPercentuale());
            } else {
                model.addAttribute("percentualeScontoAttuale", 0);
            }
            return "admin/modifica_pizza";
        } else {
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/salvaModifichePizza")
    public String salvaModifichePizza(@Valid @ModelAttribute("pizza") Pizza pizzaModificata, BindingResult bindingResult,
                                      @RequestParam(value = "ingredientiBaseIds", required = false) List<Long> ingredientiBaseIds,
                                      @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                                      @RequestParam(value= "percentualeSconto", required = false) Integer percentualeSconto, Model model) {

        Pizza pizzaOriginale = pizzaService.findById(pizzaModificata.getIdPizza());
        if (pizzaOriginale == null) {
            return "redirect:/admin/dashboard";
        }

        if (bindingResult.hasFieldErrors("nome") || bindingResult.hasFieldErrors("prezzoBase")) {
            model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
            pizzaModificata.setImageUrl(pizzaOriginale.getImageUrl()); 
             if (percentualeSconto != null) {
                model.addAttribute("percentualeScontoAttuale", percentualeSconto);
            } else if (pizzaOriginale.getScontoApplicato() != null) {
                model.addAttribute("percentualeScontoAttuale", pizzaOriginale.getScontoApplicato().getPercentuale());
            } else {
                model.addAttribute("percentualeScontoAttuale", 0);
            }
            return "admin/modifica_pizza";
        }
        
        pizzaOriginale.setNome(pizzaModificata.getNome());
        pizzaOriginale.setPrezzoBase(pizzaModificata.getPrezzoBase()); 

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String newS3FileUrl = s3Service.uploadFile("pizze", imageFile.getOriginalFilename(), imageFile);
                pizzaOriginale.setImageUrl(newS3FileUrl);
            } catch (IOException e) {
                bindingResult.rejectValue("imageUrl", "uploadError", "Errore upload nuova immagine: " + e.getMessage());
                model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
                 if (percentualeSconto != null) {
                    model.addAttribute("percentualeScontoAttuale", percentualeSconto);
                } else if (pizzaOriginale.getScontoApplicato() != null) {
                    model.addAttribute("percentualeScontoAttuale", pizzaOriginale.getScontoApplicato().getPercentuale());
                } else {
                    model.addAttribute("percentualeScontoAttuale", 0);
                }
                return "admin/modifica_pizza";
            }
        }

        Set<String> nuoviNomiIngredientiBase = new HashSet<>();
        if (ingredientiBaseIds != null && !ingredientiBaseIds.isEmpty()) {
            Set<Ingrediente> nuoviIngredientiSelezionatiOggetti = ingredienteService.findAllById(ingredientiBaseIds);
            for (Ingrediente ing : nuoviIngredientiSelezionatiOggetti) {
                nuoviNomiIngredientiBase.add(ing.getNome());
            }
        }
        pizzaOriginale.setNomiIngredientiBase(nuoviNomiIngredientiBase);

        Sconto scontoEsistente = pizzaOriginale.getScontoApplicato();
        if (percentualeSconto != null && percentualeSconto > 0) {
            Sconto scontoDaApplicare;
            if (scontoEsistente != null && scontoEsistente.getPercentuale() == percentualeSconto) {
                scontoDaApplicare = scontoEsistente;
            } else {
                scontoDaApplicare = new Sconto();
                scontoDaApplicare.setPercentuale(percentualeSconto); 
                scontoService.creaSconto(scontoDaApplicare);
            }
            pizzaOriginale.setScontoApplicato(scontoDaApplicare);
        } else {
             pizzaOriginale.setScontoApplicato(null);
        }

        pizzaService.save(pizzaOriginale);
        return "redirect:/admin/dashboard";
    }

    @Transactional
    @PostMapping("/eliminaPizza/{id}")
    public String eliminaPizza(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Pizza pizza = pizzaService.findById(id);
        if (pizza != null) {
            List<ElementoCarrello> elementiConPizza = elementoCarrelloRepository.findByPizzaIdPizza(id);
            for (ElementoCarrello elemento : new ArrayList<>(elementiConPizza)) {
                Carrello carrelloAssociato = elemento.getCarrello();
                if (carrelloAssociato != null) {
                    Carrello managedCarrello = carrelloRepository.findById(carrelloAssociato.getId()).orElse(null);
                    if (managedCarrello != null) {
                        managedCarrello.getElementi().removeIf(ec -> ec.getId().equals(elemento.getId()));
                        carrelloRepository.save(managedCarrello);
                    }
                }
                elementoCarrelloRepository.deleteById(elemento.getId());
            }

            Menu menu = pizza.getMenu();
            if (menu != null) {
                Menu managedMenu = menuService.findById(menu.getId());
                if (managedMenu != null) {
                    managedMenu.getPizze().removeIf(p -> p.getIdPizza().equals(id));
                    menuService.save(managedMenu);
                }
            }

            pizzaService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pizza e riferimenti associati eliminati con successo.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Pizza non trovata.");
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
                               @RequestParam("imageFile") MultipartFile imageFile,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/aggiungi_bevanda";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String s3FileUrl = s3Service.uploadFile("bevande", imageFile.getOriginalFilename(), imageFile);
                bevanda.setImageUrl(s3FileUrl);
            } catch (IOException e) {
                bindingResult.rejectValue("imageUrl", "uploadError", "Errore upload immagine: " + e.getMessage());
                return "admin/aggiungi_bevanda";
            }
        } else {
            bevanda.setImageUrl(null); 
        }

        Menu menu = menuService.getOrCreateDefaultMenu();
        bevanda.setMenu(menu);
        bevandaService.save(bevanda);
        return "redirect:/admin/dashboard";
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
    public String salvaModificheBevanda(@Valid @ModelAttribute("bevanda") Bevanda bevandaModificata,
                                        BindingResult bindingResult,
                                        @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                                        Model model) {

        Bevanda bevandaOriginale = bevandaService.findById(bevandaModificata.getId()).orElse(null);
        if (bevandaOriginale == null) {
            model.addAttribute("errorMessage", "Errore: Bevanda originale non trovata.");
            return "admin/modifica_bevanda"; 
        }

        if (bindingResult.hasErrors()) {
            bevandaModificata.setImageUrl(bevandaOriginale.getImageUrl());
            return "admin/modifica_bevanda";
        }

        bevandaOriginale.setNome(bevandaModificata.getNome());
        bevandaOriginale.setPrezzo(bevandaModificata.getPrezzo()); 
        bevandaOriginale.setQuantità(bevandaModificata.getQuantità()); 

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String newS3FileUrl = s3Service.uploadFile("bevande", imageFile.getOriginalFilename(), imageFile);
                bevandaOriginale.setImageUrl(newS3FileUrl);
            } catch (IOException e) {
                bindingResult.rejectValue("imageUrl", "uploadError", "Errore upload nuova immagine: " + e.getMessage());
                bevandaModificata.setImageUrl(bevandaOriginale.getImageUrl()); 
                return "admin/modifica_bevanda";
            }
        }

        bevandaService.save(bevandaOriginale);
        return "redirect:/admin/dashboard";
    }

    @Transactional
    @PostMapping("/eliminaBevanda/{id}")
    public String eliminaBevanda(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Bevanda> bevandaOptional = bevandaService.findById(id);
        if (bevandaOptional.isPresent()) {
            Bevanda bevanda = bevandaOptional.get();
            List<ElementoCarrello> elementiDaRimuovere = elementoCarrelloRepository.findByBevanda(bevanda);
            for (ElementoCarrello elemento : new ArrayList<>(elementiDaRimuovere)) {
                Carrello carrelloAssociato = elemento.getCarrello();
                if (carrelloAssociato != null) {
                    Carrello managedCarrello = carrelloRepository.findById(carrelloAssociato.getId()).orElse(null);
                    if (managedCarrello != null) {
                        managedCarrello.getElementi().removeIf(ec -> ec.getId().equals(elemento.getId()));
                        carrelloRepository.save(managedCarrello);
                    }
                }
                elementoCarrelloRepository.deleteById(elemento.getId());
            }

            Menu menu = bevanda.getMenu();
            if (menu != null) {
                Menu managedMenu = menuService.findById(menu.getId());
                if (managedMenu != null) {
                    managedMenu.getBevande().removeIf(b -> b.getId().equals(id));
                    menuService.save(managedMenu);
                }
            }

            bevandaService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Bevanda e riferimenti associati eliminati con successo.");
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", "Bevanda non trovata.");
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
                                   BindingResult bindingResult,
                                   @RequestParam("imageFile") MultipartFile imageFile,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/aggiungi_ingrediente";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String s3FileUrl = s3Service.uploadFile("ingredienti", imageFile.getOriginalFilename(), imageFile);
                ingrediente.setImageUrl(s3FileUrl);
            } catch (IOException e) {
                bindingResult.rejectValue("imageUrl", "uploadError", "Errore upload immagine: " + e.getMessage());
                return "admin/aggiungi_ingrediente";
            }
        } else {
            ingrediente.setImageUrl(null); 
        }

        Menu menu = menuService.getOrCreateDefaultMenu(); 
        ingrediente.setMenu(menu); 

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
    public String salvaModificheIngrediente(@Valid @ModelAttribute("ingrediente") Ingrediente ingredienteModificato,
                                            BindingResult bindingResult,
                                            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                                            Model model) {

        Ingrediente ingredienteOriginale = ingredienteService.findById(ingredienteModificato.getId());
        if (ingredienteOriginale == null) {
             model.addAttribute("errorMessage", "Errore: Ingrediente originale non trovato.");
             return "admin/modifica_ingrediente";
        }

        if (bindingResult.hasErrors()) {
            ingredienteModificato.setImageUrl(ingredienteOriginale.getImageUrl());
            return "admin/modifica_ingrediente";
        }

        ingredienteOriginale.setNome(ingredienteModificato.getNome());
        ingredienteOriginale.setPrezzo(ingredienteModificato.getPrezzo()); 
        // ingredienteOriginale.setOrigine(ingredienteModificato.getOrigine()); // Rimosso perché 'origine' non esiste

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String newS3FileUrl = s3Service.uploadFile("ingredienti", imageFile.getOriginalFilename(), imageFile);
                ingredienteOriginale.setImageUrl(newS3FileUrl);
            } catch (IOException e) {
                bindingResult.rejectValue("imageUrl", "uploadError", "Errore upload nuova immagine: " + e.getMessage());
                ingredienteModificato.setImageUrl(ingredienteOriginale.getImageUrl()); 
                return "admin/modifica_ingrediente";
            }
        }

        ingredienteService.save(ingredienteOriginale);
        return "redirect:/admin/dashboard";
    }

    @Transactional
    @PostMapping("/eliminaIngrediente/{id}")
    public String eliminaIngrediente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Ingrediente ingredienteDaEliminare = ingredienteService.findById(id);
        if (ingredienteDaEliminare != null) {
            
            elementoCarrelloRepository.findAll().stream()
                .filter(ec -> ec.getIngredientiExtraSelezionati().contains(ingredienteDaEliminare))
                .forEach(ec -> {
                    ec.getIngredientiExtraSelezionati().remove(ingredienteDaEliminare);
                    ec.calcolaPrezzoUnitario(); 
                    elementoCarrelloRepository.save(ec);
                    Carrello carrello = ec.getCarrello();
                    if (carrello != null) {
                        carrello.setDataUltimaModifica(java.time.LocalDateTime.now());
                        carrelloRepository.save(carrello);
                    }
                });
            
            ingredienteService.deleteById(id); 
            redirectAttributes.addFlashAttribute("successMessage", "Ingrediente eliminato e riferimenti aggiornati.");
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", "Ingrediente non trovato.");
        }
        return "redirect:/admin/dashboard";
    }
}