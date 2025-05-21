package com.example.healthmate;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class JournalAdapter
        extends RecyclerView.Adapter<JournalAdapter.VH> {


    public static class Entry {
        public long   id;
        public String html;
        public Entry(long id, String html) {
            this.id   = id;
            this.html = html;
        }
    }

    private final List<Entry> list = new ArrayList<>();
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(Entry e);
    }
    public void setOnItemClick(OnItemClickListener l) {
        clickListener = l;
    }

    public void submitList(List<Entry> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    public void addAt(int pos, Entry e) {
        list.add(pos, e);
        notifyItemInserted(pos);
    }

    public void remove(Entry e) {
        int idx = indexOf(e);
        if (idx>=0) {
            list.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    public int indexOf(Entry e) {
        for (int i=0;i<list.size();i++){
            if (list.get(i).id==e.id) return i;
        }
        return -1;
    }

    public Entry getAt(int pos) {
        return list.get(pos);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_journal_entry, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Entry e = list.get(pos);
        h.tvEntry.setText(Html.fromHtml(e.html));
        h.itemView.setOnClickListener(v -> {
            if (clickListener!=null) clickListener.onItemClick(e);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvEntry;
        VH(View iv) {
            super(iv);
            tvEntry = iv.findViewById(R.id.tvEntry);
        }
    }
}
