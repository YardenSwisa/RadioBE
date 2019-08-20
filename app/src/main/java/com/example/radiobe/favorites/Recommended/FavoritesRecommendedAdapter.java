package com.example.radiobe.favorites.Recommended;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.radiobe.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FavoritesRecommendedAdapter extends RecyclerView.Adapter<FavoritesRecommendedAdapter.FavoritesViewHolder> {
    /*Properties*/
    private List<FavoriteRecommendedItem> favoriteItemList;
    private Context context;

    /*Constructor*/
    public FavoritesRecommendedAdapter(List<FavoriteRecommendedItem> favoriteItemList, Context context) {
        this.favoriteItemList = favoriteItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_favorite_recommend, parent, false);

        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        FavoriteRecommendedItem favoriteRecommendedItem = favoriteItemList.get(position);

        holder.ivFavoriteItemImage.setImageResource(favoriteRecommendedItem.getImageURL());
        holder.tvFavoriteTitle.setText(favoriteRecommendedItem.getTitle());
        holder.tvFavoriteDescription.setText(favoriteRecommendedItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return favoriteItemList.size();
    }

    class FavoritesViewHolder extends RecyclerView.ViewHolder {
        /*Properties*/
        ImageView ivFavoriteItemImage;
        TextView tvFavoriteTitle;
        TextView tvFavoriteDescription;


        /*Constructor*/
        private FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFavoriteItemImage = itemView.findViewById(R.id.civFavoriteItemImage);
            tvFavoriteTitle = itemView.findViewById(R.id.tvItemFavoriteTitle);
            tvFavoriteDescription = itemView.findViewById(R.id.tvItemFavoriteDescription);
        }





    }

}
