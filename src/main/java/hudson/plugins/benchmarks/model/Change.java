package hudson.plugins.benchmarks.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Change implements Serializable {

    private String id;
    private Date date;

    private String author;
    private String email;
    private String message;

    public Change( String id, Date date, String author, String email, String message ) {
        this.id = id;
        this.date = date;
        this.author = author;
        this.email = email;
        this.message = message;
    }

    public String getId(){
        return id;
    }

    public Date getDate(){
        return date;
    }

    public String getAuthor(){
        return author;
    }

    public String getEmail(){
        return email;
    }

    public String getMessage(){
        return message;
    }

    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> o = new HashMap<String, Object>();

        o.put("id", id);
        o.put("date", date );
        o.put("author", author);
        o.put("email", email);
        o.put("message", message );

        return o;
    }

    public String toString() {
        return id;
    }
}
