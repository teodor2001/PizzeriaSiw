package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;
    private String nome;
    private String cognome;
    private String email;
    private String password; // In produzione, questa andrebbe gestita in modo sicuro (es. hashing)

    @ManyToMany
    @JoinTable(
        name = "cliente_pizzeria",
        joinColumns = @JoinColumn(name = "cliente_id"),
        inverseJoinColumns = @JoinColumn(name = "pizzeria_id")
    )
    private List<Pizzeria> pizzerie = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "cliente_sconto",
        joinColumns = @JoinColumn(name = "cliente_id"),
        inverseJoinColumns = @JoinColumn(name = "sconto_id")
    )
    private List<Sconto> sconti = new ArrayList<>();

    //Costruttore predefinito se no JPA si arrabbia
    public Cliente() {
    }

    //Costruttore vero
    public Cliente(String nome, String cognome, String email, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Pizzeria> getPizzerie() {
        return pizzerie;
    }

    public void setPizzerie(List<Pizzeria> pizzerie) {
        this.pizzerie = pizzerie;
    }

    public void aggiungiPizzeria(Pizzeria pizzeria) {
        this.pizzerie.add(pizzeria);
        pizzeria.getClienti().add(this); 
    }

    public void rimuoviPizzeria(Pizzeria pizzeria) {
        this.pizzerie.remove(pizzeria);
        pizzeria.getClienti().remove(this);
    }
    
    public List<Sconto> getSconti() {
        return sconti;
    }

    public void setSconti(List<Sconto> sconti) {
        this.sconti = sconti;
    }

    public void aggiungiSconto(Sconto sconto) {
        this.sconti.add(sconto);
        sconto.getClienti().add(this);
    }

    public void rimuoviSconto(Sconto sconto) {
        this.sconti.remove(sconto);
        sconto.getClienti().remove(this);
    }
}
