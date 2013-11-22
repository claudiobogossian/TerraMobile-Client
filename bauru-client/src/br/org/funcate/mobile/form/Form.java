package br.org.funcate.mobile.form;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;

public class Form implements Serializable {

	@DatabaseField(generatedId = true, columnName = "id")
	private Integer id;
	@DatabaseField
	private Date date;
	@DatabaseField
	private Double coordx;
	@DatabaseField
	private Double coordy;
	@DatabaseField
	private String info1;
	@DatabaseField
	private String info2;

	public Form() {
		// TODO Auto-generated constructor stub
	}

	public Form(Integer id, Date date, Double coordx, Double coordy,
			String info1, String info2) {
		super();
		this.id = id;
		this.date = date;
		this.coordx = coordx;
		this.coordy = coordy;
		this.info1 = info1;
		this.info2 = info2;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getCoordx() {
		return coordx;
	}

	public void setCoordx(Double coordx) {
		this.coordx = coordx;
	}

	public Double getCoordy() {
		return coordy;
	}

	public void setCoordy(Double coordy) {
		this.coordy = coordy;
	}

	public String getInfo1() {
		return info1;
	}

	public void setInfo1(String info1) {
		this.info1 = info1;
	}

	public String getInfo2() {
		return info2;
	}

	public void setInfo2(String info2) {
		this.info2 = info2;
	}

}