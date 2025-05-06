package it.uniroma3.siw.model;

import java.util.Objects;

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

	// Costruttore predefinito se no JPA si arrabbia
	public Amministratore() {
	}

	// Costruttore vero
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

	@Override
	public int hashCode() {
		return Objects.hash(cognome, email, idAdmin, menuAssociato, nome, password, pizzeria);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Amministratore other = (Amministratore) obj;
		return Objects.equals(cognome, other.cognome) && Objects.equals(email, other.email)
				&& Objects.equals(idAdmin, other.idAdmin) && Objects.equals(menuAssociato, other.menuAssociato)
				&& Objects.equals(nome, other.nome) && Objects.equals(password, other.password)
				&& Objects.equals(pizzeria, other.pizzeria);
	}
}
