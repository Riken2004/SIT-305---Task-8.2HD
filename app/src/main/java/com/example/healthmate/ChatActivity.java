package com.example.healthmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class ChatActivity extends AppCompatActivity {

    private LinearLayout chatLayout;
    private EditText    messageInput;
    private Button      sendButton;
    private ScrollView  scrollView;


    private final String API_URL     = "https://api.together.xyz/inference";
    private final String AUTH_TOKEN  = "Bearer 9311ec4dca55cb7d7ffdf9bcaec2025516b8bec9ab52dd3707f3bc6b2787085e";


    private final int MAX_HISTORY_LENGTH = 1200;


    private final StringBuilder chatHistory = new StringBuilder(
            "You are RikenBot, a knowledgeable, detailed, and friendly AI assistant. " +
                    "Provide thorough and clear answers in complete sentences. " +
                    "If needed, ask clarifying questions before answering.\n\n"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatLayout    = findViewById(R.id.messagesContainer);
        scrollView    = findViewById(R.id.chatScrollView);
        messageInput  = findViewById(R.id.messageInput);
        sendButton    = findViewById(R.id.sendButton);

        SharedPreferences prefs = getSharedPreferences("chatAppPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "User");

        // initial bot greeting
        addMessage("Bot: Hi! I'm HealthMate 🤖 How can I help you today?", false);

        sendButton.setOnClickListener(v -> {
            String userMsg = messageInput.getText().toString().trim();
            if (userMsg.isEmpty()) return;
            addMessage(username + ": " + userMsg, true);
            messageInput.setText("");
            getBotResponse(userMsg);
        });
    }

    private void addMessage(String message, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextSize(16f);
        tv.setTextColor(getResources().getColor(android.R.color.black));
        tv.setPadding(24, 16, 24, 16);
        tv.setMaxWidth( (int)(getResources().getDisplayMetrics().widthPixels * 0.75) );


        tv.setBackgroundResource(
                isUser ? R.drawable.user_bubble
                        : R.drawable.bot_bubble
        );


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(8,8,8,8);
        lp.gravity = isUser
                ? Gravity.END   // user on right
                : Gravity.START; // bot on left
        tv.setLayoutParams(lp);

        chatLayout.addView(tv);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void getBotResponse(String userInput) {
        OkHttpClient client = new OkHttpClient();


        if (chatHistory.length() > MAX_HISTORY_LENGTH) {
            chatHistory.delete(0, chatHistory.length() - MAX_HISTORY_LENGTH);
        }


        String prompt = chatHistory
                .append("User: ").append(userInput).append("\n")
                .append("Assistant: ")
                .toString();

        // show “typing…”
        runOnUiThread(() -> addMessage("Bot: …", false));

        JSONObject payload = new JSONObject();
        try {
            payload.put("model", "meta-llama/Llama-3.3-70B-Instruct-Turbo-Free");
            payload.put("prompt", prompt);
            payload.put("max_tokens", 170);
            payload.put("temperature", 0.7);
            payload.put("top_p", 0.9);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
        );

        Request req = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", AUTH_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    removeLastMessage();
                    addMessage("Bot: Connection failed 😢", false);
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body().string();
                Log.i("TOGETHER-RESPONSE", resBody);

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        removeLastMessage();
                        addMessage("Bot: Error from server ❌", false);
                    });
                    return;
                }

                String botReply = "";
                try {
                    JSONObject json = new JSONObject(resBody);
                    JSONObject out  = json.getJSONObject("output");
                    botReply = out
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getString("text")
                            .trim();


                    chatHistory.append(botReply).append("\n");

                } catch (Exception e) {
                    e.printStackTrace();
                    botReply = "Sorry, I couldn't parse that.";
                }

                final String reply = botReply;
                runOnUiThread(() -> {
                    removeLastMessage();
                    addMessage("Bot: " + reply, false);
                });
            }
        });
    }

    private void removeLastMessage() {
        int count = chatLayout.getChildCount();
        if (count > 0) {
            chatLayout.removeViewAt(count - 1);
        }
    }
}
