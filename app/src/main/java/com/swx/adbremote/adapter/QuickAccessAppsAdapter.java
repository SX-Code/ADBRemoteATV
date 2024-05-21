package com.swx.adbremote.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.swx.adbremote.R;
import com.swx.adbremote.entity.AppItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author sxcode
 * @Date 2024/5/17 23:43
 */
public class QuickAccessAppsAdapter extends RecyclerView.Adapter<QuickAccessAppsAdapter.ViewHolder> {

    private final List<AppItem> mData;
    private final LayoutInflater mInflater;

    private int layoutId;

    public interface OnItemClickListener {
        void onClick(int position, AppItem app);
    }

    private OnItemClickListener onItemClickListener;

    public QuickAccessAppsAdapter(LayoutInflater inflater, int direction) {
        mInflater = inflater;
        mData = new ArrayList<>();
        if (direction == LinearLayout.HORIZONTAL) {
            layoutId = R.layout.item_apps_home;
        } else {
            layoutId = R.layout.item_apps;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(layoutId, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppItem app = mData.get(position);
        Glide.with(holder.context).load(app.getIcon()).into(holder.app);
        holder.name.setText(app.getName());
        holder.name.setSelected(true);
        holder.app.setOnClickListener(view -> onItemClickListener.onClick(position, mData.get(position)));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<AppItem> apps) {
        mData.clear();
        mData.addAll(apps);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView app;
        TextView name;
        Context context;

        public ViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            this.app = itemView.findViewById(R.id.iv_quick_access_app_icon);
            this.name = itemView.findViewById(R.id.iv_quick_access_app_name);
        }
    }
}
