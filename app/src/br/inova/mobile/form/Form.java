package br.inova.mobile.form;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class Form implements Serializable {
        
        @DatabaseField
        private String  asphaltGuide;
        
        @DatabaseField
        private Double  coordx;
        
        @DatabaseField
        private Double  coordy;
        
        @DatabaseField
        private Date    date;
        
        @DatabaseField
        private String  energy;
        
        @DatabaseField(
                       generatedId = true,
                       columnName = "id",
                       allowGeneratedIdInsert = true)
        private Integer id;
        
        @DatabaseField
        private String  info1;
        
        @DatabaseField
        private String  info2;
        
        @DatabaseField
        private String  numberConfirmation;
        
        @DatabaseField
        private String  otherNumbers;
        
        @DatabaseField
        private String  pavimentation;
        
        @DatabaseField
        private String  pluvialGallery;
        
        @DatabaseField
        private String  primaryUse;
        
        @DatabaseField
        private String  publicIlumination;
        
        @DatabaseField
        private String  secondaryUse;
        
        @DatabaseField
        private String  variance;          // Desconformidade
                                            
        public Form() {}
        
        public Form(
                    Integer id,
                    Date date,
                    Double coordx,
                    Double coordy,
                    String info1,
                    String info2,
                    String numberConfirmation,
                    String variance,
                    String otherNumbers,
                    String primaryUse,
                    String secondaryUse,
                    String pavimentation,
                    String asphaltGuide,
                    String publicIlumination,
                    String energy,
                    String pluvialGallery) {
                super();
                this.id = id;
                this.date = date;
                this.coordx = coordx;
                this.coordy = coordy;
                this.info1 = info1;
                this.info2 = info2;
                this.numberConfirmation = numberConfirmation;
                this.variance = variance;
                this.otherNumbers = otherNumbers;
                this.primaryUse = primaryUse;
                this.secondaryUse = secondaryUse;
                this.pavimentation = pavimentation;
                this.asphaltGuide = asphaltGuide;
                this.publicIlumination = publicIlumination;
                this.energy = energy;
                this.pluvialGallery = pluvialGallery;
        }
        
        public String getAsphaltGuide() {
                return asphaltGuide;
        }
        
        public Double getCoordx() {
                return coordx;
        }
        
        public Double getCoordy() {
                return coordy;
        }
        
        public Date getDate() {
                return date;
        }
        
        public String getEnergy() {
                return energy;
        }
        
        public Integer getId() {
                return id;
        }
        
        public String getInfo1() {
                return info1;
        }
        
        public String getInfo2() {
                return info2;
        }
        
        public String getNumberConfirmation() {
                return numberConfirmation;
        }
        
        public String getOtherNumbers() {
                return otherNumbers;
        }
        
        public String getPavimentation() {
                return pavimentation;
        }
        
        public String getPluvialGallery() {
                return pluvialGallery;
        }
        
        public String getPrimaryUse() {
                return primaryUse;
        }
        
        public String getPublicIlumination() {
                return publicIlumination;
        }
        
        public String getSecondaryUse() {
                return secondaryUse;
        }
        
        public String getVariance() {
                return variance;
        }
        
        public void setAsphaltGuide(String asphaltGuide) {
                this.asphaltGuide = asphaltGuide;
        }
        
        public void setCoordx(Double coordx) {
                this.coordx = coordx;
        }
        
        public void setCoordy(Double coordy) {
                this.coordy = coordy;
        }
        
        public void setDate(Date date) {
                this.date = date;
        }
        
        public void setEnergy(String energy) {
                this.energy = energy;
        }
        
        public void setId(Integer id) {
                this.id = id;
        }
        
        public void setInfo1(String info1) {
                this.info1 = info1;
        }
        
        public void setInfo2(String info2) {
                this.info2 = info2;
        }
        
        public void setNumberConfirmation(String numberConfirmation) {
                this.numberConfirmation = numberConfirmation;
        }
        
        public void setOtherNumbers(String otherNumbers) {
                this.otherNumbers = otherNumbers;
        }
        
        public void setPavimentation(String pavimentation) {
                this.pavimentation = pavimentation;
        }
        
        public void setPluvialGallery(String pluvialGallery) {
                this.pluvialGallery = pluvialGallery;
        }
        
        public void setPrimaryUse(String primaryUse) {
                this.primaryUse = primaryUse;
        }
        
        public void setPublicIlumination(String publicIlumination) {
                this.publicIlumination = publicIlumination;
        }
        
        public void setSecondaryUse(String secondaryUse) {
                this.secondaryUse = secondaryUse;
        }
        
        public void setVariance(String variance) {
                this.variance = variance;
        }
        
        @Override
        public String toString() {
                JSONObject data = new JSONObject();
                
                try {
                        data.put("id", id);
                        data.put("date", date);
                        data.put("coordx", coordx);
                        data.put("coordy", coordy);
                        data.put("info1", info1);
                        data.put("info2", info2);
                        data.put("numberConfirmation", numberConfirmation);
                        data.put("variance", variance);
                        data.put("otherNumbers", otherNumbers);
                        data.put("primaryUse", primaryUse);
                        data.put("secondaryUse", secondaryUse);
                        data.put("pavimentation", pavimentation);
                        data.put("asphaltGuide", asphaltGuide);
                        data.put("publicIlumination", publicIlumination);
                        data.put("energy", energy);
                        data.put("pluvialGallery", pluvialGallery);
                }
                catch (JSONException e) {
                        e.printStackTrace();
                }
                
                return data.toString();
        }
        
}
