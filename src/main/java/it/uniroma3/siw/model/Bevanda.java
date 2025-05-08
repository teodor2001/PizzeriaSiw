	package it.uniroma3.siw.model;
	
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Objects;
	
	import jakarta.persistence.*;
	
	@Entity
	public class Bevanda {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;
		private String nome;
		private double prezzo;
		private String descrizione;
	
		@ManyToOne // Modifica: Relazione many-to-one con Menu
		private Menu menu;
	
		// Costruttore predefinito se no JPA si arrabbia
		public Bevanda() {
		}
	
		// Costruttore vero
		public Bevanda(String nome, double prezzo, String descrizione) {
			this.nome = nome;
			this.prezzo = prezzo;
			this.descrizione = descrizione;
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
	
		public String getDescrizione() {
			return descrizione;
		}
	
		public void setDescrizione(String descrizione) {
			this.descrizione = descrizione;
		}
	
		// Da valutare se ha senso tenere questo tipo di metodi per casi particolari
		public Menu getMenu() {
			return menu;
		}
	
		public void setMenu(Menu menu) {
			this.menu = menu;
		}
	
		@Override
		public int hashCode() {
			return Objects.hash(descrizione, id, nome, prezzo);
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Bevanda other = (Bevanda) obj;
			return Objects.equals(descrizione, other.descrizione) && Objects.equals(id, other.id)
					&& Objects.equals(nome, other.nome)
					&& Double.doubleToLongBits(prezzo) == Double.doubleToLongBits(other.prezzo);
		}
	
	}