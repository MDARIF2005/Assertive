package com.example.assertive.ui.home;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assertive.R;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<String> itemList;
    private List<String> itemValue;
    private List<Bitmap> itemImages;
    private WeakReference<OnItemClickListener> itemClickListener;
    private WeakReference<OnItemLongClickListener> itemLongClickListener;

    // Interfaces for Click Events
    public interface OnItemClickListener {
        void onItemClick(String itemName);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(String itemName);
    }

    // Constructor
    public ItemAdapter(List<String> itemList, List<String> itemValue, List<Bitmap> itemImages,
                       OnItemClickListener itemClickListener, OnItemLongClickListener itemLongClickListener) {
        this.itemList = itemList;
        this.itemValue = itemValue;
        this.itemImages = itemImages;
        this.itemClickListener = new WeakReference<>(itemClickListener);
        this.itemLongClickListener = new WeakReference<>(itemLongClickListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Ensure lists have valid indices before accessing data
        if (position >= itemList.size() || position >= itemValue.size()) {
            return;
        }

        String itemName = itemList.get(position);
        holder.itemName.setText(itemName);

        // Load Image Efficiently Using Glide
        if (position < itemImages.size() && itemImages.get(position) != null) {
            Glide.with(holder.itemView.getContext())
                    .load(itemImages.get(position))
                    .placeholder(R.drawable.folder) // Placeholder image
                    .into(holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.folder);
        }

        // Click Listeners
        holder.itemView.setOnClickListener(v -> {
            OnItemClickListener listener = itemClickListener.get();
            if (listener != null) listener.onItemClick(itemName);
        });

        holder.itemView.setOnLongClickListener(v -> {
            OnItemLongClickListener listener = itemLongClickListener.get();
            if (listener != null) listener.onItemLongClick(itemName);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder Class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView itemName;
        final ImageView itemImage;

        public ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemImage = itemView.findViewById(R.id.item_image);
        }
    }
}
