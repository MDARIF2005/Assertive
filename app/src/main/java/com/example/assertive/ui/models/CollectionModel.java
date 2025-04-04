package com.example.assertive.ui.models;

public class CollectionModel {
    private int  id;
    private String name;
    private byte[] image;

    public CollectionModel(int id, String name ,byte[] image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public String getName() { return name; }
    public byte[] getImage() { return image; }
    public int getId() { return id; }
}
