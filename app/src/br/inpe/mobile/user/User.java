package br.inpe.mobile.user;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class User implements Serializable {
    @DatabaseField(
                   generatedId = true,
                   columnName = "id",
                   allowGeneratedIdInsert = true)
    private Integer id;
    
    @DatabaseField
    private String  name;
    
    @DatabaseField
    private String  login;
    
    @DatabaseField
    private String  password;
    
    @DatabaseField
    private String  hash;
    
    public User() {
        // TODO Auto-generated constructor stub
    }
    
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
    
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
}
