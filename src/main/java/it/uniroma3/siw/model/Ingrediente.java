package it.uniroma3.siw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double prezzo;

    @ManyToMany(mappedBy = "ingredientiExtra")
    private List<Pizza> pizzeExtra = new ArrayList<>(); // Dichiarazione di pizzeExtra

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    public Ingrediente() {
    }

    public Ingrediente(String nome, Double prezzo) {
        this.nome = nome;
        this.prezzo = prezzo;
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

    public Double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(Double prezzo) {
        this.prezzo = prezzo;
    }

    public List<Pizza> getPizzeExtra() { // Getter per pizzeExtra
        return pizzeExtra;
    }

    public void setPizzeExtra(List<Pizza> pizzeExtra) { // Setter per pizzeExtra
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
        return Objects.equals(id, that.id) && Objects.equals(nome, that.nome) && Objects.equals(prezzo, that.prezzo) && Objects.equals(pizzeExtra, that.pizzeExtra) && Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, prezzo, pizzeExtra, menu);
    }
}