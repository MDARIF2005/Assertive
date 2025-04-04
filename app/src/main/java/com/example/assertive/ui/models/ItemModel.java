package com.example.assertive.ui.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ItemModel {
    private int id;
    private String name;
    private String value;
    private String  folderId;
    private byte[] image;

    // Constructor
    public ItemModel(int id, String name, String value, byte[] image, String folderId) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.image = image;
        this.folderId = folderId;
    }
    public ItemModel(String name, String value, byte[] image, String folderId) {
        this.name = name;
        this.value = value;
        this.image = image;
        this.folderId = folderId;
    }
    public int getId() { return id; }

    // Getters
    public String getName() { return name; }
    public String getValue() { return value; }
    public byte[] getImage() { return image; }
    public String getFolderId() { return folderId; }

    // Setters
    public void setId(int id) { this.id = id; }

    // Setters (if modification is required)
    public void setName(String name) { this.name = name; }
    public void setValue(String value) { this.value = value; }
    public void setImage(byte[] image) { this.image = image; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    // Convert byte[] to Bitmap
    public Bitmap getImageBitmap() {
        if (image != null) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
        return null;
    }
}
