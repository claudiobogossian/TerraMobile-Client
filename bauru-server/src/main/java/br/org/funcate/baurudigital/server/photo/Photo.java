package br.org.funcate.baurudigital.server.photo;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import br.org.funcate.baurudigital.server.form.Form;
@Entity
@Table(name = "\"photo\"")
@JsonIgnoreProperties({"blob"})
public class Photo implements Serializable {
	@Id
	@GeneratedValue
	private Integer id;
	@Transient
	private String base64;
	@Lob
	private byte[] blob;
	
	private String path;
	@ManyToOne(cascade=CascadeType.ALL, targetEntity=Form.class)
	private Form form;

	public Photo() {
		// TODO Auto-generated constructor stub
	}

	public Photo(Integer id, String base64, byte[] blob, String path, Form form) {
		super();
		this.id = id;
		this.path = path;
		this.form = form;
		this.blob = blob;
		this.base64 = base64;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getBase64() {
		return base64;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}

	public byte[] getBlob() {
		return blob;
	}

	public void setBlob(byte[] blob) {
		this.blob = blob;
	}

}
