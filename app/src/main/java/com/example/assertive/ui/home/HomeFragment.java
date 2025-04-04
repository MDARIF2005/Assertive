package com.example.assertive.ui.home;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assertive.DatabaseHelper;
import com.example.assertive.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private CollectionAdapter collectionAdapter;
    private ItemAdapter itemAdapter;

    private ImageButton speech, clear;
    private Button backButton;

    private LinearLayout itemsLayout;

    private EditText editText;
    private ArrayList<String> collections, items, itemValues;
    private ArrayList<Bitmap> collectionImages, itemImages;
    private TextToSpeech textToSpeech;
    private boolean showingItems = false;
    private String currentCollectionId = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.collectionRecyclerView;
        editText = binding.messageEditText;
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        dbHelper = new DatabaseHelper(requireContext());

        collections = new ArrayList<>();
        collectionImages = new ArrayList<>();
        items = new ArrayList<>();
        itemValues = new ArrayList<>();
        itemImages = new ArrayList<>();

        speech = binding.sendBtn;
        clear = binding.btnClear;
        backButton = binding.backButton;

        // Initially hide the back button
        backButton.setVisibility(View.GONE);
        itemsLayout = binding.itemsLayout;
        backButton = binding.backButton;

// Initially hide the back button
        backButton.setVisibility(View.GONE);
        itemsLayout.setVisibility(View.GONE);


        backButton.setOnClickListener(v -> {
            if (showingItems) {
                loadCollections();
                backButton.setVisibility(View.GONE); // Hide when returning to collections
            }
        });

        clear.setOnClickListener(v -> editText.setText(""));

        speech.setOnClickListener(v -> {
            String text = editText.getText().toString();
            if (!text.isEmpty() && textToSpeech != null) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        loadCollections();

        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        return root;
    }

    private void loadCollections() {
        collections.clear();
        collectionImages.clear();
        try (Cursor cursor = dbHelper.getCollections()) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    collections.add(cursor.getString(1)); // Collection Name
                    collectionImages.add(decodeImage(cursor.getBlob(2)));
                } while (cursor.moveToNext());
            } else {
                Log.e("Database", "No collections found!");
            }
        }

        collectionAdapter = new CollectionAdapter(collections, collectionImages, this::onCollectionClick);
        recyclerView.setAdapter(collectionAdapter);

        applyRecyclerViewAnimation();
        showingItems = false;

        // ✅ Hide back button and items layout when returning to collections
        itemsLayout.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
    }


    private void loadItems(String collectionId) {
        items.clear();
        itemValues.clear();
        itemImages.clear();
        itemsLayout.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);

        Log.d("DEBUG", "Fetching items for Collection ID: " + collectionId);
        try (Cursor cursor = dbHelper.getItemsByCollectionId(collectionId)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    items.add(cursor.getString(1)); // Item Name
                    itemValues.add(cursor.getString(2)); // Item Value
                    itemImages.add(decodeImage(cursor.getBlob(3)));
                    Log.d("DEBUG", "Item Loaded: " + cursor.getString(1));
                } while (cursor.moveToNext());
            } else {
                Log.e("Database", "No items found for Collection ID: " + collectionId);
            }
        }

        itemAdapter = new ItemAdapter(items, itemValues, itemImages, this::onItemClick, this::onItemLongClick);
        recyclerView.setAdapter(itemAdapter);

        applyRecyclerViewAnimation();
        showingItems = true;
        currentCollectionId = collectionId;

        // ✅ Make items layout and back button visible

    }

    private void onItemClick(String itemName) {
        int position = items.indexOf(itemName);
        if (position != -1 && position < itemValues.size()) {
            String text1 = editText.getText().toString();
            String text = itemValues.get(position);
            editText.setText(text1 + " " + text);
        }
    }

    private void onCollectionClick(String collectionName) {
        loadItems(collectionName);
    }

    private void onItemLongClick(String itemName) {
        int position = items.indexOf(itemName);
        if (position != -1 && position < itemValues.size() && textToSpeech != null) {
            String text = itemValues.get(position);
            if (!textToSpeech.isSpeaking()) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            }
        }
    }

    private Bitmap decodeImage(byte[] imageBlob) {
        if (imageBlob != null) {
            return BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
        }
        return null;
    }

    private void applyRecyclerViewAnimation() {
        recyclerView.setAlpha(0f);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.animate().alpha(1.0f).setDuration(300).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
