package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pizza> pizze = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingrediente> ingredientiExtra = new ArrayList<>();

    @OneToOne(mappedBy = "menu")
    private Pizzeria pizzeria;
    
    @OneToMany(mappedBy = "menuAssociato")
    private List<Amministratore> amministratoriAssociati = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "menu_bevanda",
        joinColumns = @JoinColumn(name = "menu_id"),
        inverseJoinColumns = @JoinColumn(name = "bevanda_id")
    )
    private List<Bevanda> bevande = new ArrayList<>();

    //Costruttore predefinito se no JPA si arrabbia
    public Menu() {
    }
    
    //Costruttore vero
    public Menu(List<Pizza> pizze, List<Ingrediente> ingredientiExtra, List<Bevanda> bevande) {
        this.pizze = pizze;
        this.ingredientiExtra = ingredientiExtra;
        this.bevande = bevande;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Pizza> getPizze() {
        return pizze;
    }

    public void setPizze(List<Pizza> pizze) {
        this.pizze = pizze;
    }

    public List<Ingrediente> getIngredientiExtra() {
        return ingredientiExtra;
    }

    public void setIngredientiExtra(List<Ingrediente> ingredientiExtra) {
        this.ingredientiExtra = ingredientiExtra;
    }

    public List<Bevanda> getBevande() {
        return bevande;
    }

    public void setBevande(List<Bevanda> bevande) {
        this.bevande = bevande;
    }

    public void aggiungiPizza(Pizza pizza) {
        this.pizze.add(pizza);
    }

    public void rimuoviPizza(Pizza pizza) {
        this.pizze.remove(pizza);
    }

    public void aggiungiIngredienteExtra(Ingrediente ingrediente) {
        this.ingredientiExtra.add(ingrediente);
    }

    public void rimuoviIngredienteExtra(Ingrediente ingrediente) {
        this.ingredientiExtra.remove(ingrediente);
    }

    public void aggiungiBevanda(Bevanda bevanda) {
        this.bevande.add(bevanda);
        bevanda.getMenu().add(this);
    }

    public void rimuoviBevanda(Bevanda bevanda) {
        this.bevande.remove(bevanda);
        bevanda.getMenu().remove(this);
    }
    
    public Pizzeria getPizzeria() {
        return pizzeria;
    }

    public void setPizzeria(Pizzeria pizzeria) {
        this.pizzeria = pizzeria;
    }
    public List<Amministratore> getAmministratoriAssociati() {
        return amministratoriAssociati;
    }

    public void setAmministratoriAssociati(List<Amministratore> amministratoriAssociati) {
        this.amministratoriAssociati = amministratoriAssociati;
        
    }
    
    public void aggiungiAmministratoreAssociato(Amministratore amministratore) {
        this.amministratoriAssociati.add(amministratore);
        amministratore.setMenuAssociato(this);
    }

    public void rimuoviAmministratoreResponsabile(Amministratore amministratore) {
        this.amministratoriAssociati.remove(amministratore);
        amministratore.setMenuAssociato(null);
    }

	@Override
	public int hashCode() {
		return Objects.hash(amministratoriAssociati, bevande, id, ingredientiExtra, pizze, pizzeria);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Menu other = (Menu) obj;
		return Objects.equals(amministratoriAssociati, other.amministratoriAssociati)
				&& Objects.equals(bevande, other.bevande) && Objects.equals(id, other.id)
				&& Objects.equals(ingredientiExtra, other.ingredientiExtra) && Objects.equals(pizze, other.pizze)
				&& Objects.equals(pizzeria, other.pizzeria);
	}
    
    

}
