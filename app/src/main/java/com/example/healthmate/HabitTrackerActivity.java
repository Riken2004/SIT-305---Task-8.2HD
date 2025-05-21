package com.example.healthmate;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class HabitTrackerActivity extends AppCompatActivity {
    private final List<Habit> habits = new ArrayList<>();
    private HabitAdapter       adapter;
    private RecyclerView       rvHabits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_HealthMate);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        rvHabits = findViewById(R.id.rvHabits);
        adapter  = new HabitAdapter(habits);
        rvHabits.setLayoutManager(new LinearLayoutManager(this));
        rvHabits.setAdapter(adapter);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh,
                                 int direction) {
                int pos = vh.getAdapterPosition();
                Habit removed = habits.remove(pos);
                adapter.notifyItemRemoved(pos);
                Snackbar.make(rvHabits,
                                "Deleted “" + removed.name + "”",
                                Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            habits.add(pos, removed);
                            adapter.notifyItemInserted(pos);
                        }).show();
            }
        }).attachToRecyclerView(rvHabits);


        ExtendedFloatingActionButton fab = findViewById(R.id.fabAddHabit);
        fab.setOnClickListener(v -> showCategoryDialog());
    }

    // choose category
    private void showCategoryDialog() {
        final String[] cats = { "Health", "Work", "Study", "Custom" };
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Category")
                .setItems(cats, (d, which) -> {
                    String cat = cats[which];
                    if ("Custom".equals(cat)) {

                        HabitDialog.show(this, null, this::addHabit);
                    } else {

                        showPresetDialog(cat);
                    }
                })
                .show();
    }


    private void showPresetDialog(String category) {
        final String[] opts;
        switch (category) {
            case "Health":
                opts = new String[]{ "Morning Run", "Gym", "Yoga", "Meditation", "Drink Water" };
                break;
            case "Work":
                opts = new String[]{ "Email Inbox", "Team Meeting", "Code Review", "Plan Tasks", "Write Report" };
                break;
            case "Study":
                opts = new String[]{ "Read Chapter", "Practice Problems", "Take Notes", "Watch Lecture", "Group Study" };
                break;
            default:
                return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select " + category + " Habit")
                .setItems(opts, (d, which) -> {
                    String preset = opts[which];
                    // Prefill the BottomSheet with the chosen preset
                    Habit existing = new Habit(preset, category, "", 3);
                    HabitDialog.show(this, existing, this::addHabit);
                })
                .show();
    }

    // Add a new habit
    private void addHabit(String name, String category, String notes, int priority) {
        habits.add(new Habit(name, category, notes, priority));
        adapter.notifyItemInserted(habits.size() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            int mode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
                    ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(mode);
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class Habit {
        String name, category, notes;
        int    priority, doneCount;
        boolean done;
        public Habit(String n, String c, String no, int p) {
            name = n; category = c; notes = no; priority = p;
            done = false; doneCount = 0;
        }
    }

    class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.VH> {
        private final List<Habit> list;
        HabitAdapter(List<Habit> l) { list = l; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_habit, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Habit hb = list.get(pos);
            h.tvName.setText(hb.name);
            h.tvNotes.setText(hb.notes);
            h.ivCat.setImageResource(iconFor(hb.category));
            h.vPriority.setBackgroundColor(colorFor(hb.priority));

            // Checkbox & streak
            h.cbDone.setOnCheckedChangeListener(null);
            h.cbDone.setChecked(hb.done);
            h.cbDone.setOnCheckedChangeListener((btn, checked) -> {
                hb.done = checked;
                if (checked) {
                    hb.doneCount++;
                    animateDone(h.card);
                    if (hb.doneCount % 7 == 0) {
                        Snackbar.make(rvHabits,
                                "🎉 7-day streak of “" + hb.name + "”!",
                                Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            // Tap to edit
            h.card.setOnClickListener(v ->
                    HabitDialog.show(HabitTrackerActivity.this, hb,
                            (n, c, no, p) -> {
                                hb.name = n; hb.category = c; hb.notes = no; hb.priority = p;
                                notifyItemChanged(pos);
                            })
            );
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            MaterialCardView card;
            ImageView        ivCat;
            TextView         tvName, tvNotes;
            View             vPriority;
            CheckBox         cbDone;

            VH(@NonNull View v) {
                super(v);
                card      = v.findViewById(R.id.cardHabit);
                ivCat     = v.findViewById(R.id.ivCategory);
                tvName    = v.findViewById(R.id.tvHabitName);
                tvNotes   = v.findViewById(R.id.tvNotes);
                vPriority = v.findViewById(R.id.vPriority);
                cbDone    = v.findViewById(R.id.cbDone);
            }
        }

        private int iconFor(String cat) {
            switch (cat) {
                case "Health": return R.drawable.ic_health;
                case "Work":   return R.drawable.ic_work;
                case "Study":  return R.drawable.ic_study;
                default:       return R.drawable.ic_custom;
            }
        }

        private int colorFor(int p) {
            float frac = (5 - p) / 4f;
            return Color.HSVToColor(new float[]{ frac * 120, 0.8f, 0.9f });
        }

        private void animateDone(View v) {
            v.animate()
                    .scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() ->
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                    ).start();
        }
    }
}
