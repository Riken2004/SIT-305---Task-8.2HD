package com.example.healthmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.VH> {

    public static class MoodEntry {
        public final String date;
        public final String mood;
        public MoodEntry(String date, String mood) {
            this.date = date;
            this.mood = mood;
        }
    }

    private final List<MoodEntry> entries = new ArrayList<>();


    public void setEntries(List<MoodEntry> list) {
        entries.clear();
        entries.addAll(list);
        notifyDataSetChanged();
    }


    public List<String> getDates() {
        List<String> dates = new ArrayList<>();
        for (MoodEntry e : entries) {
            dates.add(e.date);
        }
        return dates;
    }


    public void updateMood(String date, String emoji) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).date.equals(date)) {
                entries.set(i, new MoodEntry(date, emoji));
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        MoodEntry e = entries.get(position);
        holder.tvDate.setText(e.date.substring(5));
        holder.tvMood.setText(e.mood);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvDate, tvMood;
        VH(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvHistDate);
            tvMood = itemView.findViewById(R.id.tvHistMood);
        }
    }
}
