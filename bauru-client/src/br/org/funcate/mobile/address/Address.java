package br.org.funcate.mobile.address;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class Address implements Serializable {
	@DatabaseField(generatedId = true, columnName = "_id")
	private Integer id;
	@DatabaseField
	private String name; // Logradouro
	@DatabaseField
	private String number; // Base de dados pode ter String no número
	@DatabaseField
	private String extra; // complemento
	@DatabaseField
	private Double coordx;
	@DatabaseField
	private Double coordy;
	@DatabaseField
	private String postalCode;
	@DatabaseField
	private String city;
	@DatabaseField
	private String state;
	@DatabaseField
	private String featureId; // O idenfificador da feição (SSQQQNNNN),
	@DatabaseField
	private String neighborhood;

	public Address() {
		// TODO Auto-generated constructor stub
	}

	public Address(Integer id, String name, String number, String extra,
			Double coordx, Double coordy, String postalCode, String city,
			String state, String featureId) {
		super();
		this.id = id;
		this.name = name;
		this.number = number;
		this.extra = extra;
		this.coordx = coordx;
		this.coordy = coordy;
		this.postalCode = postalCode;
		this.city = city;
		this.state = state;
		this.featureId = featureId;
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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
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

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getFeatureId() {
		return featureId;
	}

	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}

	public String getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}

	@Override
	public String toString() {
		return "Address [name=" + name + ", number=" + number + ", extra="
				+ extra + ", coordx=" + coordx + ", coordy=" + coordy
				+ ", postalCode=" + postalCode + ", city=" + city + ", state="
				+ state + ", featureId=" + featureId + ", neighborhood="
				+ neighborhood + "]";
	}
}
