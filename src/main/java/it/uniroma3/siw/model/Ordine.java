package it.uniroma3.siw.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
public class Ordine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "ordine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdineItem> items = new ArrayList<>();

    private String indirizzoConsegna;
    private String orarioConsegna;
    private LocalDateTime dataOrdine;
    private String stato; // "IN_ATTESA", "IN_PREPARAZIONE", "IN_CONSEGNA", "CONSEGNATO"

    public Ordine() {
        this.dataOrdine = LocalDateTime.now();
        this.stato = "IN_ATTESA";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<OrdineItem> getItems() {
        return items;
    }

    public void setItems(List<OrdineItem> items) {
        this.items = items;
    }

    public String getIndirizzoConsegna() {
        return indirizzoConsegna;
    }

    public void setIndirizzoConsegna(String indirizzoConsegna) {
        this.indirizzoConsegna = indirizzoConsegna;
    }

    public String getOrarioConsegna() {
        return orarioConsegna;
    }

    public void setOrarioConsegna(String orarioConsegna) {
        this.orarioConsegna = orarioConsegna;
    }

    public LocalDateTime getDataOrdine() {
        return dataOrdine;
    }

    public void setDataOrdine(LocalDateTime dataOrdine) {
        this.dataOrdine = dataOrdine;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Ordine other = (Ordine) obj;
        return Objects.equals(id, other.id);
    }
} 