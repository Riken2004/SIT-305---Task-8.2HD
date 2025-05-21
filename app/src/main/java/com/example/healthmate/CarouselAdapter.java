package com.example.healthmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarouselAdapter
        extends RecyclerView.Adapter<CarouselAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(Feature feature);
    }

    private final List<Feature> items;
    private final OnItemClickListener listener;

    public CarouselAdapter(List<Feature> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feature, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Feature f = items.get(position);
        holder.ivIcon.setImageResource(f.iconRes);
        holder.tvTitle.setText(f.title);
        holder.tvSubtitle.setText(f.subtitle);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(f));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvSubtitle;

        VH(View v) {
            super(v);
            ivIcon     = v.findViewById(R.id.ivIcon);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvSubtitle = v.findViewById(R.id.tvSubtitle);
        }
    }
}
