package com.example.assertive.ui.collection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assertive.DatabaseHelper;
import com.example.assertive.ImageUtils;
import com.example.assertive.R;
import com.example.assertive.ui.models.CollectionModel;
import com.example.assertive.ui.models.ItemModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manage_collection extends Fragment {

    private RecyclerView collectionRecyclerView, itemRecyclerView;
    private CollectionAdapter collectionAdapter;
    private ItemAdapter itemAdapter;
    private CardView cardCollection, cardItems;
    private DatabaseHelper dbHelper;
    private List<CollectionModel> collectionList;
    private List<ItemModel> itemList;
    private Button btnAddCollection, btnAddItem, btnBack;
    private static final int IMAGE_PICK_CODE = 1000;
    private ImageSelectCallback imageSelectCallback;
    private String currentCollectionName;

    // Callback interface for image selection
    private interface ImageSelectCallback {
        void onImageSelected(Bitmap bitmap, byte[] imageBytes);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_managecollection, container, false);

        dbHelper = new DatabaseHelper(getContext());

        collectionRecyclerView = view.findViewById(R.id.rv_collections);
        collectionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        itemRecyclerView = view.findViewById(R.id.rv_items);
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAddCollection = view.findViewById(R.id.btn_add_collection);
        btnAddItem = view.findViewById(R.id.btn_add_item);
        btnBack = view.findViewById(R.id.btn_back);

        btnBack.setVisibility(View.GONE);
        btnAddItem.setVisibility(View.GONE);

        btnAddCollection.setOnClickListener(v -> showAddCollectionDialog());
        btnAddItem.setOnClickListener(v -> showAddItemDialog());
        btnBack.setOnClickListener(v -> showCollections());
        cardCollection = view.findViewById(R.id.card_collection);
        cardItems = view.findViewById(R.id.card_items);

        loadCollections();
        return view;
    }

    public void loadCollections() {
        collectionList = new ArrayList<>();
        Cursor cursor = dbHelper.getCollections();
        cardItems.setVisibility(View.GONE);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);

                String name = cursor.getString(1);
                byte[] image = cursor.getBlob(2);
                collectionList.add(new CollectionModel(id, name, image));
            } while (cursor.moveToNext());
            cursor.close();
        }

        collectionAdapter = new CollectionAdapter(getContext(), collectionList, new CollectionAdapter.OnCollectionClickListener() {
            @Override
            public void onViewItems(CollectionModel collection) {
                showItems(collection.getName());
            }

            @Override
            public void onUpdateCollection(CollectionModel collection) {
                showUpdateCollectionDialog(collection);
            }

            @Override
            public void onDeleteCollection(CollectionModel collection) {
                confirmDeleteCollection(collection);
            }
        });

        collectionRecyclerView.setAdapter(collectionAdapter);
    }

    public void showItems(String collectionName) {
        itemList = new ArrayList<>();
        Cursor cursor = dbHelper.getItemsByCollectionId(collectionName);
        currentCollectionName = collectionName;
        cardItems.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String value = cursor.getString(2);
                byte[] image = cursor.getBlob(3);
                itemList.add(new ItemModel(id, name, value, image, collectionName));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (itemList.isEmpty()) {
            Toast.makeText(getContext(), "No items in this collection", Toast.LENGTH_SHORT).show();
        }

        cardCollection.setVisibility(View.GONE);
        collectionRecyclerView.setVisibility(View.GONE);
        itemRecyclerView.setVisibility(View.VISIBLE);
        btnAddCollection.setVisibility(View.GONE);
        btnAddItem.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);

        itemAdapter = new ItemAdapter(getContext(), itemList, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onUpdateItem(ItemModel item) {
                showUpdateItemDialog(item);
            }

            @Override
            public void onDeleteItem(ItemModel item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Item");
                builder.setMessage("Are you sure you want to delete " + item.getName() + "?");

                builder.setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteItem(item.getId());
                    showItems(currentCollectionName);
                    Toast.makeText(getContext(), "Deleted " + item.getName(), Toast.LENGTH_SHORT).show();
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                builder.show();
            }

        });
        itemRecyclerView.setAdapter(itemAdapter);
    }

    public void showCollections() {
        collectionRecyclerView.setVisibility(View.VISIBLE);
        itemRecyclerView.setVisibility(View.GONE);
        btnAddCollection.setVisibility(View.VISIBLE);
        btnAddItem.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        cardCollection.setVisibility(View.VISIBLE);
        cardItems.setVisibility(View.GONE);
    }

    private void showAddCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_collection, null);
        builder.setView(dialogView);

        EditText etCollectionName = dialogView.findViewById(R.id.etCollectionName);
        ImageView ivCollectionImage = dialogView.findViewById(R.id.ivCollectionImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCollection);
        Button cancle = dialogView.findViewById(R.id.add_collection_cancle);

        final byte[][] imageBytesHolder = new byte[1][];

        btnSelectImage.setOnClickListener(v -> {
            imageSelectCallback = (bitmap, imageBytes) -> {
                ivCollectionImage.setImageBitmap(bitmap);
                imageBytesHolder[0] = imageBytes;
            };
            selectImage();
        });

        AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> {
            String name = etCollectionName.getText().toString().trim();

            if (!name.isEmpty() ) {
                dbHelper.addCollection(name, imageBytesHolder[0]);
                loadCollections();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a name ", Toast.LENGTH_SHORT).show();
            }
        });
        cancle.setOnClickListener(v -> dialog.dismiss());


        dialog.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                byte[] bytes = getBytesFromBitmap(bitmap);
                if (imageSelectCallback != null) {
                    imageSelectCallback.onImageSelected(bitmap, bytes);
                    imageSelectCallback = null; // 🔥 Important: clear it after use to avoid conflicts
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    private void confirmDeleteCollection(CollectionModel collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Collection")
                .setMessage("Are you sure you want to delete " + collection.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteCollection(collection.getName());
                    loadCollections();
                    Toast.makeText(getContext(), "Collection deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText etItemName = dialogView.findViewById(R.id.etItemName);
        ImageView ivItemImage = dialogView.findViewById(R.id.ivItemImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.add_item_cancle);

        final byte[][] selectedImageBytes = {null}; // Store selected image bytes

        btnSelectImage.setOnClickListener(v -> {
            imageSelectCallback = (bitmap, imageBytes) -> {
                ivItemImage.setImageBitmap(bitmap);
                selectedImageBytes[0] = imageBytes; // Store image bytes
            };
            selectImage();
        });

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();

            if (!name.isEmpty() ) {

                dbHelper.addItem(name, name, selectedImageBytes[0], currentCollectionName);
                showItems(currentCollectionName);
                dialog.dismiss();
                Toast.makeText(getContext(), "Item added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please fill the name ", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog on cancel

        dialog.show();
    }
    private void showUpdateItemDialog(ItemModel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_item, null);
        builder.setView(dialogView);

        EditText etItemName = dialogView.findViewById(R.id.etItemName);
         // Added field for item value
        ImageView ivItemImage = dialogView.findViewById(R.id.ivItemImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdateItem);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Pre-fill fields with item data
        etItemName.setText(item.getName());
         // Set existing value if available

        if (item.getImage() != null) {
            Bitmap bitmap = ImageUtils.getDownsampledBitmap(item.getImage(), 200, 200); // adjust size as needed
            ivItemImage.setImageBitmap(bitmap);
        } else {
            ivItemImage.setImageResource(R.drawable.folder); // Optional fallback image
        }

        final byte[][] updatedImageBytes = {item.getImage()}; // Store current image bytes

        // Handle image selection
        btnSelectImage.setOnClickListener(v -> {
            imageSelectCallback = (selectedBitmap, imageBytes) -> {
                ivItemImage.setImageBitmap(selectedBitmap);
                updatedImageBytes[0] = imageBytes; // Update image bytes
            };
            selectImage();
        });

        AlertDialog dialog = builder.create();

        btnUpdate.setOnClickListener(v -> {
            String updatedName = etItemName.getText().toString().trim();
             // Get updated value

            if (!updatedName.isEmpty() ) {
                dbHelper.updateitem(item.getId(), updatedName, updatedName, updatedImageBytes[0], currentCollectionName);
                showItems(currentCollectionName);
                dialog.dismiss();
                Toast.makeText(getContext(), "Item updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter all details!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog on cancel

        dialog.show();
    }

    private void showUpdateCollectionDialog(CollectionModel collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_collection, null);
        builder.setView(dialogView);

        EditText etCollectionName = dialogView.findViewById(R.id.etCollectionName);
        ImageView ivCollectionImage = dialogView.findViewById(R.id.ivCollectionImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnUpdate = dialogView.findViewById(R.id.btnSaveCollection); // Rename to btnUpdateCollection
        Button btnCancel = dialogView.findViewById(R.id.add_collection_cancle); // Cancel button

        // Pre-fill collection details
        etCollectionName.setText(collection.getName());
        Bitmap bitmap = ImageUtils.getDownsampledBitmap(collection.getImage(), 200, 200); // adjust size as needed
        ivCollectionImage.setImageBitmap(bitmap);

        final byte[][] updatedImageBytes = {collection.getImage()}; // Store the current image bytes

        // Handle image selection
        btnSelectImage.setOnClickListener(v -> {
            imageSelectCallback = (selectedBitmap, imageBytes) -> {
                ivCollectionImage.setImageBitmap(selectedBitmap);
                updatedImageBytes[0] = imageBytes; // Update the image bytes
            };
            selectImage();
        });

        AlertDialog dialog = builder.create();

        btnUpdate.setText("Update"); // Change button text to "Update"
        btnUpdate.setOnClickListener(v -> {
            String newName = etCollectionName.getText().toString().trim();

            if (!newName.isEmpty()) {
                dbHelper.updateCollection(collection.getId(), newName, updatedImageBytes[0]);
                loadCollections();
                dialog.dismiss();
                Toast.makeText(getContext(), "Collection Updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter a collection name!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss()); // Cancel button to close the dialog

        dialog.show();
    }



}
