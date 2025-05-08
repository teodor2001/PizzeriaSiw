package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Pizza;
import it.uniroma3.siw.model.Sconto;
import org.springframework.stereotype.Service;

@Service
public class ScontoService {

    public Pizza applicaSconto(Pizza pizza, Sconto sconto) {
        pizza.setScontoApplicato(sconto);
        return pizza;
    }
}