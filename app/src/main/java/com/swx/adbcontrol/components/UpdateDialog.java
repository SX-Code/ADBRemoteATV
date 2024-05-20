package com.swx.adbcontrol.components;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.swx.adbcontrol.R;

/**
 * @Author sxcode
 * @Date 2024/5/16 17:47
 */
public class UpdateDialog extends Dialog implements OnProgressBarListener {
    private Button negativeBtn;
    private TextView tvUpdateInfo;
    private NumberProgressBar updateProgress;
    private OnButtonClickListener listener;

    public UpdateDialog(@NonNull Context context) {
        super(context);
    }

    public UpdateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public UpdateDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public interface OnButtonClickListener {

        public void onNegativeClick(View view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update);
        setCancelable(false); // 进制关闭
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = widthPixels - 200;
            getWindow().setAttributes(params);
        }
        initView();
        initEvent();
    }

    private void initView() {
        negativeBtn = findViewById(R.id.btn_update_negative);
        updateProgress = findViewById(R.id.progress_bar_update);
        tvUpdateInfo = findViewById(R.id.tv_update_info);
    }

    private void initEvent() {
        negativeBtn.setOnClickListener(view -> {
            listener.onNegativeClick(view);
        });
    }

    public void setProgress(int progress) {
        this.updateProgress.setProgress(progress);
    }

    public void setUpdateInfo(String info) {
        if (info.isEmpty()) return;
        tvUpdateInfo.setText(info);
        tvUpdateInfo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProgressChange(int current, int max) {
        if (current == max) {
            updateProgress.setProgress(0);
        }
    }


    public UpdateDialog setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
        return this;
    }
}
