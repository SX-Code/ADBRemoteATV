package com.swx.adbremote.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @Author sxcode
 * @Date 2024/5/17 19:02
 * <a href="https://juejin.cn/post/6965412976383754276">...</a>
 */
public class RecyclerViewItemEqspa {


    /**
     * Recycler View等间隔
     */
    public static void equilibriumAssignmentOfLinear(int unit, Rect outRect, View view, RecyclerView parent) {
        int itemCount = getItemCount(parent);
        int itemPosition = parent.getChildAdapterPosition(view);
        LinearLayoutManager layoutManager = checkLinearLayoutManager(parent);
        // 获取 LinearLayoutManager 的布局方向
        if (layoutManager == null) return;
        int orientation = layoutManager.getOrientation();
        if (itemPosition == 0) {
            if (orientation == RecyclerView.VERTICAL) {
                // 第一行/列 && VERTICAL 布局方式 -> 对item的底部特殊处理
                outRect.top = unit * 2;
                outRect.bottom = unit;
                outRect.left = unit * 2;
                outRect.right = unit * 2;
            } else {
                // 第一行/列 && HORIZONTAL 布局方式 -> 对item的右边特殊处理
                outRect.top = unit * 2;
                outRect.bottom = unit * 2;
                outRect.left = unit * 2;
                outRect.right = unit;
            }
        } else if (itemPosition == itemCount - 1) {
            if (orientation == RecyclerView.VERTICAL) {
                // 最后一行/列 && VERTICAL 布局方式 -> 对item的顶部特殊处理
                outRect.top = unit;
                outRect.bottom = unit * 2;
                outRect.left = unit * 2;
                outRect.right = unit * 2;
            } else {
                // 最后一行/列 && HORIZONTAL 布局方式 -> 对item的左边特殊处理
                outRect.top = unit * 2;
                outRect.bottom = unit * 2;
                outRect.left = unit;
                outRect.right = unit * 2;
            }
        } else {
            if (orientation == RecyclerView.VERTICAL) {
                // 中间的行/列 && VERTICAL 布局方式 -> 对item的顶部和底部特殊处理
                outRect.top = unit;
                outRect.bottom = unit;
                outRect.left = unit * 2;
                outRect.right = unit * 2;
            } else {
                // 中间的行/列 && HORIZONTAL 布局方式 -> 对item的左边和右边特殊处理
                outRect.top = unit * 2;
                outRect.bottom = unit * 2;
                outRect.left = unit;
                outRect.right = unit;
            }
        }
    }

    /**
     * 两边间距为0，中间间距相等
     *
     * @param unit 间距单位
     */
    public static void equilibriumAssignmentOfGrid(int unit, Rect outRect, View view, RecyclerView parent) {
        int itemCount = getItemCount(parent);
        int spanCount = getSpanCount(parent);
        int itemPosition = parent.getChildAdapterPosition(view);
        GridLayoutManager gridLayoutManager = checkGridLayoutManager(parent);
        if (gridLayoutManager == null) return;
        if (spanCount < 2) {
            equilibriumAssignmentOfLinear(unit, outRect, view, parent);
            return;
        }
        int orientation = gridLayoutManager.getOrientation();
        if (orientation == RecyclerView.HORIZONTAL) {
            // 暂不支持横向布局的 GridLayoutManager
            throw new UnsupportedOperationException("You can’t set a horizontal grid layout because we don’t support！");
        }
        if (itemPosition % spanCount == 0) {
            // 最左边的那一列
            outRect.left = 0;
            outRect.right = unit * 2;
        } else if (itemPosition - (spanCount - 1) % spanCount == 0) {
            // 最右边的那一列
            outRect.left = unit * 2;
            outRect.right = 0;
        } else {
            outRect.left = unit;
            outRect.right = unit;
        }
        outRect.top = unit * 2;
        // 判断是否为最后一行，最后一行单独添加底部的间距, spanCount * Math.ceil((double)itemCount / spanCount) - spanCount
        // 下式不可化简，括号内先计算，利用 Java 特性得到的是整数
        if (itemPosition >= spanCount * ((itemCount + spanCount - 1) / spanCount) - spanCount) {
            outRect.bottom = unit * 2;
        }
    }

    /**
     * 获取 spanCount
     * 注：此方法只针对设置 LayoutManager 为 GridLayoutManager 的 RecyclerView 生效
     */
    private static int getSpanCount(RecyclerView recyclerView) {
        GridLayoutManager gridLayoutManager = checkGridLayoutManager(recyclerView);
        return gridLayoutManager == null ? 0 : gridLayoutManager.getSpanCount();
    }

    private static int getItemCount(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        return layoutManager == null ? 0 : layoutManager.getItemCount();
    }

    private static LinearLayoutManager checkLinearLayoutManager(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            return null;
        }
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalStateException("Make sure you are using the LinearLayoutManager！");
        }
        return (LinearLayoutManager) layoutManager;
    }

    private static GridLayoutManager checkGridLayoutManager(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            return null;
        }
        if (!(layoutManager instanceof GridLayoutManager)) {
            throw new IllegalStateException("Make sure you are using the GridLayoutManager！");
        }
        return (GridLayoutManager) layoutManager;
    }

}
