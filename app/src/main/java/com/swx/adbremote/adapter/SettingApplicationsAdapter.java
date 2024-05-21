package com.swx.adbremote.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.swx.adbremote.R;
import com.swx.adbremote.entity.AppItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingApplicationsAdapter extends RecyclerView.Adapter<SettingApplicationsAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    List<AppItem> mData;
    private OnItemDragClickListener itemDragClickListener;
    RequestOptions options = new RequestOptions()
            .placeholder(R.drawable.ic_android_bot)//图片加载出来前，显示的图片
            .fallback( R.drawable.ic_android_bot) //url为空的时候,显示的图片
            .error(R.drawable.ic_android_bot);//图片加载失败后，显示的图片

    public interface ItemLongClickListener {
        void onLongClick(RecyclerView.ViewHolder vh);
    }

    public interface OnItemDragClickListener {
        void onDragClick(RecyclerView.ViewHolder vh);
    }

    public interface OnItemClickListener {
        void onRemoveClick(int position, Integer id);

        void onEditClick(int position, AppItem app);
    }

    private ItemLongClickListener itemLongClickListener;
    private OnItemClickListener onItemClickListener;

    public SettingApplicationsAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_setting_applications, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppItem app = mData.get(position);
        holder.name.setText(app.getName());
        Glide.with(holder.context).load(app.getIcon()).apply(options).into(holder.icon);
        holder.itemView.setOnLongClickListener(v -> {
            itemLongClickListener.onLongClick(holder);
            return true;
        });
//        holder.btnDrag.setOnClickListener(view -> {
//            itemDragClickListener.onDragClick(holder);
//        });
        holder.remove.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            AppItem data = getData(currentPosition);
            onItemClickListener.onRemoveClick(currentPosition, data.getId());
        });
        holder.edit.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            AppItem data = getData(currentPosition);
            onItemClickListener.onEditClick(currentPosition, data);
        });
    }

    public AppItem getData(int position) {
        if (position < 0) return null;
        return mData.get(position);
    }

    public List<AppItem> getData() {
        return mData;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<AppItem> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(AppItem app) {
        this.mData.add(app);
        int itemCount = mData.size();
        notifyItemRangeInserted(itemCount - 1, itemCount);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addOnlineData(List<AppItem> apps) {
        Set<String> sets = new HashSet<>();
        for (AppItem mDatum : mData) {
            sets.add(mDatum.getUrl());
        }
        for (AppItem app : apps) {
            if (!sets.contains(app.getUrl())) {
                mData.add(app);
            }
        }
        notifyDataSetChanged();
    }

    public void remove(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public void swap(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mData, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mData, i, i - 1);
            }
        }
    }


    public void updateData(int position, AppItem app) {
        mData.get(position).update(app);
        notifyItemChanged(position);
    }

    public List<AppItem> setOrder() {
        for (int i = 0; i < mData.size(); i++) {
            mData.get(i).setPriority(i);
        }
        return mData;
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setOnItemLongClickListener(ItemLongClickListener longListener) {
        this.itemLongClickListener = longListener;
    }

    public void setOnItemDragClickListener(OnItemDragClickListener listener) {
        this.itemDragClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        ImageButton remove;
        ImageButton edit;
        Context context;
        ImageButton btnDrag;

        public ViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            icon = itemView.findViewById(R.id.iv_setting_app_icon);
            name = itemView.findViewById(R.id.iv_setting_app_name);
            remove = itemView.findViewById(R.id.btn_setting_delete_app);
            edit = itemView.findViewById(R.id.btn_setting_edit_app);
            btnDrag = itemView.findViewById(R.id.btn_setting_app_drag);
        }
    }
}
