package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
public class Pizzeria {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String nome;
    private String indirizzo;
    private String citta;
    private String email;
    private String telefono; // NUOVO ATTRIBUTO

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "menu_id", referencedColumnName = "id")
    private Menu menu;

    @ManyToMany(mappedBy = "pizzerie")
    private List<Cliente> clienti = new ArrayList<>();

    @OneToMany(mappedBy = "pizzeria")
    private List<Amministratore> amministratori = new ArrayList<>();

    // Costruttore predefinito se no JPA si arrabbia
    public Pizzeria() {
    }

    // Costruttore vero
    public Pizzeria(String nome, String indirizzo, String citta, String email, String telefono) {
        this.nome = nome;
        this.indirizzo = indirizzo;
        this.citta = citta;
        this.email = email;
        this.telefono = telefono;
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

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public List<Cliente> getClienti() {
        return clienti;
    }

    public void setClienti(List<Cliente> clienti) {
        this.clienti = clienti;
    }

    public List<Amministratore> getAmministratori() {
        return amministratori;
    }

    public void setAmministratori(List<Amministratore> amministratori) {
        this.amministratori = amministratori;
    }

    public void aggiungiAmministratore(Amministratore amministratore) {
        this.amministratori.add(amministratore);
        amministratore.setPizzeria(this);
    }

    public void rimuoviAmministratore(Amministratore amministratore) {
        this.amministratori.remove(amministratore);
        amministratore.setPizzeria(null);
    }

    public void aggiungiCliente(Cliente cliente) {
        this.clienti.add(cliente);
        cliente.getPizzerie().add(this);
    }

    public void rimuoviCliente(Cliente cliente) {
        this.clienti.remove(cliente);
        cliente.getPizzerie().remove(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(citta, email, id, indirizzo, nome, telefono);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pizzeria other = (Pizzeria) obj;
        return Objects.equals(citta, other.citta) && Objects.equals(email, other.email) && Objects.equals(id, other.id)
                && Objects.equals(indirizzo, other.indirizzo) && Objects.equals(nome, other.nome)
                && Objects.equals(telefono, other.telefono);
    }
}