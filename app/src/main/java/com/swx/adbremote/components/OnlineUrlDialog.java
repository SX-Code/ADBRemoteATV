package com.swx.adbremote.components;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.swx.adbremote.R;
import com.swx.adbremote.utils.ValidationUtil;

/**
 * @Author sxcode
 * @Date 2024/5/18 1:12
 */
public class OnlineUrlDialog extends Dialog {

    private Button positiveBtn;
    private Button negativeBtn;
    private EditText mEditTextUrl;
    private TextInputLayout onlineUrlInputLayout;
    private String url;
    private OnlineUrlDialog.OnButtonClickListener listener;

    public OnlineUrlDialog(@NonNull Context context) {
        this(context, R.style.inputDialog);
    }

    public OnlineUrlDialog(@NonNull Context context, String message, String title) {
        super(context);
    }

    public OnlineUrlDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public OnlineUrlDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public interface OnButtonClickListener {
        public void onPositiveClick(String url);

        public void onNegativeClick(View view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_online_url);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = widthPixels - 200;
            getWindow().setAttributes(params);
        }
        initView();
        initEvent();
    }


    private void initView() {
        negativeBtn = findViewById(R.id.btn_url_negative);
        positiveBtn = findViewById(R.id.btn_url_positive);
        mEditTextUrl = findViewById(R.id.et_online_config_path);
        onlineUrlInputLayout = findViewById(R.id.input_layout_online_url);
    }

    private void initEvent() {
        positiveBtn.setOnClickListener(view -> {
            Editable text = mEditTextUrl.getText();
            if (!checkParams(text)) return;
            listener.onPositiveClick(text.toString());
        });
        negativeBtn.setOnClickListener(view -> {
            listener.onNegativeClick(view);
        });
    }

    private boolean checkParams(Editable url) {
        // 校验URL地址
        boolean isPassOnlineUrl = ValidationUtil.verifyUrl(url.toString().trim());
        if (!isPassOnlineUrl) {
            onlineUrlInputLayout.setError(getContext().getString(R.string.text_url_error));
        } else {
            onlineUrlInputLayout.setError(null);
        }
        return isPassOnlineUrl;
    }

    @Override
    public void show() {
        super.show();
        mEditTextUrl.setFocusable(true);
        mEditTextUrl.setFocusableInTouchMode(true);
        mEditTextUrl.requestFocus();
        refreshView();
    }

    private void refreshView() {
        if (!TextUtils.isEmpty(url)) {
            mEditTextUrl.setText(url);
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void hide() {
        super.hide();
        clearVerifyState();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        clearVerifyState();
    }

    public void clearVerifyState() {
        onlineUrlInputLayout.setError(null);
    }

    public void setOnButtonClickListener(OnlineUrlDialog.OnButtonClickListener listener) {
        this.listener = listener;
    }
}
