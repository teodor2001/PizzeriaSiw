package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Pizza {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idPizza;

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    private String descrizione;

    @NotNull(message = "Il prezzo base è obbligatorio")
    @DecimalMin(value = "0.01", message = "Il prezzo base deve essere maggiore di 0")
    private double prezzoBase;

    @ManyToMany
    @JoinTable(
        name = "pizza_ingrediente_base",
        joinColumns = @JoinColumn(name = "pizza_id"),
        inverseJoinColumns = @JoinColumn(name = "ingrediente_id")
    )
    private List<Ingrediente> ingredientiBase = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "sconto_id")
    private Sconto scontoApplicato = new Sconto();

    @ManyToMany
    @JoinTable(name = "pizza_ingrediente_extra", joinColumns = @JoinColumn(name = "pizza_id"), inverseJoinColumns = @JoinColumn(name = "ingrediente_id"))
    private List<Ingrediente> ingredientiExtra = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
    
    private String imageUrl;

    @Transient
    private double prezzoConExtra;
    @Transient
    private double prezzoScontato;

    // Costruttori
    public Pizza() {
    }

    public Pizza(String nome, String descrizione, double prezzoBase, List<Ingrediente> ingredientiBase) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzoBase = prezzoBase;
        this.ingredientiBase = ingredientiBase;
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

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public double getPrezzoBase() {
        return prezzoBase;
    }

    public void setPrezzoBase(double prezzoBase) {
        this.prezzoBase = prezzoBase;
    }

    public List<Ingrediente> getIngredientiBase() {
        return ingredientiBase;
    }

    public void setIngredientiBase(List<Ingrediente> ingredientiBase) {
        this.ingredientiBase = ingredientiBase;
    }

    public Sconto getScontoApplicato() {
        return scontoApplicato;
    }

    public void setScontoApplicato(Sconto scontoApplicato) {
        this.scontoApplicato = scontoApplicato;
    }

    public List<Ingrediente> getIngredientiExtra() {
        return ingredientiExtra;
    }

    public void setIngredientiExtra(List<Ingrediente> ingredientiExtra) {
        this.ingredientiExtra = ingredientiExtra;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public double getPrezzoConExtra() {
        return prezzoBase + ingredientiExtra.stream().mapToDouble(Ingrediente::getPrezzo).sum();
    }

    public double getPrezzoScontato() {
        double prezzoAttuale = getPrezzoConExtra();
        if (scontoApplicato != null) {
            return prezzoAttuale * (1 - (scontoApplicato.getPercentuale() / 100));
        }
        return prezzoAttuale;
    }

    public void aggiungiIngredienteExtra(Ingrediente ingrediente) {
        this.ingredientiExtra.add(ingrediente);
        ingrediente.getPizzeExtra().add(this);
    }

    public void rimuoviIngredienteExtra(Ingrediente ingrediente) {
        this.ingredientiExtra.remove(ingrediente);
        ingrediente.getPizzeExtra().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pizza pizza = (Pizza) o;
        return Double.compare(pizza.prezzoBase, prezzoBase) == 0 && Double.compare(pizza.prezzoConExtra, prezzoConExtra) == 0 && Double.compare(pizza.prezzoScontato, prezzoScontato) == 0 && Objects.equals(idPizza, pizza.idPizza) && Objects.equals(nome, pizza.nome) && Objects.equals(descrizione, pizza.descrizione) && Objects.equals(ingredientiBase, pizza.ingredientiBase) && Objects.equals(scontoApplicato, pizza.scontoApplicato) && Objects.equals(ingredientiExtra, pizza.ingredientiExtra) && Objects.equals(menu, pizza.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPizza, nome, descrizione, prezzoBase, ingredientiBase, scontoApplicato, ingredientiExtra, menu, prezzoConExtra, prezzoScontato);
    }
}