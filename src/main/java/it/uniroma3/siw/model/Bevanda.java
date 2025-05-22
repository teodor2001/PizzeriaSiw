package it.uniroma3.siw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank; // Added
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
public class Bevanda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome della bevanda non può essere vuoto.")
    private String nome;

    @NotNull(message = "Il prezzo non può essere nullo.") // Added validation
    @DecimalMin(value = "0.01", message = "Il prezzo deve essere maggiore di 0.")
    private double prezzo;

    @NotNull(message = "La quantità non può essere nulla.")
    @DecimalMin(value = "0.1", message = "La quantità deve essere almeno 0.1 L")
    private double quantità;

    @ManyToOne
    private Menu menu;

    private String imageUrl;

    public Bevanda() {
    }

    public Bevanda(String nome, double prezzo, double quantità) {
        this.nome = nome;
        this.prezzo = prezzo;
        this.quantità = quantità;
    }

    // Getters and Setters
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bevanda bevanda = (Bevanda) o;
        return Double.compare(bevanda.prezzo, prezzo) == 0 &&
               Double.compare(bevanda.quantità, quantità) == 0 &&
               Objects.equals(id, bevanda.id) &&
               Objects.equals(nome, bevanda.nome) &&
               Objects.equals(menu, bevanda.menu) &&
               Objects.equals(imageUrl, bevanda.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, prezzo, quantità, menu, imageUrl);
    }
}