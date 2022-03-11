package com.ipy849.fakebook;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Post {

    public static String[] images = new String[]{
            "https://images.pexels.com/photos/5012112/pexels-photo-5012112.jpeg?auto=compress&cs=tinysrgb&dpr=2&w=500",
            "https://images.pexels.com/photos/8311272/pexels-photo-8311272.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260",
            "https://images.pexels.com/photos/5837013/pexels-photo-5837013.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260"
    };

    public static String[] usernmaes = new String[]{
            "maybemarhs",
            "gbn99",
            "Millie ⋆ Lia",
            "Christian Interian Felipes",
            "Edgar Gutierrez Jr.",
    };

    public static String[] captions = new String[]{
            "Por muy corto que sea el camino. Quien pisa fuerte, ¡deja huella!",
            "A quien debes retar, impresionar y superar es a ti misma.",
            "Los obstáculos en la vida nos hacen madurar, los éxitos nos hacen reflexionar y los fracasos nos hacen crecer.",
            "Tu cuerpo escucha todo lo que dice tu mente. Sé positiva.",
            "No te estoy diciendo que será fácil, te estoy diciendo que valdrá la pena."
    };

    private String user;
    private String content;
    private String caption;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Post() {
        this.user = user;
        this.content = content;
        this.caption = caption;
    }

    public void print(){
        Log.d("PRUEBA", this.user + " " + this.caption + " " + this.content);
    }

    public static List<Post> GenerateContent(int qty){
        ArrayList<Post> posts = new ArrayList<Post>();

        for (int i = 0; i < qty; i++) {
            Random random = new Random();
            Post post = new Post();
            int index = random.nextInt(Post.captions.length);
            if(index < Post.images.length) post.setContent(Post.images[index]);
            post.setUser(Post.usernmaes[index]);
            post.setCaption(Post.captions[index]);
            posts.add(post);
        }

        return posts;
    }
}
