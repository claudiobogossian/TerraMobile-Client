package br.inova.mobile.user;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class User implements Serializable {
        @DatabaseField
        private String  hash;
        
        @DatabaseField(
                       generatedId = true,
                       columnName = "id",
                       allowGeneratedIdInsert = true)
        private Integer id;
        
        @DatabaseField
        private String  login;
        
        @DatabaseField
        private String  name;
        
        @DatabaseField
        private String  password;
        
        public User() {}
        
        public User(
                    Integer id,
                    String name,
                    String login,
                    String password,
                    String hash) {
                super();
                this.id = id;
                this.name = name;
                this.login = login;
                this.password = password;
                this.hash = hash;
        }
        
        public String getHash() {
                return hash;
        }
        
        public Integer getId() {
                return id;
        }
        
        public String getLogin() {
                return login;
        }
        
        public String getName() {
                return name;
        }
        
        public String getPassword() {
                return password;
        }
        
        public void setHash(String hash) {
                this.hash = hash;
        }
        
        public void setId(Integer id) {
                this.id = id;
        }
        
        public void setLogin(String login) {
                this.login = login;
        }
        
        public void setName(String name) {
                this.name = name;
        }
        
        public void setPassword(String password) {
                this.password = password;
        }
        
        @Override
        public String toString() {
                JSONObject data = new JSONObject();
                
                try {
                        data.put("id", id);
                        data.put("name", name);
                        data.put("login", login);
                        data.put("password", password);
                        data.put("hash", hash);
                }
                catch (JSONException e) {
                        e.printStackTrace();
                }
                
                return data.toString();
        }
        
}
