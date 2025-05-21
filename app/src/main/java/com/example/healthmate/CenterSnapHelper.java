package com.example.healthmate;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CenterSnapHelper extends LinearSnapHelper {
    @Override
    public int[] calculateDistanceToFinalSnap(
            @NonNull RecyclerView.LayoutManager lm, @NonNull View target) {
        int[] out = new int[2];
        if (lm.canScrollHorizontally()) {
            int center = (lm.getWidth() - target.getWidth()) / 2;
            int childCenter = target.getLeft() + target.getWidth() / 2;
            out[0] = childCenter - center;
        }
        return out;
    }
}
