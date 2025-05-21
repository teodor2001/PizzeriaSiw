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

    @ManyToOne(optional = false)
    @JoinColumn(name = "pizza_id", nullable = false)
    private Pizza pizza;

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

    public Pizza getPizza() {
        return pizza;
    }

    public void setPizza(Pizza pizza) {
        this.pizza = pizza;
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

    public double getPrezzoTotaleElemento() {
        return this.quantita * this.prezzoUnitarioCalcolato;
    }
    

    public void calcolaPrezzoUnitario() {
        double prezzoExtra = 0;
        if (this.pizza != null && this.ingredientiExtraSelezionati != null) {
            for (Ingrediente extra : this.ingredientiExtraSelezionati) {
                if (extra.getPrezzo() != null) {
                    prezzoExtra += extra.getPrezzo();
                }
            }
            double prezzoBasePizza = this.pizza.getPrezzoBase();
            if (this.pizza.getScontoApplicato() != null && this.pizza.getScontoApplicato().getPercentuale() > 0) {
                 prezzoBasePizza = this.pizza.getPrezzoScontato();
            }
            this.prezzoUnitarioCalcolato = prezzoBasePizza + prezzoExtra;
        } else if (this.pizza != null) {
            this.prezzoUnitarioCalcolato = this.pizza.getPrezzoScontato();
        } else {
            this.prezzoUnitarioCalcolato = 0.0;
        }
    }

	@Override
	public int hashCode() {
		return Objects.hash(carrello, id, ingredientiExtraSelezionati, pizza, prezzoUnitarioCalcolato, quantita);
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
				&& Objects.equals(pizza, other.pizza) && Double.doubleToLongBits(prezzoUnitarioCalcolato) == Double
						.doubleToLongBits(other.prezzoUnitarioCalcolato)
				&& quantita == other.quantita;
	}
}