package com.xt.common.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author  xuti on 2018/11/1.
 */
public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable mDivider;

    /**
     * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    private int mOrientation;

    private final Rect    mBounds      = new Rect();
    private       boolean isDrawLeft   = true;
    private       boolean isDrawTop    = true;
    private       boolean isDrawRight  = true;
    private       boolean isDrawBottom = true;

    /**
     * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
     * {@link LinearLayoutManager}.
     *
     * @param context     Current context, it will be used to access resources.
     * @param orientation Divider orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    public RecyclerViewItemDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * {@link RecyclerView.LayoutManager} changes orientation.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
    }

    /**
     * Sets the {@link Drawable} for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    public void setDrawable(@NonNull Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("Drawable cannot be null.");
        }
        mDivider = drawable;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    @SuppressLint("NewApi")
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, mBounds);
            if (i == 0) {
                if (isDrawTop) {
                    drawableTopDivider(canvas, left, right, child);
                }
            }
            if (i == childCount - 1) {
                if (isDrawBottom) {
                    drawableBottomDivider(canvas, left, right, child);
                }
            } else {
                drawableBottomDivider(canvas, left, right, child);
            }
        }
        canvas.restore();
    }

    private void drawableBottomDivider(Canvas canvas, int left, int right, View child) {
        final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
        final int top = bottom - mDivider.getIntrinsicHeight();
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(canvas);
    }

    private void drawableTopDivider(Canvas canvas, int left, int right, View child) {
        final int bottom = child.getTop();
        final int top = bottom - mDivider.getIntrinsicHeight();
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(canvas);
    }

    @SuppressLint("NewApi")
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int top;
        final int bottom;
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            canvas.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
            if (i == 0) {
                if (isDrawLeft) {
                    drawableLeftDivider(canvas, top, bottom, child);
                }
            }
            if (i == childCount - 1) {
                if (isDrawRight) {
                    drawableRightDivider(canvas, top, bottom, child);
                }
            } else {
                drawableRightDivider(canvas, top, bottom, child);
            }
        }
        canvas.restore();
    }

    private void drawableRightDivider(Canvas canvas, int top, int bottom, View child) {
        final int right = mBounds.right + Math.round(child.getTranslationX());
        final int left = right - mDivider.getIntrinsicWidth();
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(canvas);
    }

    private void drawableLeftDivider(Canvas canvas, int top, int bottom, View child) {
        final int left = child.getLeft();
        final int right = left + mDivider.getIntrinsicWidth();
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(canvas);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            int top = 0;
            if (parent.getChildAdapterPosition(view) == 0) {
                if (isDrawTop) {
                    top = mDivider.getIntrinsicHeight();
                }
            }
            int bottom = mDivider.getIntrinsicHeight();
            if (parent.getChildAdapterPosition(view) == (parent.getAdapter().getItemCount() - 1)) {
                if (!isDrawBottom) {
                    bottom = 0;
                }
            }
            outRect.set(0, top, 0, bottom);
        } else {
            int left = 0;
            if (parent.getChildAdapterPosition(view) == 0) {
                if (isDrawLeft) {
                    left = mDivider.getIntrinsicWidth();
                }
            }
            int right = mDivider.getIntrinsicWidth();
            if (parent.getChildAdapterPosition(view) == (parent.getAdapter().getItemCount() - 1)) {
                if (!isDrawRight) {
                    right = 0;
                }
            }
            outRect.set(left, 0, right, 0);
        }
    }

    /**
     * 设置左边是否显示
     * @param drawLeft
     */
    public void setDrawLeft(boolean drawLeft) {
        isDrawLeft = drawLeft;
    }

    /**
     * 设置顶部是否显示
     * @param drawTop
     */
    public void setDrawTop(boolean drawTop) {
        isDrawTop = drawTop;
    }

    /**
     * 设置右边是否显示
     * @param drawRight
     */
    public void setDrawRight(boolean drawRight) {
        isDrawRight = drawRight;
    }

    /**
     * 设置底部是否显示
     * @param drawBottom
     */
    public void setDrawBottom(boolean drawBottom) {
        isDrawBottom = drawBottom;
    }
}
