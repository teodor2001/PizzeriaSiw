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

    // MODIFICA QUI: Rimuovi optional = false e nullable = false per permettere che sia null
    // Un ElementoCarrello può ora avere una Pizza OPPURE una Bevanda.
    @ManyToOne
    @JoinColumn(name = "pizza_id", nullable = true)
    private Pizza pizza;

    // NUOVO CAMPO: Aggiungi questo per la bevanda
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

    // --- Getter e Setter standard per i campi esistenti ---

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

    // Getter e Setter per Pizza (assicurati che siano presenti, se non usi Lombok)
    public Pizza getPizza() {
        return pizza;
    }

    public void setPizza(Pizza pizza) {
        this.pizza = pizza;
    }

    // NUOVI Getter e Setter per Bevanda
    public Bevanda getBevanda() {
        return bevanda;
    }

    public void setBevanda(Bevanda bevanda) {
        this.bevanda = bevanda;
    }

    // MODIFICA QUI: Aggiorna il calcolo del prezzo unitario per gestire sia pizze che bevande
    public void calcolaPrezzoUnitario() {
        if (this.pizza != null) {
            // Logica esistente per le pizze
            double prezzoExtra = 0;
            if (this.ingredientiExtraSelezionati != null) {
                for (Ingrediente extra : this.ingredientiExtraSelezionati) {
                    if (extra.getPrezzo() != null) {
                        prezzoExtra += extra.getPrezzo();
                    }
                }
            }
            double prezzoBasePizza = this.pizza.getPrezzoBase();
            // Il CarrelloService gestisce già l'applicazione dello sconto e passa il prezzo calcolato.
            // Quindi, qui ci basiamo sul prezzo base della pizza più gli extra.
            // Se CarrelloService imposta prezzoUnitarioCalcolato, questo metodo potrebbe essere meno critico per il prezzo base,
            // ma è comunque utile per gli extra e per una logica coesa del modello.
            this.prezzoUnitarioCalcolato = prezzoBasePizza + prezzoExtra;

        } else if (this.bevanda != null) { // NUOVA CONDIZIONE PER LE BEVANDE
            this.prezzoUnitarioCalcolato = this.bevanda.getPrezzo();
            // Per le bevande non ci sono ingredienti extra, quindi azzeriamo la lista.
            // (Anche se il CarrelloService dovrebbe già passare una lista vuota o null).
            if (this.ingredientiExtraSelezionati != null) {
                this.ingredientiExtraSelezionati.clear();
            }
        } else {
            // Se non è né pizza né bevanda (dovrebbe essere un caso raro con la logica attuale)
            this.prezzoUnitarioCalcolato = 0.0;
        }
    }

    // Questo metodo calcola il totale di un singolo elemento carrello (quantità * prezzo unitario)
    public double getPrezzoTotaleElemento() {
        // Se il prezzoUnitarioCalcolato è sempre aggiornato dal CarrelloService,
        // questa è la formula corretta.
        return this.quantita * this.prezzoUnitarioCalcolato;
    }

    @Override
    public int hashCode() {
        // MODIFICA QUI: Includi 'bevanda' nell'hashCode
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
        // MODIFICA QUI: Considera sia pizza che bevanda per l'uguaglianza
        return Objects.equals(carrello, other.carrello) && Objects.equals(id, other.id)
                && Objects.equals(ingredientiExtraSelezionati, other.ingredientiExtraSelezionati)
                && Objects.equals(pizza, other.pizza)
                && Objects.equals(bevanda, other.bevanda) // Confronta anche la bevanda
                && Double.doubleToLongBits(prezzoUnitarioCalcolato) == Double.doubleToLongBits(other.prezzoUnitarioCalcolato)
                && quantita == other.quantita;
    }
}