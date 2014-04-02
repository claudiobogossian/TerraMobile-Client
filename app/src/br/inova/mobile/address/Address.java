package br.inova.mobile.address;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class Address implements Serializable {
        @DatabaseField(
                       generatedId = true,
                       columnName = "_id",
                       allowGeneratedIdInsert = true)
        private Integer id;
        
        @DatabaseField
        private String  name;        // Logradouro
                                      
        @DatabaseField
        private String  number;      // Base de dados pode ter String no número
                                      
        @DatabaseField
        private String  extra;       // complemento
                                      
        @DatabaseField
        private Double  coordx;      // Longitude
                                      
        @DatabaseField
        private Double  coordy;      // Latitude
                                      
        @DatabaseField
        private String  postalCode;
        
        @DatabaseField
        private String  city;
        
        @DatabaseField
        private String  state;
        
        @DatabaseField
        private String  featureId;   // O idenfificador da feição (SSQQQNNNN),
                                      
        @DatabaseField
        private String  neighborhood;
        
        public Address() {}
        
        public Address(
                       Integer id,
                       String name,
                       String number,
                       String extra,
                       Double coordx,
                       Double coordy,
                       String postalCode,
                       String city,
                       String state,
                       String featureId,
                       String neighborhood) {
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
                this.neighborhood = neighborhood;
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
                JSONObject data = new JSONObject();
                
                try {
                        data.put("id", id);
                        data.put("name", name);
                        data.put("number", number);
                        data.put("extra", extra);
                        data.put("coordx", coordx);
                        data.put("coordy", coordy);
                        data.put("postalCode", postalCode);
                        data.put("city", city);
                        data.put("state", state);
                        data.put("featureId", featureId);
                        data.put("neighborhood", neighborhood);
                }
                catch (JSONException e) {
                        e.printStackTrace();
                }
                
                return data.toString();
        }
        
}
