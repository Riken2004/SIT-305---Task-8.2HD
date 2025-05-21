package com.example.healthmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuotePagerAdapter
        extends RecyclerView.Adapter<QuotePagerAdapter.VH> {

    public interface FavoriteCallback {
        void onFavoriteToggled(int position);
    }

    private final List<QuoteActivity.QuoteEntity> data;
    private final FavoriteCallback               favCb;

    public QuotePagerAdapter(
            @NonNull List<QuoteActivity.QuoteEntity> items,
            @NonNull FavoriteCallback callback
    ) {
        this.data  = items;
        this.favCb = callback;
    }

    @Override public int getItemCount() { return data.size(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_quote, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        QuoteActivity.QuoteEntity e = data.get(position);
        holder.tvContent.setText(e.content);
        holder.tvAuthor .setText("– " + e.author);
        holder.btnFav.setImageResource(
                e.isFavorite
                        ? R.drawable.ic_favorite_filled
                        : R.drawable.ic_favorite_border
        );
        holder.btnFav.setOnClickListener(v ->
                favCb.onFavoriteToggled(position)
        );
    }


    public void updateList(@NonNull List<QuoteActivity.QuoteEntity> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView    tvContent, tvAuthor;
        final ImageButton btnFav;
        VH(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvAuthor  = itemView.findViewById(R.id.tvAuthor);
            btnFav    = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
