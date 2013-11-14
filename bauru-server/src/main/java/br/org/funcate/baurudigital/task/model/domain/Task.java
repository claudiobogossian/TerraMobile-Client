package br.org.funcate.baurudigital.task.model.domain;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import br.org.funcate.baurudigital.address.model.domain.Address;
import br.org.funcate.baurudigital.form.model.domain.Form;
import br.org.funcate.baurudigital.user.model.domain.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task implements Serializable {

	private static final long serialVersionUID = 1L;


	private Integer id;
//	(canBeNull = false, foreign = true)
	private Address address;
//	(canBeNull = false, foreign = true)
	private User user;
//	(canBeNull = false, foreign = true)
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