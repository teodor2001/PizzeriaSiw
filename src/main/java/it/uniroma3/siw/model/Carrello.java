package it.uniroma3.siw.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Carrello {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", unique = true)
    private Cliente cliente;

    @OneToMany(mappedBy = "carrello", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ElementoCarrello> elementi;

    private LocalDateTime dataCreazione;

    private LocalDateTime dataUltimaModifica;

    @Transient
    private double totaleComplessivo;

    public Carrello() {
        this.elementi = new ArrayList<>();
        this.dataCreazione = LocalDateTime.now();
        this.dataUltimaModifica = LocalDateTime.now();
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

    public List<ElementoCarrello> getElementi() {
        return elementi;
    }

    public void setElementi(List<ElementoCarrello> elementi) {
        this.elementi = elementi;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public LocalDateTime getDataUltimaModifica() {
        return dataUltimaModifica;
    }

    public void setDataUltimaModifica(LocalDateTime dataUltimaModifica) {
        this.dataUltimaModifica = dataUltimaModifica;
    }

    public double getTotaleComplessivo() {
        this.totaleComplessivo = 0.0;
        if (this.elementi != null) {
            for (ElementoCarrello elemento : this.elementi) {
                if (elemento != null) {
                    elemento.calcolaPrezzoUnitario(); // Ricalcola il prezzo unitario per ogni elemento
                    this.totaleComplessivo += elemento.getPrezzoTotaleElemento();
                }
            }
        }
        return this.totaleComplessivo;
    }
    
    public void aggiungiElemento(ElementoCarrello elemento) {
        if (this.elementi == null) {
            this.elementi = new ArrayList<>();
        }
        this.elementi.add(elemento);
        elemento.setCarrello(this);
        this.dataUltimaModifica = LocalDateTime.now();
    }
    
    public void rimuoviElemento(ElementoCarrello elemento) {
        if (this.elementi != null) {
            this.elementi.remove(elemento);
            elemento.setCarrello(null); // Rimuove l'associazione
        }
        this.dataUltimaModifica = LocalDateTime.now();
    }

	@Override
	public int hashCode() {
		return Objects.hash(cliente, dataCreazione, dataUltimaModifica, elementi, id, totaleComplessivo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Carrello other = (Carrello) obj;
		return Objects.equals(cliente, other.cliente) && Objects.equals(dataCreazione, other.dataCreazione)
				&& Objects.equals(dataUltimaModifica, other.dataUltimaModifica)
				&& Objects.equals(elementi, other.elementi) && Objects.equals(id, other.id)
				&& Double.doubleToLongBits(totaleComplessivo) == Double.doubleToLongBits(other.totaleComplessivo);
	}

}