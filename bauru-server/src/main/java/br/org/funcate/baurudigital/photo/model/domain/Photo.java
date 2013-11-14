package br.org.funcate.baurudigital.photo.model.domain;

import java.io.Serializable;

import br.org.funcate.baurudigital.form.model.domain.Form;

public class Photo implements Serializable {
	
	private Integer id;
	
	private String blob;
	
	private String path;
	//(canBeNull = false, foreign = true)
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
