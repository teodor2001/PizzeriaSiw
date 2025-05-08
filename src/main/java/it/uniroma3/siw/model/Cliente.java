package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @NotEmpty(message = "Il nome non può essere vuoto.")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Il nome deve contenere solo caratteri alfabetici.")
    private String nome;

    @NotEmpty(message = "Il cognome non può essere vuoto.")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Il cognome deve contenere solo caratteri alfabetici.")
    private String cognome;

    @NotEmpty(message = "L'email non può essere vuota.")
    @Email(message = "Formato email non valido.")
    private String email;
    
    @NotEmpty(message = "La password non può essere vuota.")
    @Size(min = 8, message = "La password deve contenere almeno 8 caratteri.")
    private String password;

	@ManyToMany
	@JoinTable(name = "cliente_pizzeria", joinColumns = @JoinColumn(name = "cliente_id"), inverseJoinColumns = @JoinColumn(name = "pizzeria_id"))
	private List<Pizzeria> pizzerie = new ArrayList<>();

	@ManyToMany
	@JoinTable(name = "cliente_sconto", joinColumns = @JoinColumn(name = "cliente_id"), inverseJoinColumns = @JoinColumn(name = "sconto_id"))
	private List<Sconto> sconti = new ArrayList<>();

	// Costruttore predefinito se no JPA si arrabbia
	public Cliente() {
	}

	// Costruttore vero
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

	@Override
	public int hashCode() {
		return Objects.hash(cognome, email, idCliente, nome, password, pizzerie, sconti);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cliente other = (Cliente) obj;
		return Objects.equals(cognome, other.cognome) && Objects.equals(email, other.email)
				&& Objects.equals(idCliente, other.idCliente) && Objects.equals(nome, other.nome)
				&& Objects.equals(password, other.password) && Objects.equals(pizzerie, other.pizzerie)
				&& Objects.equals(sconti, other.sconti);
	}
	
}
