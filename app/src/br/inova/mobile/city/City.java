package br.inova.mobile.city;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class City {
        @DatabaseField(generatedId = true, columnName = "_id", //It's necessary this "_" (undescore) for the consults in the ArrayAdapter.
                       allowGeneratedIdInsert = true)
        private Integer id;
        @DatabaseField
        private String  name;
        @DatabaseField
        private String  asciiName;
        @DatabaseField
        private String  state;
        @DatabaseField
        private Double  latitude;
        @DatabaseField
        private Double  longitude;
        
        public City() {}
        
        public City(
                    Integer id,
                    String name,
                    String asciiName,
                    String state,
                    Double latitude,
                    Double longitude) {
                super();
                this.id = id;
                this.name = name;
                this.asciiName = asciiName;
                this.state = state;
                this.latitude = latitude;
                this.longitude = longitude;
        }
        
        /**
         * The representation of the name without accents
         * 
         * @return the name without accents.
         * */
        public String getAsciiName() {
                return asciiName;
        }
        
        public Integer getId() {
                return id;
        }
        
        public Double getLatitude() {
                return latitude;
        }
        
        public Double getLongitude() {
                return longitude;
        }
        
        public String getName() {
                return name;
        }
        
        public String getState() {
                return state;
        }
        
        public void setAsciiName(String asciiName) {
                this.asciiName = asciiName;
        }
        
        public void setId(Integer id) {
                this.id = id;
        }
        
        public void setLatitude(Double latitude) {
                this.latitude = latitude;
        }
        
        public void setLongitude(Double longitude) {
                this.longitude = longitude;
        }
        
        public void setName(String name) {
                this.name = name;
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
                        data.put("asciiName", asciiName);
                        data.put("state", state);
                        data.put("latitude", latitude);
                        data.put("longitude", longitude);
                }
                catch (JSONException e) {
                        e.printStackTrace();
                }
                
                return data.toString();
        }
        
}
