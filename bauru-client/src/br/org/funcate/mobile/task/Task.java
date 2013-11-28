package br.org.funcate.mobile.task;

import java.io.Serializable;

import br.org.funcate.mobile.address.Address;
import br.org.funcate.mobile.form.Form;
import br.org.funcate.mobile.user.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task implements Serializable {

	private static final long serialVersionUID = 1L;

	@DatabaseField( columnName = "_id", generatedId = true, canBeNull = false, allowGeneratedIdInsert = true)
	private Integer _id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, foreignAutoCreate = true, maxForeignAutoRefreshLevel = 3)
	private Address address;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, foreignAutoCreate = true, maxForeignAutoRefreshLevel = 3)
	private User user;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, foreignAutoCreate = true, maxForeignAutoRefreshLevel = 3)
	private Form form;
	@DatabaseField
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

	@Override
	public String toString() {
		return "" + address;
	}

}