package com.xt.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.xt.common.R;
import com.xt.common.statusbar.ImmersiveStatusBarUtils;

import butterknife.ButterKnife;


/**
 * @author xuti on 2017/8/21.
 */

public abstract class BaseDialog extends Dialog {
    protected ConstraintLayout mRootView;
    protected View             mContentView;

    private boolean              mTouchOutsideCancelable = true;
    private View.OnClickListener mOnClickOutsideListener;

    public BaseDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    protected void init() {
        mRootView = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(getContentLayoutRes(), new ConstraintLayout(getContext()), true);
        mContentView = mRootView.getChildAt(0);
        setContentView(mRootView);
        {
            setMaskColor(R.color.colorBlack9913131A);
        }
        {
            ButterKnife.bind(this, mRootView);
        }

        {
            final Window dialogWindow = getWindow();
            // 获取对话框当前的参数值
            if (dialogWindow != null) {
                {
                    WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
                    layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                    layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                    dialogWindow.setAttributes(layoutParams);
                }
                {
                    dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
                }
                {
                    //核心代码 解决了无法去除遮罩问题
                    dialogWindow.setDimAmount(0f);
                }
            }
        }

        ImmersiveStatusBarUtils.setImmersiveStatus(getWindow());

        {
            mContentView.setClickable(true);
            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTouchOutsideCancelable) {
                        dismiss();
                    }
                    if (mOnClickOutsideListener != null) {
                        mOnClickOutsideListener.onClick(view);
                    }
                }
            });
        }

        initDialog(mRootView);
    }


    /**
     * 可以看到这里定义了一个抽象方法，这个将交由子类去实现
     *
     * @return
     */
    public abstract int getContentLayoutRes();

    protected abstract void initDialog(View view);

    public void setBackgroundColor(int color) {
        mRootView.setBackgroundColor(color);
    }

    /**
     * 设置遮罩颜色
     * 只有FrameLayout可以设置前景
     *
     * @param resId
     */
    private void setForegroundResId(int resId) {
        if (getWindow() != null) {
            View decorView = getWindow().getDecorView();
            if (decorView instanceof FrameLayout) {
                ((FrameLayout) decorView).setForeground(ContextCompat.getDrawable(getContext(), resId));
            }
        }
    }

    /**
     * 设置遮罩颜色
     *
     * @param colorRes
     */
    private void setMaskColor(int colorRes) {
        mRootView.setBackgroundResource(colorRes);
    }

    public void setTouchOutsideCancelable(boolean touchOutsideCancelable) {
        mTouchOutsideCancelable = touchOutsideCancelable;
    }

    public void setOnClickOutsideListener(View.OnClickListener onClickOutsideListener) {
        mOnClickOutsideListener = onClickOutsideListener;
    }

    /*************点击空白地方，输入法隐藏******************/
    private InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    /*************点击空白地方，输入法隐藏******************/
}
