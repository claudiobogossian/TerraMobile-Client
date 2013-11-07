package br.org.funcate.mobile.form;

import com.j256.ormlite.field.DatabaseField;

public class Form {

	@DatabaseField(generatedId = true, columnName = "id")
	public Integer id;
	@DatabaseField
	public String photo;
	@DatabaseField
	public String date;
	@DatabaseField
	public String latitude;
	@DatabaseField
	public String longitude;
	@DatabaseField
	public String number;
	@DatabaseField(canBeNull = true)
	public String if1;
	@DatabaseField(canBeNull = true)
	public String if2;
	@DatabaseField
	public String address; // logradouro
	@DatabaseField
	public String postalCode;
	@DatabaseField
	public String city;
	@DatabaseField
	public String state;

	public Form() {
		// TODO Auto-generated constructor stub
	}

	public Form(Integer id, String photo, String date, String latitude,
			String longitude, String number, String if1, String if2,
			String address, String postalCode, String city, String state) {
		super();
		this.id = id;
		this.photo = photo;
		this.date = date;
		this.latitude = latitude;
		this.longitude = longitude;
		this.number = number;
		this.if1 = if1;
		this.if2 = if2;
		this.address = address;
		this.postalCode = postalCode;
		this.city = city;
		this.state = state;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getIf1() {
		return if1;
	}

	public void setIf1(String if1) {
		this.if1 = if1;
	}

	public String getIf2() {
		return if2;
	}

	public void setIf2(String if2) {
		this.if2 = if2;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

}