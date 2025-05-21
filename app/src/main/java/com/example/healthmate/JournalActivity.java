package com.example.healthmate;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class JournalActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private JournalAdapter adapter;
    private RecyclerView rv;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        dbHelper = new DBHelper(this);
        rv       = findViewById(R.id.rvJournal);
        fab      = findViewById(R.id.fabAddEntry);

        adapter = new JournalAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadAllEntries();


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) {
                return false;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                final JournalAdapter.Entry e = adapter.getAt(vh.getAdapterPosition());
                dbHelper.delete(e.id);
                adapter.remove(e);
                Snackbar.make(rv, "Deleted entry", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            long newId = dbHelper.insert(e.html);
                            e.id = newId;
                            adapter.addAt(vh.getAdapterPosition(), e);
                        }).show();
            }
        }).attachToRecyclerView(rv);

        // edit
        adapter.setOnItemClick(e -> showEntryDialog(e));

        // Add new entry
        fab.setOnClickListener(v -> showEntryDialog(null));
    }

    //  search
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.journal_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) item.getActionView();
        sv.setQueryHint("Search journal…");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) {
                search(q); return true;
            }
            @Override public boolean onQueryTextChange(String q) {
                search(q); return true;
            }
        });
        return true;
    }

    private void loadAllEntries() {
        List<JournalAdapter.Entry> list = dbHelper.queryAll();
        adapter.submitList(list);
    }

    private void search(String q) {
        List<JournalAdapter.Entry> list = dbHelper.querySearch(q);
        adapter.submitList(list);
    }

    private void showEntryDialog(final JournalAdapter.Entry edit) {
        View dlg = getLayoutInflater().inflate(R.layout.dialog_journal_entry, null);
        RichEditor   editor    = dlg.findViewById(R.id.editor);
        ImageButton  btnBold   = dlg.findViewById(R.id.btnBold);
        ImageButton  btnItalic = dlg.findViewById(R.id.btnItalic);
        ImageButton  btnUnder  = dlg.findViewById(R.id.btnUnderline);

        if (edit != null) editor.setHtml(edit.html);

        btnBold.setOnClickListener(v -> {
            editor.setBold();
            v.setSelected(!v.isSelected());
        });
        btnItalic.setOnClickListener(v -> {
            editor.setItalic();
            v.setSelected(!v.isSelected());
        });
        btnUnder.setOnClickListener(v -> {
            editor.setUnderline();
            v.setSelected(!v.isSelected());
        });

        new AlertDialog.Builder(this)
                .setTitle(edit == null ? "New Entry" : "Edit Entry")
                .setView(dlg)
                .setPositiveButton("Save", (d,w) -> {
                    String html = editor.getHtml();
                    if (edit == null) {
                        long id = dbHelper.insert(html);
                        adapter.addAt(0, new JournalAdapter.Entry(id, html));
                        rv.scrollToPosition(0);
                    } else {
                        dbHelper.update(edit.id, html);
                        edit.html = html;
                        adapter.notifyItemChanged(adapter.indexOf(edit));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private static class DBHelper extends SQLiteOpenHelper {
        private static final String DB  = "journal.db";
        private static final String TBL = "entries";

        DBHelper(JournalActivity ctx) {
            super(ctx, DB, null, 1);
        }
        @Override public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TBL +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT,html TEXT,timestamp INTEGER)");
        }
        @Override public void onUpgrade(SQLiteDatabase db,int o,int n) {
            db.execSQL("DROP TABLE IF EXISTS " + TBL);
            onCreate(db);
        }
        long insert(String html) {
            SQLiteDatabase db = getWritableDatabase();
            long ts = System.currentTimeMillis();
            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put("html", html);
            cv.put("timestamp", ts);
            return db.insert(TBL, null, cv);
        }
        void update(long id, String html) {
            SQLiteDatabase db = getWritableDatabase();
            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put("html", html);
            cv.put("timestamp", System.currentTimeMillis());
            db.update(TBL, cv, "_id=?", new String[]{ String.valueOf(id) });
        }
        void delete(long id) {
            getWritableDatabase().delete(TBL, "_id=?", new String[]{ String.valueOf(id) });
        }
        List<JournalAdapter.Entry> queryAll() {
            return query("SELECT * FROM " + TBL + " ORDER BY timestamp DESC");
        }
        List<JournalAdapter.Entry> querySearch(String q) {
            return query("SELECT * FROM " + TBL +
                            " WHERE html LIKE ? ORDER BY timestamp DESC",
                    "%" + q + "%");
        }
        private List<JournalAdapter.Entry> query(String sql, String... args) {
            List<JournalAdapter.Entry> out = new ArrayList<>();
            Cursor c = getReadableDatabase().rawQuery(sql, args);
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow("_id"));
                String html = c.getString(c.getColumnIndexOrThrow("html"));
                out.add(new JournalAdapter.Entry(id, html));
            }
            c.close();
            return out;
        }
    }
}
