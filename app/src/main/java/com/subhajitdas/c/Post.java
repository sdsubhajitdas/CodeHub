package com.subhajitdas.c;

/**
 * Created by Subhajit Das on 10-01-2017.
 */

public class Post {

    private String title;
    private String userName;
    private String date;
    private String likes;
    private String bookmark;


    public Post() {

    }

    public Post(String title, String userId) {
        this.title = title;
        this.userName = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getBookmark(){
        return bookmark;
    }

    public void setBookmark(String bookmark){
        this.bookmark=bookmark;
    }
}
