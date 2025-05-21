package com.example.healthmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 0, TYPE_BOT = 1;
    private final List<Message> messages = new ArrayList<>();

    public void addUserMessage(String text) {
        messages.add(new Message(text, TYPE_USER));
        notifyItemInserted(messages.size() - 1);
    }

    public void addBotMessage(String text) {
        messages.add(new Message(text, TYPE_BOT));
        notifyItemInserted(messages.size() - 1);
    }

    @Override public int getItemCount() { return messages.size(); }
    @Override public int getItemViewType(int pos) { return messages.get(pos).type; }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_USER
                ? R.layout.item_chat_user
                : R.layout.item_chat_bot;
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return viewType == TYPE_USER
                ? new UserVH(v)
                : new BotVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        ((BaseVH)h).bind(messages.get(pos).text);
    }

    static class Message {
        final String text;
        final int type;
        Message(String t, int tp) { text = t; type = tp; }
    }

    abstract static class BaseVH extends RecyclerView.ViewHolder {
        TextView tv;
        BaseVH(View itemView, int tvId) {
            super(itemView);
            tv = itemView.findViewById(tvId);
        }
        void bind(String text) { tv.setText(text); }
    }
    static class UserVH extends BaseVH {
        UserVH(View v) { super(v, R.id.tvUserMessage); }
    }
    static class BotVH  extends BaseVH {
        BotVH(View v) { super(v, R.id.tvBotMessage); }
    }
}
