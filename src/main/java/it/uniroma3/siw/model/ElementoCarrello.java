package it.uniroma3.siw.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class ElementoCarrello {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "carrello_id", nullable = false)
    private Carrello carrello;

    @ManyToOne
    @JoinColumn(name = "pizza_id", nullable = true)
    private Pizza pizza;

    @ManyToOne
    @JoinColumn(name = "bevanda_id")
    private Bevanda bevanda;

    private int quantita;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "elemento_carrello_ingrediente_extra",
            joinColumns = @JoinColumn(name = "elemento_carrello_id"),
            inverseJoinColumns = @JoinColumn(name = "ingrediente_id")
    )
    private List<Ingrediente> ingredientiExtraSelezionati;

    private double prezzoUnitarioCalcolato;

    public ElementoCarrello() {
        this.ingredientiExtraSelezionati = new ArrayList<>();
        this.quantita = 1;
    }

    // --- Getter e Setter standard ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carrello getCarrello() {
        return carrello;
    }

    public void setCarrello(Carrello carrello) {
        this.carrello = carrello;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public List<Ingrediente> getIngredientiExtraSelezionati() {
        return ingredientiExtraSelezionati;
    }

    public void setIngredientiExtraSelezionati(List<Ingrediente> ingredientiExtraSelezionati) {
        this.ingredientiExtraSelezionati = ingredientiExtraSelezionati;
    }

    public double getPrezzoUnitarioCalcolato() {
        return prezzoUnitarioCalcolato;
    }

    public void setPrezzoUnitarioCalcolato(double prezzoUnitarioCalcolato) {
        this.prezzoUnitarioCalcolato = prezzoUnitarioCalcolato;
    }

    public Pizza getPizza() {
        return pizza;
    }

    public void setPizza(Pizza pizza) {
        this.pizza = pizza;
    }

    public Bevanda getBevanda() {
        return bevanda;
    }

    public void setBevanda(Bevanda bevanda) {
        this.bevanda = bevanda;
    }

    // MODIFIED METHOD
    public void calcolaPrezzoUnitario() {
        if (this.pizza != null) {
            // Usa pizza.getPrezzoScontato() che già gestisce se c'è uno sconto o meno
            double prezzoPizzaEffettivo = this.pizza.getPrezzoScontato(); // Correctly uses discounted price

            double prezzoExtraAggiuntivi = 0;
            if (this.ingredientiExtraSelezionati != null) {
                for (Ingrediente extra : this.ingredientiExtraSelezionati) {
                    if (extra.getPrezzo() != null) {
                        prezzoExtraAggiuntivi += extra.getPrezzo();
                    }
                }
            }
            this.prezzoUnitarioCalcolato = prezzoPizzaEffettivo + prezzoExtraAggiuntivi;

        } else if (this.bevanda != null) {
            this.prezzoUnitarioCalcolato = this.bevanda.getPrezzo();
            if (this.ingredientiExtraSelezionati != null) { // Bevande non hanno extra
                this.ingredientiExtraSelezionati.clear();
            }
        } else {
            this.prezzoUnitarioCalcolato = 0.0;
        }
    }

    public double getPrezzoTotaleElemento() {
        // Il prezzo unitario calcolato tiene già conto di sconti e ingredienti extra selezionati.
        return this.quantita * this.prezzoUnitarioCalcolato;
    }

    @Override
    public int hashCode() {
        return Objects.hash(carrello, id, ingredientiExtraSelezionati, pizza, bevanda, prezzoUnitarioCalcolato, quantita);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElementoCarrello other = (ElementoCarrello) obj;
        return Objects.equals(carrello, other.carrello) && Objects.equals(id, other.id)
                && Objects.equals(ingredientiExtraSelezionati, other.ingredientiExtraSelezionati)
                && Objects.equals(pizza, other.pizza)
                && Objects.equals(bevanda, other.bevanda)
                && Double.doubleToLongBits(prezzoUnitarioCalcolato) == Double.doubleToLongBits(other.prezzoUnitarioCalcolato)
                && quantita == other.quantita;
    }
}