package br.inova.mobile.address;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class Address implements Serializable {
        @DatabaseField
        private String  city;
        
        @DatabaseField
        private Double  coordx;      // Longitude
                                      
        @DatabaseField
        private Double  coordy;      // Latitude
                                      
        @DatabaseField
        private String  extra;       // complemento
                                      
        @DatabaseField
        private String  featureId;   // O idenfificador da feição (SSQQQNNNN),
                                      
        @DatabaseField(
                       generatedId = true,
                       columnName = "_id",
                       allowGeneratedIdInsert = true)
        private Integer id;
        
        @DatabaseField
        private String  name;        // Logradouro
                                      
        @DatabaseField
        private String  neighborhood;
        
        @DatabaseField
        private String  number;      // Base de dados pode ter String no número
                                      
        @DatabaseField
        private String  postalCode;
        
        @DatabaseField
        private String  state;
        
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
        
        public String getCity() {
                return city;
        }
        
        /**
         * @return the Longitude.
         */
        public Double getCoordx() {
                return coordx;
        }
        
        /**
         * @return the Latitude.
         */
        public Double getCoordy() {
                return coordy;
        }
        
        public String getExtra() {
                return extra;
        }
        
        public String getFeatureId() {
                return featureId;
        }
        
        public Integer getId() {
                return id;
        }
        
        public String getName() {
                return name;
        }
        
        public String getNeighborhood() {
                return neighborhood;
        }
        
        public String getNumber() {
                return number;
        }
        
        public String getPostalCode() {
                return postalCode;
        }
        
        public String getState() {
                return state;
        }
        
        public void setCity(String city) {
                this.city = city;
        }
        
        public void setCoordx(Double coordx) {
                this.coordx = coordx;
        }
        
        public void setCoordy(Double coordy) {
                this.coordy = coordy;
        }
        
        public void setExtra(String extra) {
                this.extra = extra;
        }
        
        public void setFeatureId(String featureId) {
                this.featureId = featureId;
        }
        
        public void setId(Integer id) {
                this.id = id;
        }
        
        public void setName(String name) {
                this.name = name;
        }
        
        public void setNeighborhood(String neighborhood) {
                this.neighborhood = neighborhood;
        }
        
        public void setNumber(String number) {
                this.number = number;
        }
        
        public void setPostalCode(String postalCode) {
                this.postalCode = postalCode;
        }
        
        public void setState(String state) {
                this.state = state;
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
