	package it.uniroma3.siw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
public class Bevanda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private double prezzo;

    @NotNull
    @DecimalMin(value = "0.1", message = "La quantità deve essere almeno 0.1 L")
    private double quantità;

    @ManyToOne
    private Menu menu;

    public Bevanda() {
    }

    public Bevanda(String nome, double prezzo, double quantità) {
        this.nome = nome;
        this.prezzo = prezzo;
        this.quantità = quantità;
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

    public double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }

    public double getQuantità() {
        return quantità;
    }

    public void setQuantità(double quantità) {
        this.quantità = quantità;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bevanda bevanda = (Bevanda) o;
        return Double.compare(bevanda.prezzo, prezzo) == 0 && Double.compare(bevanda.quantità, quantità) == 0 && Objects.equals(id, bevanda.id) && Objects.equals(nome, bevanda.nome) && Objects.equals(menu, bevanda.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, prezzo, quantità, menu);
    }
}