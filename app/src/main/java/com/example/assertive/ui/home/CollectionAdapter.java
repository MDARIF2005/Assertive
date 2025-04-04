package com.example.assertive.ui.home;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assertive.R;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<String> collectionList;
    private List<Bitmap> collectionImages;
    private OnCollectionClickListener listener;

    public interface OnCollectionClickListener {
        void onCollectionClick(String collectionName);
    }

    public CollectionAdapter(List<String> collectionList, List<Bitmap> collectionImages, OnCollectionClickListener listener) {
        this.collectionList = collectionList;
        this.collectionImages = collectionImages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String collectionName = collectionList.get(position);
        Bitmap imageBitmap = collectionImages.get(position);

        holder.collectionName.setText(collectionName);
        if (imageBitmap != null) {
            holder.collectionImage.setImageBitmap(imageBitmap);
        } else {
            holder.collectionImage.setImageResource(R.drawable.folder);
        }

        holder.itemView.setOnClickListener(v -> listener.onCollectionClick(collectionName));
    }

    @Override
    public int getItemCount() {
        return collectionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView collectionName;
        ImageView collectionImage;

        public ViewHolder(View itemView) {
            super(itemView);
            collectionName = itemView.findViewById(R.id.collection_name);
            collectionImage = itemView.findViewById(R.id.collection_image);
        }
    }
}
