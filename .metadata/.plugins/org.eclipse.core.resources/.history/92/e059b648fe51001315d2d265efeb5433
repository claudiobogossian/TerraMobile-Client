package br.org.funcate.baurudigital.user;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "\"user\"")
public class User implements Serializable {

	@Id
	@GeneratedValue
	private Integer id;
	
	private String name;
	
	private String login;
	
	private String password;
	
	private String hash;

	public User() {
		// TODO Auto-generated constructor stub
	}

	public User(String userHash) {
		this.hash = userHash;
	}
	
	public User(Integer id, String name, String login, String password, String userHash) {
		super();
		this.id = id;
		this.name = name;
		this.login = login;
		this.password = password;
		this.hash = userHash;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", login=" + login
				+ ", password=" + password + ", hash=" + hash + "]";
	}

}
