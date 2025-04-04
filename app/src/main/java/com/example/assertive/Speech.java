package com.example.assertive;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import com.example.assertive.*;

import java.util.Locale;

public class Speech {

    private TextToSpeech textToSpeech;
    DatabaseHelper databaseHelper;

    public Speech(Context context, String text) {
        databaseHelper = new DatabaseHelper(context);


        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

}
