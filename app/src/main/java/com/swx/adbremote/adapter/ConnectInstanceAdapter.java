package com.swx.adbremote.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.swx.adbremote.R;
import com.swx.adbremote.entity.ConnectInstance;

import java.util.ArrayList;
import java.util.List;

public class ConnectInstanceAdapter extends RecyclerView.Adapter<ConnectInstanceAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    List<ConnectInstance> mData;

    public interface ItemClickListener {
        public void onItemClick(int position);
    }

    public interface ItemLongClickListener {
        public void onLongClick(int position);
    }

    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    public ConnectInstanceAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_connect_instace, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConnectInstance instance = mData.get(position);
        holder.alias.setText(instance.getAlias());
        holder.uri.setText(String.format("%s:%s", instance.getIp(), instance.getPort()));
        if (instance.isActive()) {
            // 连接中的item样式
            holder.icon.setBackgroundColor(Color.parseColor("#1EAA63"));
        } else {
            holder.icon.setBackgroundColor(ContextCompat.getColor(holder.context, R.color.btn_bg_color));
        }

        if (instance.isSelect()) {
            holder.icon.setImageResource(R.drawable.ic_check);
            holder.icon.setBackgroundColor(Color.parseColor("#19CAAD"));
            holder.icon.setPadding(16, 16, 16, 16);
        } else {
            holder.icon.setImageResource(R.drawable.ic_android_bot);
            if (instance.isActive()) {
                holder.icon.setBackgroundColor(Color.parseColor("#1EAA63"));
            } else {
                holder.icon.setBackgroundColor(ContextCompat.getColor(holder.context, R.color.btn_bg_color));
            }
        }

        holder.progressBar.setVisibility(instance.isConnecting() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(o -> itemClickListener.onItemClick(position));
        holder.itemView.setOnLongClickListener(v -> {
            itemLongClickListener.onLongClick(position);
            return true;
        });
    }

    public ConnectInstance getData(int position) {
        if (position < 0) return null;
        return mData.get(position);
    }

    public List<ConnectInstance> getData() {
        return mData;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<ConnectInstance> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(ConnectInstance instance) {
        this.mData.add(instance);
        int itemCount = getItemCount();
        notifyItemRangeChanged(itemCount - 1, itemCount);
    }

    public void connectData(int position, boolean isConnect) {
        mData.get(position).setConnecting(isConnect);
        notifyItemChanged(position);
    }

    public void selectData(int position, boolean select) {
        mData.get(position).setSelect(select);
        notifyItemChanged(position);
    }

    public int updateData(ConnectInstance connectInstance) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getId().equals(connectInstance.getId())) {
                mData.get(i).update(connectInstance);
                notifyItemChanged(i);
                return i;
            }
        }
        return -1;
    }


    public ConnectInstance getSelected() {
        for (ConnectInstance mDatum : mData) {
            if (mDatum.isSelect()) {
                return mDatum;
            }
        }
        return null;
    }

    public int getSelectedIndex() {
        // 清除选中的item
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).isSelect()) {
                return i;
            }
        }
        return -1;
    }


    public boolean getSelectedIds(ArrayList<Integer> ids) {
        boolean containConnected = false;
        for (ConnectInstance mDatum : mData) {
            if (mDatum.isSelect()) {
                if (mDatum.isActive()) containConnected = true;
                ids.add(mDatum.getId());
            }
        }
        return containConnected;
    }

    public void clearData(int position) {
        ConnectInstance instance = mData.get(position);
        instance.setActive(false);
        instance.setConnecting(false);
        notifyItemChanged(position);
    }

    public void clearSelection() {
        // 清除选中的item
        for (int i = 0; i < mData.size(); i++) {
            ConnectInstance instance = mData.get(i);
            if (instance.isSelect()) {
                instance.setSelect(false);
                notifyItemChanged(i);
            }
        }
    }

    public void clearActive() {
        // 清除选中的item
        for (int i = 0; i < mData.size(); i++) {
            ConnectInstance instance = mData.get(i);
            if (instance.isActive()) {
                instance.setActive(false);
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        itemClickListener = listener;
    }

    public void setOnItemLongClickListener(ItemLongClickListener longListener) {
        this.itemLongClickListener = longListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView alias;
        TextView uri;
        ImageView icon;
        ProgressBar progressBar;
        Context context;

        public ViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            alias = itemView.findViewById(R.id.tv_connect_name);
            uri = itemView.findViewById(R.id.tv_connect_uri);
            icon = itemView.findViewById(R.id.tv_connect_icon);
            progressBar = itemView.findViewById(R.id.pb_connecting);
        }
    }
}
