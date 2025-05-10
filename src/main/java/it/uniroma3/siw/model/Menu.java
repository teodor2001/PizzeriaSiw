package it.uniroma3.siw.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descrizione;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pizza> pizze;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bevanda> bevande;

    // Costruttori, getter e setter

    public Menu() {
        this.pizze = new ArrayList<>();
        this.bevande = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public List<Pizza> getPizze() {
        return pizze;
    }

    public void setPizze(List<Pizza> pizze) {
        this.pizze = pizze;
    }

    public List<Bevanda> getBevande() {
        return bevande;
    }

    public void setBevande(List<Bevanda> bevande) {
        this.bevande = bevande;
    }

    public void aggiungiPizza(Pizza pizza) {
        this.pizze.add(pizza);
        pizza.setMenu(this);
    }

    public void aggiungiBevanda(Bevanda bevanda) {
        this.bevande.add(bevanda);
        bevanda.setMenu(this);
    }

    public void aggiornaPizza(Pizza pizzaAggiornata) {
        for (int i = 0; i < this.pizze.size(); i++) {
            if (this.pizze.get(i).getIdPizza().equals(pizzaAggiornata.getIdPizza())) {
                this.pizze.set(i, pizzaAggiornata);
                pizzaAggiornata.setMenu(this); // Assicurati che la relazione sia mantenuta
                return; // Esci dal loop una volta trovata e aggiornata la pizza
            }
        }
        // Se la pizza non viene trovata nella lista, potresti voler gestire questo caso (es. lanciare un'eccezione)
    }

    // equals e hashCode (importanti per le entità)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return Objects.equals(id, menu.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}