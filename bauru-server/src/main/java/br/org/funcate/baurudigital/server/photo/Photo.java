package br.org.funcate.baurudigital.server.photo;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.org.funcate.baurudigital.server.form.Form;
@Entity
@Table(name = "\"photo\"")
public class Photo implements Serializable {
	@Id
	@GeneratedValue
	private Integer id;
	@Lob
	private String blob;
	
	private String path;
	@ManyToOne(cascade=CascadeType.ALL, targetEntity=Form.class)
	private Form form;

	public Photo() {
		// TODO Auto-generated constructor stub
	}

	public Photo(Integer id, String blob, String path, Form form) {
		super();
		this.id = id;
		this.blob = blob;
		this.path = path;
		this.form = form;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBlob() {
		return blob;
	}

	public void setBlob(String blob) {
		this.blob = blob;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

}
