package br.inova.mobile.photo;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import br.inova.mobile.form.Form;

import com.j256.ormlite.field.DatabaseField;

public class Photo implements Serializable {
        @DatabaseField
        private String  base64; // BASE64
                                
        @DatabaseField(
                       canBeNull = false,
                       foreign = true,
                       foreignAutoCreate = true,
                       foreignAutoRefresh = true)
        private Form    form;
        
        @DatabaseField(generatedId = true, columnName = "id")
        private Integer id;
        
        @DatabaseField
        private String  path;
        
        public Photo() {}
        
        public Photo(Integer id, String base64, String path, Form form) {
                super();
                this.id = id;
                this.base64 = base64;
                this.path = path;
                this.form = form;
        }
        
        public String getBase64() {
                return base64;
        }
        
        public Form getForm() {
                return form;
        }
        
        public Integer getId() {
                return id;
        }
        
        public String getPath() {
                return path;
        }
        
        public void setBase64(String base64) {
                this.base64 = base64;
        }
        
        public void setForm(Form form) {
                this.form = form;
        }
        
        public void setId(Integer id) {
                this.id = id;
        }
        
        public void setPath(String path) {
                this.path = path;
        }
        
        @Override
        public String toString() {
                JSONObject data = new JSONObject();
                
                try {
                        data.put("id", id);
                        data.put("base64", base64);
                        data.put("path", path);
                        data.put("form", form);
                }
                catch (JSONException e) {
                        e.printStackTrace();
                }
                
                return data.toString();
        }
        
}
