package br.inova.mobile.task;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import br.inova.mobile.address.Address;
import br.inova.mobile.form.Form;
import br.inova.mobile.user.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        public static long getSerialversionuid() {
                return serialVersionUID;
        }
        
        @DatabaseField(
                       canBeNull = false,
                       foreign = true,
                       foreignAutoRefresh = true,
                       foreignAutoCreate = true,
                       maxForeignAutoRefreshLevel = 3)
        private Address address;
        
        @DatabaseField
        private boolean done;   // sincronizado com o servidor?
                                 
        @DatabaseField(
                       canBeNull = false,
                       foreign = true,
                       foreignAutoRefresh = true,
                       foreignAutoCreate = true,
                       maxForeignAutoRefreshLevel = 3)
        private Form    form;
        
        @DatabaseField(
                       columnName = "_id",
                       generatedId = true,
                       canBeNull = false,
                       allowGeneratedIdInsert = true)
        private Integer id;
        
        @DatabaseField(
                       canBeNull = false,
                       foreign = true,
                       foreignAutoRefresh = true,
                       foreignAutoCreate = true,
                       maxForeignAutoRefreshLevel = 3)
        private User    user;
        
        public Task() {}
        
        public Task(
                    Integer id,
                    Address address,
                    User user,
                    Form form,
                    boolean done) {
                super();
                this.id = id;
                this.address = address;
                this.user = user;
                this.form = form;
                this.done = done;
        }
        
        public Address getAddress() {
                return address;
        }
        
        public Form getForm() {
                return form;
        }
        
        public Integer getId() {
                return id;
        }
        
        public User getUser() {
                return user;
        }
        
        public boolean isDone() {
                return done;
        }
        
        public void setAddress(Address address) {
                this.address = address;
        }
        
        public void setDone(boolean done) {
                this.done = done;
        }
        
        public void setForm(Form form) {
                this.form = form;
        }
        
        public void setId(Integer id) {
                this.id = id;
        }
        
        public void setUser(User user) {
                this.user = user;
        }
        
        @Override
        public String toString() {
                JSONObject data = new JSONObject();
                
                try {
                        data.put("id", id);
                        data.put("address", address);
                        data.put("user", user);
                        data.put("form", form);
                        data.put("done", done);
                }
                catch (JSONException e) {
                        e.printStackTrace();
                }
                
                return data.toString();
        }
        
}
