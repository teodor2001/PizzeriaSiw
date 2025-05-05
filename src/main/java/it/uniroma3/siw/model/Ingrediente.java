package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
public class Ingrediente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIngrediente;
    private String nome;
    private double prezzo;

    @ManyToMany(mappedBy = "ingredientiExtra")
    private List<Pizza> pizzeExtra = new ArrayList<>(); // Lista di pizze a cui questo ingrediente è stato aggiunto come extra

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
    
    //Costruttore base se no JPA si arrabbia
    public Ingrediente() {
    }

    //Costruttore vero
    public Ingrediente(String nome, double prezzo) {
        this.nome = nome;
        this.prezzo = prezzo;
    }

    // Getter e setter

    public Long getIdIngrediente() {
        return idIngrediente;
    }

    public void setIdIngrediente(Long idIngrediente) {
        this.idIngrediente = idIngrediente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }

    public List<Pizza> getPizzeExtra() {
        return pizzeExtra;
    }

    public void setPizzeExtra(List<Pizza> pizzeExtra) {
        this.pizzeExtra = pizzeExtra;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public void aggiungiPizzaExtra(Pizza pizza) {
        this.pizzeExtra.add(pizza);
        pizza.getIngredientiExtra().add(this);
    }

    public void rimuoviPizzaExtra(Pizza pizza) {
        this.pizzeExtra.remove(pizza);
        pizza.getIngredientiExtra().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingrediente that = (Ingrediente) o;
        return Double.compare(that.prezzo, prezzo) == 0 && Objects.equals(idIngrediente, that.idIngrediente) && Objects.equals(nome, that.nome) && Objects.equals(pizzeExtra, that.pizzeExtra) && Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idIngrediente, nome, prezzo, pizzeExtra, menu);
    }
}