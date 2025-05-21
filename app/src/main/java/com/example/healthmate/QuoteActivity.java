package com.example.healthmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class QuoteActivity extends AppCompatActivity {
    private static final String PREFS       = "quote_prefs";
    private static final String KEY_HISTORY = "quote_history";

    private SharedPreferences prefs;
    private List<QuoteEntity> history;
    private QuotePagerAdapter adapter;
    private ViewPager2 viewPager;
    private QuoteService svc;


    interface QuoteService {
        @GET("random")
        Call<List<ZenQuote>> getRandom();
    }


    public static class ZenQuote {
        public String q;  // quote text
        public String a;  // author
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());


        prefs   = getSharedPreferences(PREFS, MODE_PRIVATE);
        history = loadHistory();


        svc = new Retrofit.Builder()
                .baseUrl("https://zenquotes.io/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QuoteService.class);


        viewPager = findViewById(R.id.viewPager);
        adapter = new QuotePagerAdapter(
                history,
                pos -> {

                    QuoteEntity e = history.get(pos);
                    e.isFavorite = !e.isFavorite;
                    saveHistory();
                    adapter.notifyItemChanged(pos);
                }
        );
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer((page, pos) ->
                page.setAlpha(1f - Math.abs(pos))
        );


        FloatingActionButton fabNew = findViewById(R.id.fabNew);
        fabNew.setOnClickListener(v -> fetchAndShowDialog());


        FloatingActionButton fabHistory = findViewById(R.id.fabHistory);
        fabHistory.setOnClickListener(v -> showHistoryDialog());


        if (history.isEmpty()) {
            fetchAndShowDialog();
        } else {
            viewPager.setCurrentItem(0, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_view_fav) {
            showFavoritesDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void fetchAndShowDialog() {
        svc.getRandom().enqueue(new Callback<List<ZenQuote>>() {
            @Override
            public void onResponse(Call<List<ZenQuote>> call, Response<List<ZenQuote>> resp) {
                if (resp.isSuccessful()
                        && resp.body()!=null
                        && !resp.body().isEmpty()
                ) {
                    ZenQuote z = resp.body().get(0);
                    new AlertDialog.Builder(QuoteActivity.this)
                            .setTitle("New Quote")
                            .setMessage("“" + z.q + "”\n\n– " + z.a)
                            .setPositiveButton("Add to History", (d,w) -> {
                                QuoteEntity e = new QuoteEntity(z.q, z.a, false);
                                history.add(0, e);
                                saveHistory();
                                adapter.notifyItemInserted(0);
                                viewPager.setCurrentItem(0, true);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    Toast.makeText(QuoteActivity.this,
                            "API error: " + resp.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ZenQuote>> call, Throwable t) {
                Toast.makeText(QuoteActivity.this,
                        "Network fail: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void showHistoryDialog() {
        if (history.isEmpty()) {
            Toast.makeText(this, "No saved quotes yet!", Toast.LENGTH_SHORT).show();
            return;
        }
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_history, null);
        RecyclerView rv = dialogView.findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new HistoryListAdapter(history));

        new AlertDialog.Builder(this)
                .setTitle("Quote History")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }


    private void showFavoritesDialog() {
        List<QuoteEntity> favs = new ArrayList<>();
        for (QuoteEntity e : history) if (e.isFavorite) favs.add(e);
        if (favs.isEmpty()) {
            Toast.makeText(this, "No favorites yet!", Toast.LENGTH_SHORT).show();
            return;
        }
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_favorites, null);
        RecyclerView rv = dialogView.findViewById(R.id.rvFavorites);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new FavoriteListAdapter(favs));

        new AlertDialog.Builder(this)
                .setTitle("Your Favorites")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }


    private List<QuoteEntity> loadHistory() {
        String json = prefs.getString(KEY_HISTORY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<QuoteEntity>>(){}.getType();
        return new Gson().fromJson(json, type);
    }


    private void saveHistory() {
        String json = new Gson().toJson(history);
        prefs.edit().putString(KEY_HISTORY, json).apply();
    }


    public static class QuoteEntity {
        public String content, author;
        public boolean isFavorite;
        public QuoteEntity(String c, String a, boolean f) {
            content = c; author = a; isFavorite = f;
        }
    }


    private static class HistoryListAdapter
            extends RecyclerView.Adapter<HistoryListAdapter.VH> {
        private final List<QuoteEntity> items;
        HistoryListAdapter(List<QuoteEntity> list) {
            items = list;
        }
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_favorite_quote, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            QuoteEntity e = items.get(pos);
            h.tvQuote.setText("“" + e.content + "”");
            h.tvAuthor.setText("– " + e.author);
        }
        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvQuote, tvAuthor;
            VH(@NonNull View v) {
                super(v);
                tvQuote  = v.findViewById(R.id.tvFavQuote);
                tvAuthor = v.findViewById(R.id.tvFavAuthor);
            }
        }
    }


    private static class FavoriteListAdapter
            extends RecyclerView.Adapter<FavoriteListAdapter.VH> {
        private final List<QuoteEntity> items;
        FavoriteListAdapter(List<QuoteEntity> list) { items = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_favorite_quote, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            QuoteEntity e = items.get(pos);
            h.tvQuote.setText("“" + e.content + "”");
            h.tvAuthor.setText("– " + e.author);
        }
        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvQuote, tvAuthor;
            VH(@NonNull View v) {
                super(v);
                tvQuote  = v.findViewById(R.id.tvFavQuote);
                tvAuthor = v.findViewById(R.id.tvFavAuthor);
            }
        }
    }
}
