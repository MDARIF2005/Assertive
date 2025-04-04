package com.example.assertive.ui.collection;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assertive.DatabaseHelper;
import com.example.assertive.R;
import com.example.assertive.ui.models.CollectionModel;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private Context context;
    private List<CollectionModel> collectionList;
    private DatabaseHelper dbHelper;
    private OnCollectionClickListener listener;

    // ✅ Updated constructor to use OnCollectionClickListener instead of Manage_collection
    public CollectionAdapter(Context context, List<CollectionModel> collectionList, OnCollectionClickListener listener) {
        this.context = context;
        this.collectionList = collectionList;
        this.dbHelper = new DatabaseHelper(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.manage_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionModel collection = collectionList.get(position);

        holder.tvName.setText(collection.getName());

        if (collection.getImage() != null && collection.getImage().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(collection.getImage(), 0, collection.getImage().length);
            holder.ivImage.setImageBitmap(bitmap);
        } else {
            holder.ivImage.setImageResource(R.drawable.folder); // Default image
        }

        // ✅ Pass collection to the listener instead of calling fragment methods directly
        holder.btnView.setOnClickListener(v -> listener.onViewItems(collection));
        holder.btnUpdate.setOnClickListener(v -> listener.onUpdateCollection(collection));
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(collection));
    }

    private void showDeleteConfirmation(CollectionModel collection) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Collection")
                .setMessage("Are you sure you want to delete \"" + collection.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteCollection(collection.getName());
                    listener.onDeleteCollection(collection); // Notify listener
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return collectionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        Button btnView, btnUpdate, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.collection_image);
            tvName = itemView.findViewById(R.id.collection_name);
            btnView = itemView.findViewById(R.id.btn_view_items);
            btnUpdate = itemView.findViewById(R.id.btn_update_collection);
            btnDelete = itemView.findViewById(R.id.btn_delete_collection);
        }
    }

    // ✅ Interface for handling collection actions
    public interface OnCollectionClickListener {
        void onViewItems(CollectionModel collection);
        void onUpdateCollection(CollectionModel collection);
        void onDeleteCollection(CollectionModel collection);
    }
}
