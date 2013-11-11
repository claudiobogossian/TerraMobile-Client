package br.org.funcate.mobile.task;

import java.io.Serializable;

import br.org.funcate.mobile.form.Form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;

/**
 * 
 * Model
 * 
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task implements Serializable {

	private static final long serialVersionUID = 1L;

	// id is generated by the database and set on the object automatically
	@DatabaseField(generatedId = true, columnName = "id")
	private Integer id;
	@DatabaseField
	private Integer featureCode; // O idenfificador da feição (SSQQQNNNN),
	@DatabaseField
	private Integer idAddress; // o código do logradouro
	@DatabaseField
	private String addressName; // nome do Logradouro
	@DatabaseField
	private Integer buildingNumber; // numeração predial
	@DatabaseField
	private Double latitude; // coordenadas do centroide da feição, no caso de Bauru o Lote.
	@DatabaseField
	private Double longitude;
	@DatabaseField
	private boolean syncronized; // sincronizado com o servidor?
	@DatabaseField(canBeNull = false, foreign = true)
	private Form form;

	public Task() {
		// TODO Auto-generated constructor stub
	}

	public Task(Integer id, Integer featureCode, Integer idAddress,
			String addressName, Integer buildingNumber, Double latitude,
			Double longitude, Boolean syncronized, Form form) {
		super();
		this.id = id;
		this.featureCode = featureCode;
		this.idAddress = idAddress;
		this.addressName = addressName;
		this.buildingNumber = buildingNumber;
		this.latitude = latitude;
		this.longitude = longitude;
		this.syncronized = syncronized;
		this.form = form;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getFeatureCode() {
		return featureCode;
	}

	public void setFeatureCode(Integer featureCode) {
		this.featureCode = featureCode;
	}

	public Integer getIdAddress() {
		return idAddress;
	}

	public void setIdAddress(Integer idAddress) {
		this.idAddress = idAddress;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}

	public Integer getBuildingNumber() {
		return buildingNumber;
	}

	public void setBuildingNumber(Integer buildingNumber) {
		this.buildingNumber = buildingNumber;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public boolean isSyncronized() {
		return syncronized;
	}

	public void setSyncronized(boolean syncronized) {
		this.syncronized = syncronized;
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	@Override
	public String toString() {
		return "\n Rua : " + addressName
				+ "\n Número : " + buildingNumber;
	}
	
	
}