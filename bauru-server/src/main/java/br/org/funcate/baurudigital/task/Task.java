package br.org.funcate.baurudigital.task;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import br.org.funcate.baurudigital.address.Address;
import br.org.funcate.baurudigital.form.Form;
import br.org.funcate.baurudigital.user.User;

@Entity
@Table(name = "task")
public class Task implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Integer id;
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=Address.class)
	private Address address;
	
	@ManyToOne(cascade=CascadeType.REFRESH, targetEntity=User.class)
	private User user;
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=Form.class)
	private Form form;
	
	private boolean syncronized; // sincronizado com o servidor?

	public Task() {
		// TODO Auto-generated constructor stub
	}

	public Task(Integer id, Address address, User user, Form form,
			boolean syncronized) {
		super();
		this.id = id;
		this.address = address;
		this.user = user;
		this.form = form;
		this.syncronized = syncronized;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public boolean isSyncronized() {
		return syncronized;
	}

	public void setSyncronized(boolean syncronized) {
		this.syncronized = syncronized;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}