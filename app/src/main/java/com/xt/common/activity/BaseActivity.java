package com.xt.common.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.xt.common.R;
import com.xt.common.statusbar.ImmersiveStatusBarUtils;
import com.xt.common.statusbar.StatusBarUtil;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/3/20.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beforeSetContentView(savedInstanceState);
        {
            //去除标题栏
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        setContentView(getLayoutResID());
        {
            ButterKnife.bind(this);
        }
        {
            //设置沉浸式状态栏
            ImmersiveStatusBarUtils.setImmersiveStatus(getWindow());
            //设置状态栏颜色
//            StatusBarUtil.setColor(this, getStatusBarColorRes());
        }
        {
            getWindow().getDecorView().setBackgroundResource(getBackgroundResource());
        }
        afterSetContentView(savedInstanceState);
        initViews();
        afterInitViews(savedInstanceState);
        setListener();
        try {
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void beforeSetContentView(Bundle savedInstanceState) {

    }

    protected void afterSetContentView(Bundle savedInstanceState) {

    }

    protected abstract int getLayoutResID();

    protected abstract void initViews();

    protected void afterInitViews(Bundle savedInstanceState) {
    }

    protected void setListener() {
    }

    protected abstract void initData();

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.anim_no);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_no, R.anim.out_to_right);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return new HideInput().dispatchTouchEvent(ev);
    }


    /**********************************设置状态栏颜色*****************************************/
    protected int getStatusBarColorRes() {
        return android.R.color.transparent;
    }

    /**********************************设置状态栏颜色*****************************************/
    protected int getBackgroundResource() {
        return android.R.color.white;
    }

    /**
     * 去除状态栏全屏，一般在app的第一个界面中调用，要在{@link #setContentView(int)}方法之前调用
     */
    public void setFullscreen() {
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 点击空白地方，输入法隐藏
     */
    class HideInput {
        private boolean dispatchTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (isShouldHideInput(v, ev)) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    onHideSoftInputFromWindow();
                }
                return BaseActivity.super.dispatchTouchEvent(ev);
            }
            // 必不可少，否则所有的组件都不会有TouchEvent了
            if (getWindow().superDispatchTouchEvent(ev)) {
                return true;
            }
            return onTouchEvent(ev);
        }

        protected void onHideSoftInputFromWindow() {
            //键盘隐藏后，界面上的edittext的光标要隐藏
        }

        public boolean isShouldHideInput(View v, MotionEvent event) {
            if (v != null && (v instanceof EditText)) {
                int[] leftTop = {0, 0};
                //获取输入框当前的location位置
                v.getLocationInWindow(leftTop);
                int left   = leftTop[0];
                int top    = leftTop[1];
                int bottom = top + v.getHeight();
                int right  = left + v.getWidth();
                if (event.getX() > left && event.getX() < right
                        && event.getY() > top && event.getY() < bottom) {
                    // 点击的是输入框区域，保留点击EditText的事件
//                ((EditText) v).setCursorVisible(true);
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        /*************点击空白地方，输入法隐藏******************/
    }

}