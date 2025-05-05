package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
public class Pizza {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long idPizza;
    private String nome;
    private String descrizione;
    private double prezzoBase;
    @ElementCollection
    private List<String> ingredientiBase = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "sconto_id")
    private Sconto scontoApplicato;

    @ManyToMany
    @JoinTable(
        name = "pizza_ingrediente_extra",
        joinColumns = @JoinColumn(name = "pizza_id"),
        inverseJoinColumns = @JoinColumn(name = "ingrediente_id")
    )
    private List<Ingrediente> ingredientiExtra = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Transient
    private double prezzoConExtra;
    @Transient
    private double prezzoScontato;

    // Costruttore base se no JPA si arrabbia
    public Pizza() {
    }
    
    //Costruttore vero, senza sconti per gli utenti non loggati
    public Pizza(String nome, String descrizione, double prezzoBase, List<String> ingredientiBase) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzoBase = prezzoBase;
        this.ingredientiBase = ingredientiBase;
    }
    
    //Costruttore per applicare anche gli sconti agli utenti loggati sulle pizze base
    public Pizza(String nome, String descrizione, double prezzoBase, List<String> ingredientiBase, Sconto scontoApplicato) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzoBase = prezzoBase;
        this.ingredientiBase = ingredientiBase;
        this.scontoApplicato = scontoApplicato;
    }
    
    //Costruttore per gestire le pizze con condimenti extra
    public Pizza(String nome, String descrizione, double prezzoBase, List<String> ingredientiBase, List<Ingrediente> ingredientiExtra) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzoBase = prezzoBase;
        this.ingredientiBase = ingredientiBase;
        this.ingredientiExtra = ingredientiExtra;
    }
    
    
    //Costruttore per gestire le pizze con ingredienti extra e anche sconti per utenti loggati
    public Pizza(String nome, String descrizione, double prezzoBase, List<String> ingredientiBase, Sconto scontoApplicato, List<Ingrediente> ingredientiExtra) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzoBase = prezzoBase;
        this.ingredientiBase = ingredientiBase;
        this.scontoApplicato = scontoApplicato;
        this.ingredientiExtra = ingredientiExtra;
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

    public List<String> getIngredientiBase() {
        return ingredientiBase;
    }

    public void setIngredientiBase(List<String> ingredientiBase) {
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
            return prezzoAttuale * (1 - (scontoApplicato.getPercentuale()/100));
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