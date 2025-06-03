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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Value("${upload.directory.pizze}")
    private String uploadDirectoryPizze;

    @Value("${upload.directory.bevande}")
    private String uploadDirectoryBevande;

    @Value("${upload.directory.ingredienti}")
    private String uploadDirectoryIngredienti;

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "jpg"; // Estensione di default se non trovata
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private void deleteImageFile(String directory, String imageUrl, String defaultImageUrlPrefix) {
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith(defaultImageUrlPrefix)) {
            try {
                String filenameWithPrefix = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path imagePath = Paths.get(directory, filenameWithPrefix);
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Error deleting image file " + imageUrl + ": " + e.getMessage());
            }
        }
    }

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

        Set<Ingrediente> ingredientiBaseSelezionati = new HashSet<>();
        if (ingredientiBaseIds != null && !ingredientiBaseIds.isEmpty()) {
            ingredientiBaseSelezionati = ingredienteService.findAllById(ingredientiBaseIds);
        }
        pizza.setIngredientiBase(ingredientiBaseSelezionati);

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
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName);
                String baseName = pizza.getNome() != null ? pizza.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "pizza";
                String uniqueFileName = baseName + "_" + java.util.UUID.randomUUID().toString() + "." + fileExtension;

                Path filePath = Paths.get(uploadDirectoryPizze, uniqueFileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                pizza.setImageUrl("/images/pizze/" + uniqueFileName);
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Errore durante il caricamento dell'immagine: " + e.getMessage());
                model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
                return "admin/aggiungi_pizza";
            }
        } else {
            pizza.setImageUrl("/images/pizze/Default.jpg");
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
                model.addAttribute("percentualeScontoAttuale", 0.0f);
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
                                      @RequestParam(required = false) Float percentualeSconto, Model model) {

        Pizza pizzaOriginale = pizzaService.findById(pizzaModificata.getIdPizza());
        if (pizzaOriginale == null) {
            model.addAttribute("errorMessage", "Errore: Pizza originale non trovata per ID: " + pizzaModificata.getIdPizza());
            model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
            model.addAttribute("pizza", pizzaModificata);
             if (percentualeSconto != null) {
                model.addAttribute("percentualeScontoAttuale", percentualeSconto);
            } else {
                model.addAttribute("percentualeScontoAttuale", 0.0f);
            }
            return "admin/modifica_pizza";
        }

        if (bindingResult.hasFieldErrors("nome") || bindingResult.hasFieldErrors("prezzoBase")) {
            model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
            if (imageFile == null || imageFile.isEmpty()) {
                pizzaModificata.setImageUrl(pizzaOriginale.getImageUrl());
            }
            if (percentualeSconto != null) {
                 model.addAttribute("percentualeScontoAttuale", percentualeSconto);
            } else if (pizzaOriginale.getScontoApplicato() != null) {
                 model.addAttribute("percentualeScontoAttuale", pizzaOriginale.getScontoApplicato().getPercentuale());
            } else {
                 model.addAttribute("percentualeScontoAttuale", 0.0f);
            }
            return "admin/modifica_pizza";
        }

        String oldImageUrl = pizzaOriginale.getImageUrl();
        String newSanitizedName = pizzaModificata.getNome() != null ? pizzaModificata.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "pizza_sconosciuta";

        pizzaOriginale.setNome(pizzaModificata.getNome());
        pizzaOriginale.setPrezzoBase(pizzaModificata.getPrezzoBase());

        if (imageFile != null && !imageFile.isEmpty()) {
            deleteImageFile(uploadDirectoryPizze, oldImageUrl, "/images/pizze/Default.jpg");
            try {
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName);
                String baseName = pizzaModificata.getNome() != null ? pizzaModificata.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "pizza";
                String uniqueFileName = baseName + "_" + java.util.UUID.randomUUID().toString() + "." + fileExtension;
                
                Path filePath = Paths.get(uploadDirectoryPizze, uniqueFileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                pizzaOriginale.setImageUrl("/images/pizze/" + uniqueFileName);
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Errore durante il caricamento della nuova immagine: " + e.getMessage());
                model.addAttribute("tuttiGliIngredienti", ingredienteService.findAll());
                pizzaModificata.setImageUrl(oldImageUrl);
                model.addAttribute("pizza", pizzaModificata);
                 if (percentualeSconto != null) {
                    model.addAttribute("percentualeScontoAttuale", percentualeSconto);
                } else if (pizzaOriginale.getScontoApplicato() != null) {
                    model.addAttribute("percentualeScontoAttuale", pizzaOriginale.getScontoApplicato().getPercentuale());
                } else {
                     model.addAttribute("percentualeScontoAttuale", 0.0f);
                }
                return "admin/modifica_pizza";
            }
        } else {
             // Se non viene caricata una nuova immagine, gestisci la rinomina se il nome della pizza cambia
             if (!newSanitizedName.equals(pizzaOriginale.getNome().replaceAll("[^a-zA-Z0-9.-]", "_")) &&
                oldImageUrl != null && !oldImageUrl.equals("/images/pizze/Default.jpg")) {
                try {
                    String oldFileNameOnly = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                    
                    String uuidPartAndExtension = "";
                    int lastDot = oldFileNameOnly.lastIndexOf(".");
                    if (lastDot > 0) {
                        int lastUnderscore = oldFileNameOnly.lastIndexOf("_", lastDot);
                        if (lastUnderscore > 0 && lastUnderscore < lastDot) {
                            uuidPartAndExtension = oldFileNameOnly.substring(lastUnderscore); 
                        } else {
                            uuidPartAndExtension = oldFileNameOnly.substring(lastDot);
                        }
                    } else {
                         uuidPartAndExtension = ".jpg"; 
                    }

                    String newFileNameOnly = newSanitizedName + uuidPartAndExtension;

                    Path oldPath = Paths.get(uploadDirectoryPizze, oldFileNameOnly);
                    Path newPath = Paths.get(uploadDirectoryPizze, newFileNameOnly);

                    if (Files.exists(oldPath)) {
                         Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                         pizzaOriginale.setImageUrl("/images/pizze/" + newFileNameOnly);
                    } else {
                         pizzaOriginale.setImageUrl(pizzaModificata.getImageUrl());
                    }
                } catch (IOException e) {
                     System.err.println("Errore durante la rinomina dell'immagine: " + e.getMessage());
                     pizzaOriginale.setImageUrl(pizzaModificata.getImageUrl());
                }
            } else {
                 pizzaOriginale.setImageUrl(pizzaModificata.getImageUrl());
            }
        }

        Set<Ingrediente> ingredientiBaseSelezionati = new HashSet<>();
        if (ingredientiBaseIds != null && !ingredientiBaseIds.isEmpty()) {
            ingredientiBaseSelezionati = ingredienteService.findAllById(ingredientiBaseIds);
        }
        pizzaOriginale.setIngredientiBase(ingredientiBaseSelezionati);

        Sconto scontoEsistente = pizzaOriginale.getScontoApplicato();
        if (percentualeSconto != null && percentualeSconto > 0) {
            Sconto scontoDaApplicare;
            if (scontoEsistente != null && scontoEsistente.getPercentuale() == percentualeSconto) {
                scontoDaApplicare = scontoEsistente;
            } else {
                scontoDaApplicare = new Sconto();
                scontoDaApplicare.setPercentuale(Math.round(percentualeSconto));
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
            deleteImageFile(uploadDirectoryPizze, pizza.getImageUrl(), "/images/pizze/Default.jpg");

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
                    boolean removed = managedMenu.getPizze().removeIf(p -> p.getIdPizza().equals(id));
                    if (removed) {
                        menuService.save(managedMenu);
                    }
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
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName);
                String baseName = bevanda.getNome() != null ? bevanda.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "bevanda";
                String uniqueFileName = baseName + "_" + java.util.UUID.randomUUID().toString() + "." + fileExtension;

                Path filePath = Paths.get(uploadDirectoryBevande, uniqueFileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                bevanda.setImageUrl("/images/bevande/" + uniqueFileName);
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Errore durante il caricamento dell'immagine della bevanda: " + e.getMessage());
                return "admin/aggiungi_bevanda";
            }
        } else {
            bevanda.setImageUrl("/images/bevande/DefaultBevanda.jpg");
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
            if (imageFile == null || imageFile.isEmpty()) {
                bevandaModificata.setImageUrl(bevandaOriginale.getImageUrl());
            }
            return "admin/modifica_bevanda";
        }

        String oldImageUrl = bevandaOriginale.getImageUrl();
        String newSanitizedName = bevandaModificata.getNome() != null ? bevandaModificata.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "bevanda_sconosciuta";

        bevandaOriginale.setNome(bevandaModificata.getNome());
        bevandaOriginale.setPrezzo(bevandaModificata.getPrezzo());
        bevandaOriginale.setQuantità(bevandaModificata.getQuantità());

        if (imageFile != null && !imageFile.isEmpty()) {
            deleteImageFile(uploadDirectoryBevande, oldImageUrl, "/images/bevande/DefaultBevanda.jpg");
            try {
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName);
                String baseName = bevandaModificata.getNome() != null ? bevandaModificata.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "bevanda";
                String uniqueFileName = baseName + "_" + java.util.UUID.randomUUID().toString() + "." + fileExtension;
                
                Path filePath = Paths.get(uploadDirectoryBevande, uniqueFileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                bevandaOriginale.setImageUrl("/images/bevande/" + uniqueFileName);
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Errore durante il caricamento della nuova immagine: " + e.getMessage());
                bevandaModificata.setImageUrl(oldImageUrl);
                return "admin/modifica_bevanda";
            }
        } else {
            // Se non viene caricata una nuova immagine, gestisci la rinomina se il nome della bevanda cambia
            if (!newSanitizedName.equals(bevandaOriginale.getNome().replaceAll("[^a-zA-Z0-9.-]", "_")) &&
                oldImageUrl != null && !oldImageUrl.equals("/images/bevande/DefaultBevanda.jpg")) {
                try {
                    String oldFileNameOnly = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                    
                    String uuidPartAndExtension = "";
                    int lastDot = oldFileNameOnly.lastIndexOf(".");
                    if (lastDot > 0) {
                        int lastUnderscore = oldFileNameOnly.lastIndexOf("_", lastDot);
                        if (lastUnderscore > 0 && lastUnderscore < lastDot) {
                            uuidPartAndExtension = oldFileNameOnly.substring(lastUnderscore);
                        } else {
                            uuidPartAndExtension = oldFileNameOnly.substring(lastDot);
                        }
                    } else {
                         uuidPartAndExtension = ".jpg";
                    }

                    String newFileNameOnly = newSanitizedName + uuidPartAndExtension;

                    Path oldPath = Paths.get(uploadDirectoryBevande, oldFileNameOnly);
                    Path newPath = Paths.get(uploadDirectoryBevande, newFileNameOnly);

                    if (Files.exists(oldPath)) {
                         Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                         bevandaOriginale.setImageUrl("/images/bevande/" + newFileNameOnly);
                    } else {
                        bevandaOriginale.setImageUrl(bevandaModificata.getImageUrl());
                    }
                } catch (IOException e) {
                     System.err.println("Errore durante la rinomina dell'immagine della bevanda: " + e.getMessage());
                     bevandaOriginale.setImageUrl(bevandaModificata.getImageUrl());
                }
            } else {
                 bevandaOriginale.setImageUrl(bevandaModificata.getImageUrl());
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

            deleteImageFile(uploadDirectoryBevande, bevanda.getImageUrl(), "/images/bevande/DefaultBevanda.jpg");

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
                    boolean removed = managedMenu.getBevande().removeIf(b -> b.getId().equals(id));
                    if(removed){
                        menuService.save(managedMenu);
                    }
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
                // MODIFICA QUI: USARE LA LOGICA DI NOME FILE CON UUID
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName);
                String baseName = ingrediente.getNome() != null ? ingrediente.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "ingrediente";
                // Aggiungi UUID per unicità
                String uniqueFileName = baseName + "_" + java.util.UUID.randomUUID().toString() + "." + fileExtension;

                Path filePath = Paths.get(uploadDirectoryIngredienti, uniqueFileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                ingrediente.setImageUrl("/images/ingredienti/" + uniqueFileName); // Salva il nome unico nel DB
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Errore durante il caricamento dell'immagine dell'ingrediente: " + e.getMessage());
                return "admin/aggiungi_ingrediente";
            }
        } else {
            ingrediente.setImageUrl("/images/ingredienti/DefaultIngrediente.jpg"); // Immagine di default
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
            if (imageFile == null || imageFile.isEmpty()) {
                 ingredienteModificato.setImageUrl(ingredienteOriginale.getImageUrl());
            }
            return "admin/modifica_ingrediente";
        }

        String oldImageUrl = ingredienteOriginale.getImageUrl();
        String newSanitizedName = ingredienteModificato.getNome() != null ? ingredienteModificato.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "ingrediente_sconosciuto";

        ingredienteOriginale.setNome(ingredienteModificato.getNome());
        ingredienteOriginale.setPrezzo(ingredienteModificato.getPrezzo());

        if (imageFile != null && !imageFile.isEmpty()) {
            deleteImageFile(uploadDirectoryIngredienti, oldImageUrl, "/images/ingredienti/DefaultIngrediente.jpg");
            try {
                // MODIFICA QUI: USARE LA LOGICA DI NOME FILE CON UUID
                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName);
                String baseName = ingredienteModificato.getNome() != null ? ingredienteModificato.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") : "ingrediente";
                String uniqueFileName = baseName + "_" + java.util.UUID.randomUUID().toString() + "." + fileExtension;

                Path filePath = Paths.get(uploadDirectoryIngredienti, uniqueFileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                ingredienteOriginale.setImageUrl("/images/ingredienti/" + uniqueFileName);
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Errore durante il caricamento della nuova immagine: " + e.getMessage());
                ingredienteModificato.setImageUrl(oldImageUrl);
                return "admin/modifica_ingrediente";
            }
        } else {
            // Se non viene caricata una nuova immagine, gestisci la rinomina se il nome dell'ingrediente cambia
            if (!newSanitizedName.equals(ingredienteOriginale.getNome().replaceAll("[^a-zA-Z0-9.-]", "_")) &&
                oldImageUrl != null && !oldImageUrl.equals("/images/ingredienti/DefaultIngrediente.jpg")) {
                try {
                    String oldFileNameOnly = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                    
                    String uuidPartAndExtension = "";
                    int lastDot = oldFileNameOnly.lastIndexOf(".");
                    if (lastDot > 0) {
                        int lastUnderscore = oldFileNameOnly.lastIndexOf("_", lastDot);
                        if (lastUnderscore > 0 && lastUnderscore < lastDot) {
                            uuidPartAndExtension = oldFileNameOnly.substring(lastUnderscore);
                        } else {
                            // Questo caso si applica se il vecchio nome non aveva UUID, ma solo nome.estensione
                            uuidPartAndExtension = oldFileNameOnly.substring(lastDot); 
                        }
                    } else {
                         uuidPartAndExtension = ".jpg"; // Fallback se non ha estensione
                    }

                    String newFileNameOnly = newSanitizedName + uuidPartAndExtension;

                    Path oldPath = Paths.get(uploadDirectoryIngredienti, oldFileNameOnly);
                    Path newPath = Paths.get(uploadDirectoryIngredienti, newFileNameOnly);

                    if (Files.exists(oldPath)) {
                         Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                         ingredienteOriginale.setImageUrl("/images/ingredienti/" + newFileNameOnly);
                    } else {
                        ingredienteOriginale.setImageUrl(ingredienteModificato.getImageUrl());
                    }
                } catch (IOException e) {
                     System.err.println("Errore durante la rinomina dell'immagine dell'ingrediente: " + e.getMessage());
                     ingredienteOriginale.setImageUrl(ingredienteModificato.getImageUrl());
                }
            } else {
                 ingredienteOriginale.setImageUrl(ingredienteModificato.getImageUrl());
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
            deleteImageFile(uploadDirectoryIngredienti, ingredienteDaEliminare.getImageUrl(), "/images/ingredienti/DefaultIngrediente.jpg");

            List<ElementoCarrello> elementiDaAggiornare = elementoCarrelloRepository.findAll().stream()
                .filter(ec -> ec.getIngredientiExtraSelezionati().contains(ingredienteDaEliminare))
                .collect(Collectors.toList());

            for (ElementoCarrello ec : elementiDaAggiornare) {
                ec.getIngredientiExtraSelezionati().remove(ingredienteDaEliminare);
                ec.calcolaPrezzoUnitario();
                elementoCarrelloRepository.save(ec);
                Carrello carrello = ec.getCarrello();
                if (carrello != null) {
                    carrello.setDataUltimaModifica(java.time.LocalDateTime.now());
                    carrelloRepository.save(carrello);
                }
            }
            ingredienteService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ingrediente reso inattivo e rimosso dagli extra nei carrelli.");
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", "Ingrediente non trovato.");
        }
        return "redirect:/admin/dashboard";
    }
}