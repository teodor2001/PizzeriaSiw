package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
public class Sconto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private double percentuale;

	@OneToMany(mappedBy = "scontoApplicato")
	private List<Pizza> pizze = new ArrayList<>();

	@ManyToMany(mappedBy = "sconti")
	private List<Cliente> clienti = new ArrayList<>();

	// Costruttore predefinito se no JPA si arrabbia
	public Sconto() {
	}

	// Costruttore vero
	public Sconto(double percentuale) {
		this.percentuale = percentuale;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public double getPercentuale() {
		return percentuale;
	}

	public void setPercentuale(double percentuale) {
		this.percentuale = percentuale;
	}

	// Utilità discutibile, potrebbe tornare utile per recuperare tutte le pizze con
	// un certo sconto, teniamolo per buono al momento
	public List<Pizza> getPizze() {
		return pizze;
	}

	public void setPizze(List<Pizza> pizze) {
		this.pizze = pizze;
	}

	public List<Cliente> getClienti() {
		return clienti;
	}

	public void setClienti(List<Cliente> clienti) {
		this.clienti = clienti;
	}

	public void aggiungiPizza(Pizza pizza) {
		this.pizze.add(pizza);
		pizza.setScontoApplicato(this);
	}

	public void rimuoviPizza(Pizza pizza) {
		this.pizze.remove(pizza);
		pizza.setScontoApplicato(null);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, percentuale);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Sconto other = (Sconto) obj;
		return Objects.equals(id, other.id)
				&& Double.doubleToLongBits(percentuale) == Double.doubleToLongBits(other.percentuale);
	}

}
