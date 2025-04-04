package com.example.assertive.ui.collection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assertive.R;
import com.example.assertive.ui.models.ItemModel;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final Context context;
    private final List<ItemModel> itemList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onUpdateItem(ItemModel item);
        void onDeleteItem(ItemModel item);
    }

    public ItemAdapter(Context context, List<ItemModel> itemList, OnItemClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.manage_items, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        final ItemModel item = itemList.get(position);

        holder.itemName.setText(item.getName());
        String valueText = (item.getValue() != null) ? "Value: " + item.getValue() : "Value: N/A";
        holder.itemValue.setText(valueText);

        // Convert byte array to Bitmap (optimized)
        byte[] imageBytes = item.getImage();
        if (imageBytes != null && imageBytes.length > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Optimize memory usage
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
            holder.itemImage.setImageBitmap(bitmap);
        } else {
            holder.itemImage.setImageResource(R.drawable.folder); // Default Image
        }

        // Ensure listener is not null before calling methods
        if (listener != null) {
            holder.btnUpdate.setOnClickListener(v -> listener.onUpdateItem(item));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteItem(item));
        }

        // Long press listener for future features
        holder.itemView.setOnLongClickListener(v -> {
            Toast.makeText(context, "Long press detected on: " + item.getName(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemValue;
        ImageView itemImage;
        Button btnUpdate, btnDelete;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemValue = itemView.findViewById(R.id.item_value);
            itemImage = itemView.findViewById(R.id.item_image);
            btnUpdate = itemView.findViewById(R.id.btn_update_item);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
        }
    }
}
