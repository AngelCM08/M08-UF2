package com.example.proyecto_m8_uf2;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Post {
    public String uid;
    public String postId;
    public String author;
    public String authorPhotoUrl;
    public String content;
    public String mediaUrl;
    public String mediaType;
    public Map<String, Boolean> retweet = new HashMap<>();
    public Map<String, Boolean> likes = new HashMap<>();
    public com.google.firebase.Timestamp date;

    // Constructor vacio requerido por Firestore
    public Post() {}

    public Post(String uid, String author, String authorPhotoUrl, String content, String mediaUrl, String mediaType, Timestamp date) {
        this.uid = uid;
        this.author = author;
        this.authorPhotoUrl = authorPhotoUrl;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.date = date;
    }
}