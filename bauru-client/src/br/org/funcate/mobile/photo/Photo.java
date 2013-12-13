package br.org.funcate.mobile.photo;

import java.io.Serializable;

import br.org.funcate.mobile.form.Form;

import com.j256.ormlite.field.DatabaseField;

public class Photo implements Serializable {
    @DatabaseField(generatedId = true, columnName = "id")
    private Integer id;
    @DatabaseField
    private String  blob; // BASE64
    @DatabaseField
    private String  path;
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private Form    form;

    public Photo() {
    }

    public Photo(Integer id, String blob, String path, Form form) {
        super();
        this.id = id;
        this.blob = blob;
        this.path = path;
        this.form = form;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

}
