package com.example.logindemo;

public class UserProfile {
    public String userName;
    public int userBodovi, userLifes;
    public String userEmail;
    public String otkljucaneSkulputre;

    public UserProfile(){

    }

    public UserProfile(String userName, String userEmail, int userBodovi, String otkljucaneSkulputre, int UserLifes){
        this.userName = userName;
        this.userEmail = userEmail;
        this.userBodovi = userBodovi;
        this.otkljucaneSkulputre = otkljucaneSkulputre;
        this.userLifes = UserLifes;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserBodovi() {
        return userBodovi;
    }

    public void setUserBodovi(int userBodovi) {
        this.userBodovi = userBodovi;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getOtkljucaneSkulputre() {
        return otkljucaneSkulputre;
    }

    public void setOtkljucaneSkulputre(String otkljucaneSkulputre) {
        this.otkljucaneSkulputre = otkljucaneSkulputre;
    }

    public int getUserLifes() {
        return userLifes;
    }

    public void setUserLifes(int lifes) {
        this.userLifes = lifes;
    }

}
