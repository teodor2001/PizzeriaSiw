package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Carrello;
import it.uniroma3.siw.model.Cliente;
import it.uniroma3.siw.model.ElementoCarrello;
import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Ingrediente;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarrelloService {

    @Autowired
    private CarrelloRepository carrelloRepository;

    @Autowired
    private ElementoCarrelloRepository elementoCarrelloRepository;

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private HttpSession httpSession;

    private static final String SESSION_CART_KEY = "sessionCart";

    private Cliente getUtenteLoggatoSePresente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                return clienteService.findByEmail(userDetails.getUsername());
            }
        }
        return null;
    }

    @Transactional
    public Carrello getCarrelloCorrente() {
        Cliente clienteLoggato = getUtenteLoggatoSePresente();

        if (clienteLoggato != null) {
            Optional<Carrello> carrelloOpt = carrelloRepository.findByCliente(clienteLoggato);
            Carrello carrelloDB;
            if (carrelloOpt.isPresent()) {
                carrelloDB = carrelloOpt.get();
            } else {
                carrelloDB = new Carrello();
                carrelloDB.setCliente(clienteLoggato);
                carrelloDB = carrelloRepository.save(carrelloDB);
            }
            unisciCarrelloSessioneConCarrelloDB(clienteLoggato, carrelloDB);
            return carrelloDB;
        } else {
            Carrello carrelloSessione = (Carrello) httpSession.getAttribute(SESSION_CART_KEY);
            if (carrelloSessione == null) {
                carrelloSessione = new Carrello();
                httpSession.setAttribute(SESSION_CART_KEY, carrelloSessione);
            }
            return carrelloSessione;
        }
    }

    @Transactional
    public void unisciCarrelloSessioneConCarrelloDB(Cliente cliente, Carrello carrelloDB) {
        Carrello carrelloSessione = (Carrello) httpSession.getAttribute(SESSION_CART_KEY);
        if (carrelloSessione != null && !carrelloSessione.getElementi().isEmpty()) {
            List<ElementoCarrello> elementiSessione = new ArrayList<>(carrelloSessione.getElementi());
            for (ElementoCarrello elSessione : elementiSessione) {
                Pizza pizzaDaSessione = elSessione.getPizza();

                Pizza pizzaManaged = pizzaRepository.findById(pizzaDaSessione.getIdPizza())
                                       .orElseThrow(() -> new IllegalArgumentException("Pizza non trovata con ID: " + pizzaDaSessione.getIdPizza()));

                Hibernate.initialize(pizzaManaged.getIngredientiExtra());

                List<Ingrediente> extraSelezionatiManaged = new ArrayList<>();
                if (elSessione.getIngredientiExtraSelezionati() != null && !elSessione.getIngredientiExtraSelezionati().isEmpty()) {
                    List<Long> extraIds = elSessione.getIngredientiExtraSelezionati().stream()
                                                    .map(Ingrediente::getId)
                                                    .collect(Collectors.toList());
                    if (!extraIds.isEmpty()) {
                       extraSelezionatiManaged.addAll(ingredienteRepository.findAllById(extraIds));
                    }
                }

                aggiungiElementoACarrelloSpecifico(carrelloDB, pizzaManaged,
                        extraSelezionatiManaged,
                        elSessione.getQuantita(),
                        false,
                        true); 
            }
            httpSession.removeAttribute(SESSION_CART_KEY);
        }
    }

 private void aggiungiElementoACarrelloSpecifico(Carrello carrello, Pizza pizza, List<Ingrediente> extraSelezionati,
                                                 int quantitaDaAggiungere, boolean isAnonimo, boolean sovrascriviQuantitaSeEsiste) {

     final List<Ingrediente> finalExtraSelezionati = extraSelezionati != null ? new ArrayList<>(extraSelezionati) : Collections.emptyList();

     Optional<ElementoCarrello> elementoEsistenteOpt = carrello.getElementi().stream()
             .filter(el -> el.getPizza().getIdPizza().equals(pizza.getIdPizza()) &&
                            el.getIngredientiExtraSelezionati().size() == finalExtraSelezionati.size() &&
                            el.getIngredientiExtraSelezionati().containsAll(finalExtraSelezionati) &&
                            finalExtraSelezionati.containsAll(el.getIngredientiExtraSelezionati()))
             .findFirst();

     double prezzoBasePizzaDaUsare;
     if (isAnonimo || pizza.getScontoApplicato() == null || pizza.getScontoApplicato().getPercentuale() == 0) {
         prezzoBasePizzaDaUsare = pizza.getPrezzoBase();
     } else {
         prezzoBasePizzaDaUsare = pizza.getPrezzoScontato();
     }

     double costoExtra = finalExtraSelezionati.stream()
                               .mapToDouble(ing -> ing.getPrezzo() != null ? ing.getPrezzo() : 0.0)
                               .sum();
     double prezzoUnitarioFinale = prezzoBasePizzaDaUsare + costoExtra;

     if (elementoEsistenteOpt.isPresent()) {
         ElementoCarrello el = elementoEsistenteOpt.get();
         if (sovrascriviQuantitaSeEsiste) {
             el.setQuantita(quantitaDaAggiungere);
         } else {
             el.setQuantita(el.getQuantita() + quantitaDaAggiungere);
         }
         el.setPrezzoUnitarioCalcolato(prezzoUnitarioFinale);
         if (!isAnonimo) {
              elementoCarrelloRepository.save(el);
         }
     } else {
         ElementoCarrello nuovoElemento = new ElementoCarrello();
         nuovoElemento.setPizza(pizza);
         nuovoElemento.setIngredientiExtraSelezionati(finalExtraSelezionati);
         nuovoElemento.setQuantita(quantitaDaAggiungere);
         nuovoElemento.setPrezzoUnitarioCalcolato(prezzoUnitarioFinale);

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

        Pizza pizza = pizzaRepository.findById(pizzaId)
                .orElseThrow(() -> new IllegalArgumentException("Pizza non trovata con ID: " + pizzaId));

        Hibernate.initialize(pizza.getIngredientiExtra());
        if (pizza.getScontoApplicato() != null) {
             Hibernate.initialize(pizza.getScontoApplicato());
        }

        List<Ingrediente> extraSelezionati = (idsIngredientiExtraSelezionati != null && !idsIngredientiExtraSelezionati.isEmpty()) ?
                ingredienteRepository.findAllById(idsIngredientiExtraSelezionati) : Collections.emptyList();

        aggiungiElementoACarrelloSpecifico(carrello, pizza, extraSelezionati, quantita, isAnonimo, false);

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
            .filter(e -> {
                if (!isAnonimo) return elementoCarrelloId.equals(e.getId());
                return elementoCarrelloId.equals(System.identityHashCode(e));
            })
            .findFirst();


        if (elOpt.isPresent()) {
            ElementoCarrello elementoDaRimuovere = elOpt.get();
            if (!isAnonimo) {
                if (elementoDaRimuovere.getCarrello() == null || !elementoDaRimuovere.getCarrello().getId().equals(carrello.getId())) {
                    throw new SecurityException("Tentativo di rimuovere un elemento da un carrello non proprio o mal associato.");
                }
                elementoCarrelloRepository.delete(elementoDaRimuovere);
                carrello.getElementi().remove(elementoDaRimuovere);
            } else {
                 carrello.getElementi().remove(elementoDaRimuovere);
            }
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

        Optional<ElementoCarrello> elOpt = carrello.getElementi().stream()
            .filter(e -> {
                 if (!isAnonimo) return elementoCarrelloId.equals(e.getId());
                 return elementoCarrelloId.equals(System.identityHashCode(e));
            })
            .findFirst();

        if(elOpt.isPresent()){
            ElementoCarrello elementoDaAggiornare = elOpt.get();
             if (!isAnonimo && (elementoDaAggiornare.getCarrello() == null || !elementoDaAggiornare.getCarrello().getId().equals(carrello.getId()))) {
                throw new SecurityException("Tentativo di aggiornare un elemento di un carrello non proprio o mal associato.");
            }

            if (nuovaQuantita <= 0) {
                if(!isAnonimo) elementoCarrelloRepository.delete(elementoDaAggiornare);
                carrello.getElementi().remove(elementoDaAggiornare);
            } else {
                elementoDaAggiornare.setQuantita(nuovaQuantita);
                if(!isAnonimo) elementoCarrelloRepository.save(elementoDaAggiornare);
            }
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
    public Carrello svuotaCarrello() {
        Carrello carrello = getCarrelloCorrente();
        boolean isAnonimo = (getUtenteLoggatoSePresente() == null);

        if (!isAnonimo && carrello.getId() != null) {
            List<ElementoCarrello> elementiDaRimuovere = new ArrayList<>(carrello.getElementi());
            for(ElementoCarrello el : elementiDaRimuovere){
                 if(el.getId() != null) elementoCarrelloRepository.delete(el);
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
            throw new IllegalStateException("Operazione non permessa per utenti loggati. Usare aggiornaQuantitaElemento.");
        }
        if (itemIndex < 0 || carrello.getElementi() == null || itemIndex >= carrello.getElementi().size()) {
            throw new IllegalArgumentException("Indice articolo non valido: " + itemIndex);
        }

        ElementoCarrello elementoDaAggiornare = carrello.getElementi().get(itemIndex);
        elementoDaAggiornare.setQuantita(nuovaQuantita);
        httpSession.setAttribute(SESSION_CART_KEY, carrello); 
        return carrello;
    }

    @Transactional
    public Carrello rimuoviElementoDalCarrelloAnon(int itemIndex) {
        Carrello carrello = getCarrelloCorrente(); 
        if (getUtenteLoggatoSePresente() != null) {
            throw new IllegalStateException("Operazione non permessa per utenti loggati. Usare rimuoviElementoDalCarrello.");
        }
        if (itemIndex < 0 || carrello.getElementi() == null || itemIndex >= carrello.getElementi().size()) {
            throw new IllegalArgumentException("Indice articolo non valido: " + itemIndex);
        }

        carrello.getElementi().remove(itemIndex);
        httpSession.setAttribute(SESSION_CART_KEY, carrello); 
        return carrello;
    }
}