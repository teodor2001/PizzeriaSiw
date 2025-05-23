package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Bevanda;
import it.uniroma3.siw.model.Carrello;
import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.model.ElementoCarrello;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.repository.BevandaRepository;
import it.uniroma3.siw.repository.CarrelloRepository;
import it.uniroma3.siw.repository.ElementoCarrelloRepository;
import it.uniroma3.siw.repository.PizzaRepository;
import it.uniroma3.siw.repository.IngredienteRepository;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CarrelloService {

    @Autowired
    private ElementoCarrelloRepository elementoCarrelloRepository;

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private BevandaRepository bevandaRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;
    
    @Autowired
    private IngredienteService ingredienteService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private CarrelloRepository carrelloRepository;

    private static final String SESSION_CART_KEY = "sessionCart";

    private Cliente getUtenteLoggatoSePresente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                return clienteService.findByEmail(userDetails.getUsername());
            }
            if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    return clienteService.findByEmail(email);
                }
            }
        }
        return null;
    }

    @Transactional
    public Carrello getCarrelloCorrente() {
        Cliente cliente = getUtenteLoggatoSePresente();
        Carrello carrello;
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        if (cliente != null) {
            carrello = carrelloRepository.findByCliente(cliente)
                    .orElseGet(() -> {
                        Carrello nuovoCarrello = new Carrello();
                        nuovoCarrello.setCliente(cliente);
                        return carrelloRepository.save(nuovoCarrello);
                    });
        } else {
            carrello = (Carrello) httpSession.getAttribute(SESSION_CART_KEY);
            if (carrello == null) {
                carrello = new Carrello();
                httpSession.setAttribute(SESSION_CART_KEY, carrello);
            }
        }

        // Initialize pizza's extra ingredients and recalculate prices
        for (ElementoCarrello elemento : carrello.getElementi()) {
            if (elemento.getPizza() != null) {
                elemento.getPizza().setIngredientiExtraDisponibili(tuttiGliIngredienti);
                if (elemento.getPizza().getScontoApplicato() != null) {
                    Hibernate.initialize(elemento.getPizza().getScontoApplicato());
                }
            }
            elemento.calcolaPrezzoUnitario();
        }

        return carrello;
    }

    @Transactional
    public void unisciCarrelloSessioneConCarrelloDB(Cliente cliente, Carrello carrelloDB) {
        Carrello carrelloSessione = (Carrello) httpSession.getAttribute(SESSION_CART_KEY);
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();
        if (carrelloSessione != null && !carrelloSessione.getElementi().isEmpty()) {
            List<ElementoCarrello> elementiSessione = new ArrayList<>(carrelloSessione.getElementi());
            
            for (ElementoCarrello elSessione : elementiSessione) {
                if (elSessione.getPizza() != null) {
                    Pizza pizzaDaSessione = elSessione.getPizza();
                    Pizza pizzaManaged = pizzaRepository.findById(pizzaDaSessione.getIdPizza())
                            .orElseThrow(() -> new IllegalArgumentException("Pizza non trovata con ID: " + pizzaDaSessione.getIdPizza()));

                    Hibernate.initialize(pizzaManaged.getNomiIngredientiBase());
                    pizzaManaged.setIngredientiExtraDisponibili(tuttiGliIngredienti);
                     if (pizzaManaged.getScontoApplicato() != null) {
                        Hibernate.initialize(pizzaManaged.getScontoApplicato());
                    }

                    List<Ingrediente> extraSelezionatiManaged = new ArrayList<>();
                    if (elSessione.getIngredientiExtraSelezionati() != null && !elSessione.getIngredientiExtraSelezionati().isEmpty()) {
                        List<Long> extraIds = elSessione.getIngredientiExtraSelezionati().stream()
                                .map(Ingrediente::getId)
                                .collect(Collectors.toList());
                        if (!extraIds.isEmpty()) {
                            extraSelezionatiManaged.addAll(ingredienteRepository.findAllById(extraIds));
                        }
                    }
                    aggiungiElementoACarrelloSpecifico(carrelloDB, pizzaManaged, extraSelezionatiManaged, elSessione.getQuantita(), false, false);
                } else if (elSessione.getBevanda() != null) {
                    Bevanda bevandaDaSessione = elSessione.getBevanda();
                    Bevanda bevandaManaged = bevandaRepository.findById(bevandaDaSessione.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Bevanda non trovata con ID: " + bevandaDaSessione.getId()));
                    aggiungiElementoACarrelloSpecifico(carrelloDB, bevandaManaged, elSessione.getQuantita(), false, false);
                }
            }
            carrelloRepository.save(carrelloDB);
            httpSession.removeAttribute(SESSION_CART_KEY); 
        }
    }
    
    private void aggiungiElementoACarrelloSpecifico(Carrello carrello, Pizza pizza, List<Ingrediente> extraSelezionatiInput,
                                                 int quantitaDaAggiungere, boolean isAnonimo, boolean sovrascriviQuantitaSeEsiste) {

        final List<Ingrediente> finalExtraSelezionati = extraSelezionatiInput != null ? new ArrayList<>(extraSelezionatiInput) : Collections.emptyList();
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();
        pizza.setIngredientiExtraDisponibili(tuttiGliIngredienti);

        Optional<ElementoCarrello> elementoEsistenteOpt = carrello.getElementi().stream()
                .filter(el -> el.getPizza() != null && el.getPizza().getIdPizza().equals(pizza.getIdPizza()) &&
                               new HashSet<>(el.getIngredientiExtraSelezionati()).equals(new HashSet<>(finalExtraSelezionati)))
                .findFirst();
        
        if (elementoEsistenteOpt.isPresent()) {
            ElementoCarrello el = elementoEsistenteOpt.get();
            if (sovrascriviQuantitaSeEsiste) {
                el.setQuantita(quantitaDaAggiungere);
            } else {
                el.setQuantita(el.getQuantita() + quantitaDaAggiungere);
            }
            el.setPizza(pizza); 
            el.setIngredientiExtraSelezionati(new ArrayList<>(finalExtraSelezionati)); 
            el.calcolaPrezzoUnitario(); 
            if (!isAnonimo) {
                elementoCarrelloRepository.save(el);
            }
        } else {
            ElementoCarrello nuovoElemento = new ElementoCarrello();
            nuovoElemento.setPizza(pizza); 
            nuovoElemento.setIngredientiExtraSelezionati(new ArrayList<>(finalExtraSelezionati));
            nuovoElemento.setQuantita(quantitaDaAggiungere);
            nuovoElemento.calcolaPrezzoUnitario(); 
            carrello.aggiungiElemento(nuovoElemento); 
            if (!isAnonimo) {
                elementoCarrelloRepository.save(nuovoElemento); 
            }
        }
    }

    private void aggiungiElementoACarrelloSpecifico(Carrello carrello, Bevanda bevanda,
                                                 int quantitaDaAggiungere, boolean isAnonimo, boolean sovrascriviQuantitaSeEsiste) {

        Optional<ElementoCarrello> elementoEsistenteOpt = carrello.getElementi().stream()
                .filter(el -> el.getBevanda() != null && el.getBevanda().getId().equals(bevanda.getId()) &&
                               (el.getIngredientiExtraSelezionati() == null || el.getIngredientiExtraSelezionati().isEmpty()))
                .findFirst();
        
        if (elementoEsistenteOpt.isPresent()) {
            ElementoCarrello el = elementoEsistenteOpt.get();
            if (sovrascriviQuantitaSeEsiste) {
                el.setQuantita(quantitaDaAggiungere);
            } else {
                el.setQuantita(el.getQuantita() + quantitaDaAggiungere);
            }
            el.setBevanda(bevanda); 
            el.calcolaPrezzoUnitario();
            if (!isAnonimo) {
                elementoCarrelloRepository.save(el);
            }
        } else {
            ElementoCarrello nuovoElemento = new ElementoCarrello();
            nuovoElemento.setBevanda(bevanda);
            nuovoElemento.setQuantita(quantitaDaAggiungere);
            nuovoElemento.calcolaPrezzoUnitario();
            carrello.aggiungiElemento(nuovoElemento);
            if (!isAnonimo) {
                elementoCarrelloRepository.save(nuovoElemento);
            }
        }
    }

    @Transactional
    public Carrello aggiungiPizzaAlCarrello(Long pizzaId, List<Long> idsIngredientiExtraSelezionati, int quantita) {
        if (quantita <= 0) {
            throw new IllegalArgumentException("La quantità deve essere maggiore di zero.");
        }

        Carrello carrello = getCarrelloCorrente();
        boolean isAnonimo = (getUtenteLoggatoSePresente() == null);
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        Pizza pizza = pizzaRepository.findById(pizzaId)
                .orElseThrow(() -> new IllegalArgumentException("Pizza non trovata con ID: " + pizzaId));

        Hibernate.initialize(pizza.getNomiIngredientiBase());
        pizza.setIngredientiExtraDisponibili(tuttiGliIngredienti);
        if (pizza.getScontoApplicato() != null) {
            Hibernate.initialize(pizza.getScontoApplicato());
        }

        List<Ingrediente> extraEffettivamenteSelezionati = (idsIngredientiExtraSelezionati != null && !idsIngredientiExtraSelezionati.isEmpty()) ?
                ingredienteRepository.findAllById(idsIngredientiExtraSelezionati) : Collections.emptyList();

        aggiungiElementoACarrelloSpecifico(carrello, pizza, extraEffettivamenteSelezionati, quantita, isAnonimo, false);

        carrello.setDataUltimaModifica(LocalDateTime.now());

        if (!isAnonimo) {
            return carrelloRepository.save(carrello);
        } else {
            httpSession.setAttribute(SESSION_CART_KEY, carrello);
            return carrello;
        }
    }
    
    @Transactional
    public Carrello aggiungiBevandaAlCarrello(Long bevandaId, int quantita) {
        if (quantita <= 0) {
            throw new IllegalArgumentException("La quantità deve essere maggiore di zero.");
        }

        Carrello carrello = getCarrelloCorrente();
        boolean isAnonimo = (getUtenteLoggatoSePresente() == null);

        Bevanda bevanda = bevandaRepository.findById(bevandaId)
                .orElseThrow(() -> new IllegalArgumentException("Bevanda non trovata con ID: " + bevandaId));

        aggiungiElementoACarrelloSpecifico(carrello, bevanda, quantita, isAnonimo, false);

        carrello.setDataUltimaModifica(LocalDateTime.now());

        if (!isAnonimo) {
            return carrelloRepository.save(carrello);
        } else {
            httpSession.setAttribute(SESSION_CART_KEY, carrello);
            return carrello;
        }
    }

    @Transactional
    public Carrello rimuoviElementoDalCarrello(Long elementoCarrelloId) {
        Carrello carrello = getCarrelloCorrente();
        boolean isAnonimo = (getUtenteLoggatoSePresente() == null);

        Optional<ElementoCarrello> elOpt = carrello.getElementi().stream()
                .filter(e -> elementoCarrelloId.equals(e.getId()))
                .findFirst();

        if (elOpt.isPresent()) {
            ElementoCarrello elementoDaRimuovere = elOpt.get();
            carrello.rimuoviElemento(elementoDaRimuovere); 
            if (!isAnonimo && elementoDaRimuovere.getId() != null) { 
                elementoCarrelloRepository.delete(elementoDaRimuovere);
            }
        } else {
            throw new IllegalArgumentException("Elemento carrello non trovato con ID: " + elementoCarrelloId);
        }

        carrello.setDataUltimaModifica(LocalDateTime.now());
        if (!isAnonimo) {
            return carrelloRepository.save(carrello);
        } else {
            httpSession.setAttribute(SESSION_CART_KEY, carrello);
            return carrello;
        }
    }

    @Transactional
    public Carrello aggiornaQuantitaElemento(Long elementoCarrelloId, int nuovaQuantita) {
        Carrello carrello = getCarrelloCorrente();
        boolean isAnonimo = (getUtenteLoggatoSePresente() == null);
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        Optional<ElementoCarrello> elOpt = carrello.getElementi().stream()
                .filter(e -> elementoCarrelloId.equals(e.getId())) 
                .findFirst();

        if (elOpt.isPresent()) {
            ElementoCarrello elementoDaAggiornare = elOpt.get();

            if (nuovaQuantita <= 0) { 
                carrello.rimuoviElemento(elementoDaAggiornare);
                if (!isAnonimo && elementoDaAggiornare.getId() != null) {
                    elementoCarrelloRepository.delete(elementoDaAggiornare);
                }
            } else {
                elementoDaAggiornare.setQuantita(nuovaQuantita);
                if (elementoDaAggiornare.getPizza() != null) {
                     Pizza pizzaManaged = pizzaRepository.findById(elementoDaAggiornare.getPizza().getIdPizza()).orElseThrow();
                     Hibernate.initialize(pizzaManaged.getScontoApplicato()); 
                     pizzaManaged.setIngredientiExtraDisponibili(tuttiGliIngredienti);
                     elementoDaAggiornare.setPizza(pizzaManaged); 
                }
                elementoDaAggiornare.calcolaPrezzoUnitario(); 
                if (!isAnonimo) {
                    elementoCarrelloRepository.save(elementoDaAggiornare);
                }
            }
        } else {
             throw new IllegalArgumentException("Elemento carrello non trovato con ID: " + elementoCarrelloId + " per l'aggiornamento quantità.");
        }

        carrello.setDataUltimaModifica(LocalDateTime.now());
        if (!isAnonimo) {
            return carrelloRepository.save(carrello);
        } else {
            httpSession.setAttribute(SESSION_CART_KEY, carrello);
            return carrello;
        }
    }

    @Transactional
    public Carrello aggiornaExtraElementoCarrello(Long elementoCarrelloId, List<Long> selectedExtraIds) {
        Carrello carrello = getCarrelloCorrente();
        boolean isAnonimo = (getUtenteLoggatoSePresente() == null);
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        Optional<ElementoCarrello> elOpt = carrello.getElementi().stream()
            .filter(e -> elementoCarrelloId.equals(e.getId())) 
            .findFirst();

        if (elOpt.isPresent()) {
            ElementoCarrello elemento = elOpt.get();
            if (elemento.getPizza() == null) {
                throw new IllegalArgumentException("L'elemento non è una pizza, non può avere extra.");
            }

            Pizza pizzaDellElemento = elemento.getPizza();
            pizzaDellElemento.setIngredientiExtraDisponibili(tuttiGliIngredienti);
            
            List<Ingrediente> nuoviExtraSelezionati = new ArrayList<>();
            if (selectedExtraIds != null && !selectedExtraIds.isEmpty()) {
                List<Long> idsPotenzialiExtra = pizzaDellElemento.getIngredientiExtraDisponibili().stream()
                    .map(Ingrediente::getId)
                    .collect(Collectors.toList());
                
                for(Long selectedId : selectedExtraIds) {
                    if(!idsPotenzialiExtra.contains(selectedId)) {
                        throw new IllegalArgumentException("Ingrediente extra con ID " + selectedId + " non valido per questa pizza.");
                    }
                }
                nuoviExtraSelezionati = ingredienteRepository.findAllById(selectedExtraIds);
            }
            
            elemento.setIngredientiExtraSelezionati(nuoviExtraSelezionati);
            Pizza pizzaManaged = pizzaRepository.findById(elemento.getPizza().getIdPizza()).orElseThrow();
            Hibernate.initialize(pizzaManaged.getScontoApplicato()); 
            pizzaManaged.setIngredientiExtraDisponibili(tuttiGliIngredienti);
            elemento.setPizza(pizzaManaged);

            elemento.calcolaPrezzoUnitario(); 

            if (!isAnonimo) {
                elementoCarrelloRepository.save(elemento);
            }
        } else {
            throw new IllegalArgumentException("Elemento carrello non trovato con ID: " + elementoCarrelloId + " per aggiornare gli extra.");
        }
        
        carrello.setDataUltimaModifica(LocalDateTime.now());
        if (isAnonimo) {
            httpSession.setAttribute(SESSION_CART_KEY, carrello);
        } else {
            carrelloRepository.save(carrello);
        }
        return carrello;
    }

    @Transactional
    public Carrello svuotaCarrello() {
        Carrello carrello = getCarrelloCorrente();
        boolean isAnonimo = (getUtenteLoggatoSePresente() == null);

        if (!isAnonimo && carrello.getId() != null) {
            List<ElementoCarrello> elementiDaRimuovere = new ArrayList<>(carrello.getElementi());
            for (ElementoCarrello el : elementiDaRimuovere) {
                if (el.getId() != null) elementoCarrelloRepository.delete(el);
            }
        }
        carrello.getElementi().clear();

        carrello.setDataUltimaModifica(LocalDateTime.now());
        if (!isAnonimo) {
            return carrelloRepository.save(carrello); 
        } else {
            httpSession.setAttribute(SESSION_CART_KEY, carrello);
            return carrello;
        }
    }

    @Transactional
    public Carrello aggiornaQuantitaElementoAnon(int itemIndex, int nuovaQuantita) {
        Carrello carrello = getCarrelloCorrente(); 
        if (getUtenteLoggatoSePresente() != null) {
            throw new IllegalStateException("Operazione non permessa per utenti loggati. Usare aggiornaQuantitaElemento con ID.");
        }
        if (itemIndex < 0 || carrello.getElementi() == null || itemIndex >= carrello.getElementi().size()) {
            throw new IllegalArgumentException("Indice articolo non valido: " + itemIndex);
        }
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        ElementoCarrello elementoDaAggiornare = carrello.getElementi().get(itemIndex); // Modificato per usare getElementiAsList()
        if (nuovaQuantita <= 0) {
            carrello.getElementi().remove(elementoDaAggiornare); // Rimuovi l'oggetto, non l'indice, se la lista sottostante può cambiare
        } else {
            elementoDaAggiornare.setQuantita(nuovaQuantita);
            if (elementoDaAggiornare.getPizza() != null) {
                elementoDaAggiornare.getPizza().setIngredientiExtraDisponibili(tuttiGliIngredienti);
            }
            elementoDaAggiornare.calcolaPrezzoUnitario();
        }
        httpSession.setAttribute(SESSION_CART_KEY, carrello); 
        return carrello;
    }

    @Transactional
    public Carrello rimuoviElementoDalCarrelloAnon(int itemIndex) {
        Carrello carrello = getCarrelloCorrente();
        if (getUtenteLoggatoSePresente() != null) {
            throw new IllegalStateException("Operazione non permessa per utenti loggati. Usare rimuoviElementoDalCarrello con ID.");
        }
        if (itemIndex < 0 || carrello.getElementi() == null || itemIndex >= carrello.getElementi().size()) {
            throw new IllegalArgumentException("Indice articolo non valido: " + itemIndex);
        }

        carrello.getElementi().remove(carrello.getElementi().get(itemIndex));
        httpSession.setAttribute(SESSION_CART_KEY, carrello);
        return carrello;
    }
    
    @Transactional
    public Carrello aggiornaExtraElementoCarrelloAnon(int itemIndex, List<Long> selectedExtraIds) {
        Carrello carrello = getCarrelloCorrente();
        if (getUtenteLoggatoSePresente() != null) {
            throw new IllegalStateException("Operazione non permessa per utenti loggati.");
        }
        if (itemIndex < 0 || carrello.getElementi() == null || itemIndex >= carrello.getElementi().size()) {
            throw new IllegalArgumentException("Indice articolo non valido: " + itemIndex);
        }
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();

        ElementoCarrello elemento = carrello.getElementi().get(itemIndex); 
        if (elemento.getPizza() == null) {
            throw new IllegalArgumentException("L'elemento non è una pizza, non può avere extra.");
        }
        
        Pizza pizzaDellElemento = elemento.getPizza();
        pizzaDellElemento.setIngredientiExtraDisponibili(tuttiGliIngredienti);

        List<Ingrediente> nuoviExtraSelezionati = new ArrayList<>();
        if (selectedExtraIds != null && !selectedExtraIds.isEmpty()) {
             List<Long> idsPotenzialiExtra = pizzaDellElemento.getIngredientiExtraDisponibili().stream().map(Ingrediente::getId).collect(Collectors.toList());
             for(Long selectedId : selectedExtraIds) {
                 if(!idsPotenzialiExtra.contains(selectedId)) {
                     throw new IllegalArgumentException("Ingrediente extra con ID " + selectedId + " non valido per questa pizza.");
                 }
             }
            nuoviExtraSelezionati = ingredienteRepository.findAllById(selectedExtraIds);
        }
        
        elemento.setIngredientiExtraSelezionati(nuoviExtraSelezionati);
        elemento.calcolaPrezzoUnitario();

        httpSession.setAttribute(SESSION_CART_KEY, carrello);
        return carrello;
    }

    @Transactional
    public void svuotaCarrelloSpecifico(Long carrelloId) {
        Optional<Carrello> carrelloOpt = carrelloRepository.findById(carrelloId);
        if (carrelloOpt.isPresent()) {
            Carrello carrello = carrelloOpt.get();
            Hibernate.initialize(carrello.getElementi()); 
            if (carrello.getElementi() != null && !carrello.getElementi().isEmpty()) {
                for (ElementoCarrello el : new ArrayList<>(carrello.getElementi())) { // Crea una copia per evitare ConcurrentModificationException
                    elementoCarrelloRepository.delete(el);
                }
                carrello.getElementi().clear(); 
            }
            carrello.setDataUltimaModifica(LocalDateTime.now());
            carrelloRepository.save(carrello); 
        } else {
            System.out.println("Tentativo di svuotare un carrello non esistente con ID: " + carrelloId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Carrello> getCarrelloByClienteId(Long clienteId) {
        Optional<Carrello> carrelloOpt = carrelloRepository.findByClienteIdCliente(clienteId);
        Set<Ingrediente> tuttiGliIngredienti = ingredienteService.findAll();
        carrelloOpt.ifPresent(carrello -> {
            Hibernate.initialize(carrello.getElementi());
             for (ElementoCarrello el : carrello.getElementi()) {
                if (el.getPizza() != null) {
                    Hibernate.initialize(el.getPizza().getNomiIngredientiBase());
                    el.getPizza().setIngredientiExtraDisponibili(tuttiGliIngredienti);
                     if (el.getPizza().getScontoApplicato() != null) {
                        Hibernate.initialize(el.getPizza().getScontoApplicato());
                    }
                }
                Hibernate.initialize(el.getIngredientiExtraSelezionati());
            }
        });
        return carrelloOpt;
    }
}