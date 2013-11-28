package br.org.funcate.baurudigital.server.task;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import br.org.funcate.baurudigital.server.address.Address;
import br.org.funcate.baurudigital.server.form.Form;
import br.org.funcate.baurudigital.server.user.User;

@Entity
@Table(name = "\"task\"")
public class Task implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Integer _id;
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=Address.class)
	private Address address;
	
	@ManyToOne(cascade=CascadeType.REFRESH, targetEntity=User.class)
	private User user;
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=Form.class)
	private Form form;
	
	private boolean done; // sincronizado com o servidor?

	public Task() {
		// TODO Auto-generated constructor stub
	}

	public Task(Integer _id, Address address, User user, Form form,
			boolean done) {
		super();
		this._id = _id;
		this.address = address;
		this.user = user;
		this.form = form;
		this.done = done;
	}

	public Integer get_Id() {
		return _id;
	}

	public void set_Id(Integer _id) {
		this._id = _id;
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

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}