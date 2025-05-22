package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Pizza {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idPizza;

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotNull(message = "Il prezzo base è obbligatorio")
    @DecimalMin(value = "0.01", message = "Il prezzo base deve essere maggiore di 0")
    private double prezzoBase;

    @ManyToMany
    @JoinTable(
        name = "pizza_ingrediente_base",
        joinColumns = @JoinColumn(name = "pizza_id"),
        inverseJoinColumns = @JoinColumn(name = "ingrediente_id")
    )
    private Set<Ingrediente> ingredientiBase = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "sconto_id")
    private Sconto scontoApplicato;

    @Transient
    private Set<Ingrediente> ingredientiExtraDisponibili;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    private String imageUrl;

    @Transient
    private double prezzoScontato;

    public Pizza() {
    }

    public Pizza(String nome, double prezzoBase, Set<Ingrediente> ingredientiBase) {
        this.nome = nome;
        this.prezzoBase = prezzoBase;
        this.ingredientiBase = ingredientiBase != null ? ingredientiBase : new HashSet<>();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getIdPizza() {
        return idPizza;
    }

    public void setIdPizza(Long idPizza) {
        this.idPizza = idPizza;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPrezzoBase() {
        return prezzoBase;
    }

    public void setPrezzoBase(double prezzoBase) {
        this.prezzoBase = prezzoBase;
    }

    public Set<Ingrediente> getIngredientiBase() {
        return ingredientiBase;
    }

    public void setIngredientiBase(Set<Ingrediente> ingredientiBase) {
        this.ingredientiBase = ingredientiBase;
    }

    public Sconto getScontoApplicato() {
        return scontoApplicato;
    }

    public void setScontoApplicato(Sconto scontoApplicato) {
        this.scontoApplicato = scontoApplicato;
    }

    public Set<Ingrediente> getIngredientiExtraDisponibili() {
        return ingredientiExtraDisponibili;
    }

    public void setIngredientiExtraDisponibili(Set<Ingrediente> tuttiGliIngredienti) {
        if (tuttiGliIngredienti == null) {
            this.ingredientiExtraDisponibili = new HashSet<>();
        } else {
            this.ingredientiExtraDisponibili = new HashSet<>(tuttiGliIngredienti);
        }
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public double getPrezzoScontato() {
        if (scontoApplicato != null && scontoApplicato.getPercentuale() > 0) {
            return prezzoBase * (1 - (scontoApplicato.getPercentuale() / 100.0));
        }
        return prezzoBase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pizza pizza = (Pizza) o;
        return Objects.equals(idPizza, pizza.idPizza) &&
               Objects.equals(nome, pizza.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPizza, nome);
    }
}