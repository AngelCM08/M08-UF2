package com.example.proyecto_m8_uf2;

import java.util.HashMap;
import java.util.Map;

public class Post {
    public String uid;
    public String author;
    public String authorPhotoUrl;
    public String content;
    public Map<String, Boolean> likes = new HashMap<>();
    public com.google.firebase.Timestamp date;

    // Constructor vacio requerido por Firestore
    public Post() {}

    public Post(String uid, String author, String authorPhotoUrl, String content, com.google.firebase.Timestamp date) {
        this.uid = uid;
        this.author = author;
        this.authorPhotoUrl = authorPhotoUrl;
        this.content = content;
        this.date = date;
    }
}
