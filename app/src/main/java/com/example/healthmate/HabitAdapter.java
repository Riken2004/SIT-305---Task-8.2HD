package com.example.healthmate;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.VH> {
    private final List<String> data;
    public HabitAdapter(List<String> data) { this.data = data; }

    @Override public VH onCreateViewHolder(ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_habit, p, false));
    }

    @Override public void onBindViewHolder(VH h, int i) {
        h.tvName.setText(data.get(i));
        h.cbDone.setChecked(false);
        h.cbDone.setOnCheckedChangeListener((btn, ok) -> {
            h.tvName.setAlpha(ok ? 0.5f : 1f);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName; CheckBox cbDone;
        VH(android.view.View v) {
            super(v);
            tvName = v.findViewById(R.id.tvHabitName);
            cbDone = v.findViewById(R.id.cbDone);
        }
    }
}
