package com.xt.common.recyclerview;

import android.graphics.drawable.PaintDrawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.xt.common.recyclerview.RecyclerViewItemDecoration;

/**
 * @author XuTi on 2019/5/10 16:54
 */
public class MyRecyclerViewUtils {
    public static void setOrientation(RecyclerView recyclerView, int orientation) {
        //LinearLayoutManager.HORIZONTAL
        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        layoutManager.setOrientation(orientation);
        recyclerView.setLayoutManager(layoutManager);
    }

    public static void initRecyclerV(RecyclerView recycler, int orientation) {
        //LinearLayoutManager.VERTICAL
        setOrientation(recycler, orientation);
        recycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (orientation == LinearLayoutManager.VERTICAL) {
            recycler.setVerticalScrollBarEnabled(false);
        }
        if (orientation == LinearLayoutManager.HORIZONTAL) {
            recycler.setHorizontalScrollBarEnabled(false);
        }
    }

    public static RecyclerViewItemDecoration addRecyclerViewItemDecoration(RecyclerView recycler, int orientation, float dividerSize, int colorRes) {
        RecyclerViewItemDecoration recyclerViewItemDecoration = new RecyclerViewItemDecoration(recycler.getContext(), orientation);
        PaintDrawable              drawable                   = new PaintDrawable();
        if (colorRes != 0) {
            drawable.getPaint().setColor(ContextCompat.getColor(recycler.getContext(), colorRes));
        }
        int sizeInPixels = ConvertUtils.dp2px(dividerSize);
        if (orientation == LinearLayoutManager.VERTICAL) {
            drawable.setIntrinsicHeight(sizeInPixels);
        }
        if (orientation == LinearLayoutManager.HORIZONTAL) {
            drawable.setIntrinsicWidth(sizeInPixels);
        }
        recyclerViewItemDecoration.setDrawable(drawable);
        recycler.addItemDecoration(recyclerViewItemDecoration);
        return recyclerViewItemDecoration;
    }
}
