package com.heim.dragdrawlayout.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zxp on 2016/8/9 0009.
 */
public class DragLayout extends FrameLayout {

    private ViewDragHelper mDragHelper;
    private ViewGroup mMenuView;
    private ViewGroup mMainView;
    private int mRange;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private OnStatusChangeListener mListener;

    Status status = Status.Close; //默认
    public enum Status{
        Open,Close, Draging;
    }

    public interface OnStatusChangeListener {
        void onOpen();
        void onClose();
        void onDraging(float percent);
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.mListener = listener;
    }


    public DragLayout(Context context) {
        this(context,null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);

    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDragHelper = ViewDragHelper.create(this, 1.0f, cb);

    }

    ViewDragHelper.Callback cb = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int oldLeft = child.getLeft();
            System.out.println("clampViewPositionHorizontal:left: " + left + " dx: " + dx + " oldLeft: " + oldLeft);
            if (child == mMainView) {
               left = limitLeft(left);
            }
            return left;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == mMenuView) {
                mMenuView.layout(0, 0, 0 + mMeasuredWidth, 0 + mMeasuredHeight);
                int newLeft = mMainView.getLeft() + dx;

                 newLeft = limitLeft(newLeft);

                mMainView.layout(newLeft,0,newLeft+mMeasuredWidth,0+mMeasuredHeight);
            }

            dispatch();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (Math.abs(xvel)<=30 && mMainView.getLeft() > mRange * 0.5f) {
                // 水平速度较小时, 根据位置判断打开
                open();
            }else if(xvel>30){
                // 向右的速度较大时, 打开
                open();
            }else{
                close();
            }

        }
    };

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    //关闭
    private void close() {
        close(true);
    }

    private void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {
            if (mDragHelper.smoothSlideViewTo(mMainView, finalLeft, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }else{
            mMainView.layout(finalLeft,0,finalLeft+mMeasuredWidth,mMeasuredHeight);
        }
    }

    //打开
    private void open() {
        open(true);
    }

    private void open(boolean isSmooth) {
        int finalLeft = mRange;
        if (isSmooth) {
            if (mDragHelper.smoothSlideViewTo(mMainView, finalLeft, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }else{
            mMainView.layout(finalLeft,0,finalLeft+mMeasuredWidth,mMeasuredHeight);
        }
    }

    private void dispatch() {
        float percent = mMainView.getLeft() *1.0f/ mRange;

        animViews(percent);

        if (mListener != null) {
            mListener.onDraging(percent);
        }

        Status lastStatus = Status.Close;
        status = upDataStatus(percent);

        if (lastStatus != status && mListener != null) {
            if (status == Status.Close) {
                mListener.onClose();
            } else if (status == Status.Open) {
                mListener.onOpen();
            }
        }

    }

    private Status upDataStatus(float percent) {
        if (percent == 0) {
            return Status.Close;
        } else if (percent == 1) {
            return Status.Open;
        }

        return Status.Draging;
    }

    private void animViews(float percent) {
        //左菜单 缩放,平移,透明度动画

        //缩放动画
        ViewHelper.setScaleX(mMenuView, evaluate(percent, 0.5f, 1.0f));
        ViewHelper.setScaleY(mMenuView, evaluate(percent, 0.5f, 1.0f));
        //透明度动画
        ViewHelper.setAlpha(mMenuView, evaluate(percent, 0.3f, 1.0f));
        //平移动画
        ViewHelper.setTranslationX(mMenuView, evaluate(percent, -mMeasuredWidth/2, 0));

        //主界面
        //缩放动画
        ViewHelper.setScaleX(mMainView, evaluate(percent, 1.0f, 0.8f));
        ViewHelper.setScaleY(mMainView, evaluate(percent, 1.0f, 0.8f));

        //背景 亮度变化
        Drawable background = getBackground();
        background.setColorFilter((Integer) evaluateColor(percent, Color.BLACK,Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                (int)((startB + (int)(fraction * (endB - startB))));
    }

    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    //限定左右边界
    private int limitLeft(int left) {
        if (left < 0) {
            left = 0;
        }else if(left>mRange){
            left = mRange;
        }

        return left;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();

        mRange = (int) (mMeasuredWidth * 0.6f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount < 2) {
            throw new IllegalStateException("Your child must be at least 2");
        }

        if (!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalStateException("Your child must be an instance of ViewGroup");
        }

        mMenuView = (ViewGroup) getChildAt(0);
        mMainView = (ViewGroup) getChildAt(1);
    }
}
