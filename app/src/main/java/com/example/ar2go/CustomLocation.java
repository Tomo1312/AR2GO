package com.example.ar2go;

public class CustomLocation {
    String description,name;
    double latitude, longitude;
    int radius;
    public CustomLocation(){}
    public CustomLocation(String Name, String Description, double Latitude, double Longitude, int radius){
        this.name = Name;
        this.description = Description;
        this.latitude = Latitude;
        this.longitude = Longitude;
        this.radius = radius;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double Latitude) {
        this.latitude = Latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double Longitude) {
        this.longitude = Longitude;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int Radius) {
        this.radius = Radius;
    }
}
