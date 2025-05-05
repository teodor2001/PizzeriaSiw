package it.uniroma3.siw.model;

import jakarta.persistence.*;

@Entity
public class Amministratore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAdmin;
    private String nome;
    private String cognome;
    private String email;
    private String password; // In produzione, questa andrebbe gestita in modo sicuro (es. hashing)

    @ManyToOne
    @JoinColumn(name = "pizzeria_id")
    private Pizzeria pizzeria;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menuAssociato;

    //Costruttore predefinito se no JPA si arrabbia
    public Amministratore() {
    }

    //Costruttore vero
    public Amministratore(String nome, String cognome, String email, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
    }

    public Long getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(Long idAdmin) {
        this.idAdmin = idAdmin;
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

    public Pizzeria getPizzeria() {
        return pizzeria;
    }

    public void setPizzeria(Pizzeria pizzeria) {
        this.pizzeria = pizzeria;
    }

    public Menu getMenuAssociato() {
        return menuAssociato;
    }

    public void setMenuAssociato(Menu menuAssociato) {
        this.menuAssociato = menuAssociato;
    }
}
