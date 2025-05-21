package com.example.healthmate;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class HabitDialog {
    public interface Callback {
        void onSaved(String name, String category, String notes, int priority);
    }

    public static void show(
            @NonNull Context ctx,
            HabitTrackerActivity.Habit existing,
            @NonNull Callback cb
    ) {

        View content = View.inflate(ctx, R.layout.dialog_habit, null);
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        dialog.setContentView(content);

        // Find views
        TextInputEditText etName         = content.findViewById(R.id.etName);
        ChipGroup          chips          = content.findViewById(R.id.chipGroup);
        HorizontalScrollView scrollPresets = content.findViewById(R.id.scrollPresets);
        ChipGroup          chipPresets    = content.findViewById(R.id.chipPresets);
        TextInputEditText etNotes        = content.findViewById(R.id.etNotes);
        EditText           etPriority     = content.findViewById(R.id.etPriority);
        MaterialButton     btnSave        = content.findViewById(R.id.btnSave);

        // edit mode
        if (existing != null) {
            etName.setText(existing.name);
            etNotes.setText(existing.notes);
            etPriority.setText(String.valueOf(existing.priority));
            for (int i = 0; i < chips.getChildCount(); i++) {
                Chip c = (Chip) chips.getChildAt(i);
                if (c.getText().toString().equals(existing.category)) {
                    c.setChecked(true);
                    break;
                }
            }
        }


        chips.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip == null) return;
            String category = chip.getText().toString();

            String[] presets;
            switch (category) {
                case "Health":
                    presets = new String[]{ "Morning Run", "Gym", "Yoga", "Meditation", "Drink Water" };
                    break;
                case "Work":
                    presets = new String[]{ "Email Inbox", "Team Meeting", "Code Review", "Plan Tasks", "Write Report" };
                    break;
                case "Study":
                    presets = new String[]{ "Read Chapter", "Practice Problems", "Take Notes", "Watch Lecture", "Group Study" };
                    break;
                default:

                    scrollPresets.setVisibility(View.GONE);
                    chipPresets.removeAllViews();
                    return;
            }


            scrollPresets.setVisibility(View.VISIBLE);
            chipPresets.removeAllViews();
            for (String p : presets) {
                Chip preset = new Chip(ctx);
                preset.setText(p);
                preset.setChipBackgroundColorResource(R.color.teal_200);
                preset.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
                preset.setClickable(true);
                preset.setOnClickListener(v -> {
                    etName.setText(p);
                    etName.setSelection(p.length());
                });
                chipPresets.addView(preset);
            }
        });

        // Save action
        btnSave.setOnClickListener(v -> {
            String name  = etName.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();
            int    prio  = 3;
            try { prio = Integer.parseInt(etPriority.getText().toString().trim()); }
            catch (Exception ignored){}

            Chip sel = content.findViewById(chips.getCheckedChipId());
            String category = sel != null ? sel.getText().toString() : "Custom";

            if (name.isEmpty()) {
                etName.setError("Please enter a habit name");
                return;
            }
            cb.onSaved(name, category, notes, prio);
            dialog.dismiss();
        });

        dialog.show();
    }
}
