package com.swx.adbremote.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.swx.adbremote.R;

public class IndicatorView extends View {

    // 指示器容器 宽/高
    private int mIndicatorWidth = 0;
    private int mIndicatorHeight = 0;

    // 指示器 item 宽/高
    private int mItemWidth = 0;
    private int mItemHeight = 0;

    // 指示器item的间隔
    private int mIndicatorItemDistance = 0;

    // 指示器个数
    private int mIndicatorItemCount = 0;

    // 首个item的起点
    private float mstartPos = 0f;

    // item画笔 选中态/未选中态
    private Paint mSelectedPaint = new Paint();
    private Paint mUnSelectedPaint = new Paint();

    // item画笔颜色 选中态/未选中态
    private int mColorSelected = Color.WHITE;
    private int mColorUnSelected = Color.GRAY;

    // 当前选中位置
    private int mCurrentSelectedPosition = 0;

    private boolean isCircle = true;
    private int visibility = VISIBLE;

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        this.mColorSelected = a.getColor(R.styleable.IndicatorView_colorSelected, Color.WHITE);
        this.mColorUnSelected = a.getColor(R.styleable.IndicatorView_colorUnSelected, Color.GRAY);
        a.recycle();

        // 配置paint画笔
        mSelectedPaint.setStyle(Paint.Style.FILL);
        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setColor(mColorSelected);

        mUnSelectedPaint.setStyle(Paint.Style.FILL);
        mUnSelectedPaint.setAntiAlias(true);
        mUnSelectedPaint.setColor(mColorUnSelected);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mIndicatorWidth = MeasureSpec.getSize(widthMeasureSpec);
        mIndicatorHeight = MeasureSpec.getSize(heightMeasureSpec);
        // item宽度 = 指示器宽度 / （item个数 + 间隔个数）
        mItemWidth = mIndicatorWidth / (mIndicatorItemCount + mIndicatorItemCount - 1);
        // item高度 = item宽度和指示器高度中的最小值，避免绘制不全
        mItemHeight = Math.min(mItemWidth, mIndicatorHeight);
        // 绘制item的起始位置 = 指示器宽度 / 2 - 绘制区域/2，保持绘制区域居中显示
        mstartPos = mIndicatorWidth / 2f - ((mIndicatorItemCount + mIndicatorItemCount - 1) * mItemHeight) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float dy = mIndicatorHeight / 2f;
        // 圆半径
        float cr = mItemHeight / 2f;
        for (int i = 0; i < mIndicatorItemCount; i++) {
            mIndicatorItemDistance = mItemHeight;
            // 动态计算每个item的起始绘制位置
            float dx = mstartPos + i * mItemHeight + i * mIndicatorItemDistance + cr;
            // item选中状态在大小和颜色有所不同
            Paint paint = mUnSelectedPaint;
            if (i == mCurrentSelectedPosition) {
                paint = mSelectedPaint;
            }
            canvas.drawCircle(dx, dy, cr, paint);
        }
    }

    private void indicatorVisibility() {
        if (mCurrentSelectedPosition >= mIndicatorItemCount) {
            mCurrentSelectedPosition = mIndicatorItemCount - 1;
        }
        // 小于1个不显示
        if (mIndicatorItemCount <= 1) {
            visibility = GONE;
        }
    }

    public void setIndicatorCount(int count) {
        this.mIndicatorItemCount = count;
    }

    public void setCurrentSelectedPosition(int pos) {
        this.mCurrentSelectedPosition = pos;
    }
}
