package com.example.assertive;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "Assistive.db";
    private DatabaseReference firebaseRef;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        firebaseRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users( user_id TEXT PRIMARY KEY, username TEXT , DOB TEXT, email TEXT, gender TEXT)");
        db.execSQL("CREATE TABLE Collection(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, Image BLOB)");
        db.execSQL("CREATE TABLE item(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT, image BLOB, folder_id text, FOREIGN KEY(folder_id) REFERENCES Collection(name))");
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS your_table_name");
        onCreate(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS Collection");
        db.execSQL("DROP TABLE IF EXISTS item");
        onCreate(db);
    }

    public boolean insertUser(String userid,    String username, String DOB, String email, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", userid);
        contentValues.put("username", username);
        contentValues.put("DOB", DOB);
        contentValues.put("email", email);
        contentValues.put("gender", gender);
        String age = getAge(DOB);

        long result = db.insert("users", null, contentValues);
        db.close();

        addCollection("User", drawableToByteArray(R.drawable.folder));

        addItem("Name", username, drawableToByteArray(R.drawable.folder),"User" );
        addItem("Date of birth", DOB, drawableToByteArray(R.drawable.folder), "User");
        addItem("Email", email, drawableToByteArray(R.drawable.folder), "User");
        addItem("Gender", gender, drawableToByteArray(R.drawable.folder), "User");
        addItem("Age", age, drawableToByteArray(R.drawable.folder), "User");

        return result != -1;
    }

    public String getAge(String dob) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); // Format for input date
        try {
            Date birthDate = sdf.parse(dob);
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(birthDate);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return String.valueOf(age);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Date";
        }
    }

    public boolean updateUser(String userid, String username, String DOB, String newEmail, String Gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("DOB", DOB);
        contentValues.put("email", newEmail);
        contentValues.put("gender", Gender);

        int result = db.update("users", contentValues, "user_id=?", new String[]{userid});
        db.close();

        if (result > 0) {
            // Update Firebase Realtime Database
            firebaseRef.child(userid).child("dob").setValue(DOB);
            firebaseRef.child(userid).child("email").setValue(newEmail);
            firebaseRef.child(userid).child("name").setValue(username);
            firebaseRef.child(userid).child("gender").setValue(Gender);

            // Update Firebase Authentication (if email changed)
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && !user.getEmail().equals(newEmail)) {
                user.updateEmail(newEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("FirebaseAuth", "Email updated successfully.");
                            } else {
                                Log.e("FirebaseAuth", "Failed to update email.", task.getException());
                            }
                        });
            }
            return true;
        }
        return false;
    }


    public boolean deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("users", null, null);
        db.close();
        deleteCollection("User");
        return result > 0;
    }
    public Cursor getUser(String userid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE user_id=?", new String[]{userid});
        return cursor;

    }

    public boolean addCollection(String name, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("Image", image != null ? image : getDefaultImage());

        long result = db.insert("Collection", null, contentValues);
        db.close();
        return result != -1;
    }


    public boolean updateCollection(int id ,String name,byte[] img){
         SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("name", name);
        contentValues.put("Image", img);
        int result = db.update("Collection", contentValues, "id=?", new String[]{String.valueOf(id)});
        db.close();
        return result !=1;

    }

    public boolean deleteCollection(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("Collection", "name=?", new String[]{name});
        db.close();
        deleteItem(name);
        return result > 0;
    }

    public Cursor getCollections() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Collection", null);
    }
    public String getUserId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM users WHERE logged_in = 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            String userId = cursor.getString(0);
            cursor.close();
            return userId;
        }
        return null;
    }

    public Cursor getCollectionId(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id FROM Collection WHERE name=?", new String[]{name});
    }

    public boolean addItem(String name, String value, byte[] image, String folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("value", value);
        contentValues.put("Image", image);
        contentValues.put("folder_id", folderId);

        long result = db.insert("item", null, contentValues);
        db.close();
        return result != -1;
    }

    public boolean deleteItem(String folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("item", "folder_id=?", new String[]{folderId});
        db.close();
        return result > 0;
    }

    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("item", "id=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public Cursor getItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM item", null);
    }

    public Cursor getItemsByCollectionId(String folderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM item WHERE folder_id=?", new String[]{folderId});
    }


    public byte[] drawableToByteArray(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            return new byte[0];
        }
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public byte[] getDefaultImage() {
        return drawableToByteArray(R.drawable.folder);
    }


    public boolean updateitem(int id,String name,String value,byte[] img,String collection){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("name", name);
        contentValues.put("value", value);
        contentValues.put("Image", img);
        contentValues.put("folder_id", collection);
        int result = db.update("item", contentValues, "id=?", new String[]{String.valueOf(id)});
        db.close();
        return result !=1;

    }




}
