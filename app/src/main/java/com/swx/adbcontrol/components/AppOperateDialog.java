package com.swx.adbcontrol.components;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.swx.adbcontrol.R;
import com.swx.adbcontrol.entity.AppItem;
import com.swx.adbcontrol.utils.ValidationUtil;

public class AppOperateDialog extends Dialog {
    private EditText etName;
    private EditText etIconPath;
    private EditText etStartPath;
    private Button positiveBtn;
    private Button negativeBtn;
    private TextInputLayout appIconInputLayout;
    private TextInputLayout appUrlInputLayout;
    private TextInputLayout appNameInputLayout;
    private Integer id;
    private String icon;
    private String name;
    private String url;
    private OnButtonClickListener listener;

    public AppOperateDialog(@NonNull Context context) {
        super(context);
    }

    public AppOperateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public AppOperateDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public interface OnButtonClickListener {
        public void onPositiveClick(Integer id, String name, String icon, String url);

        public void onNegativeClick(View view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_app_operate);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = widthPixels - 200;
            getWindow().setAttributes(params);
        }
        initView();
        initEvent();
    }

    private void initView() {
        negativeBtn = findViewById(R.id.btn_app_negative);
        positiveBtn = findViewById(R.id.btn_app_positive);
        etIconPath = findViewById(R.id.et_icon_path);
        etName = findViewById(R.id.et_app_name);
        etStartPath = findViewById(R.id.et_start_path);
        appIconInputLayout = findViewById(R.id.input_layout_et_icon_path);
        appUrlInputLayout = findViewById(R.id.input_layout_et_start_path);
        appNameInputLayout = findViewById(R.id.input_layout_et_app_name);
    }

    private void initEvent() {
        positiveBtn.setOnClickListener(view -> {
            Editable name = etName.getText();
            Editable icon = etIconPath.getText();
            Editable url = etStartPath.getText();
            boolean isPass = checkParams(name, icon, url);
            if (!isPass) return;
            if (TextUtils.isEmpty(icon)) {
                icon.append("");
            }
            listener.onPositiveClick(id, name.toString(), icon.toString(), url.toString());
        });
        negativeBtn.setOnClickListener(view -> {
            listener.onNegativeClick(view);
        });
    }

    /**
     * 检查数据
     *
     * @param icon 图标url
     * @param url  启动路径
     */
    private boolean checkParams(Editable name, Editable icon, Editable url) {
        if (TextUtils.isEmpty(name)) {
            appNameInputLayout.setError(getContext().getString(R.string.text_app_name_error));
        } else {
            appNameInputLayout.setError(null);
        }
        boolean isPassIconUrl = ValidationUtil.verifyUrl(icon.toString().trim());
        if (!isPassIconUrl) {
            appIconInputLayout.setError(getContext().getString(R.string.text_app_icon_error));
        } else {
            appIconInputLayout.setError(null);
        }
        boolean isPassUrl = ValidationUtil.verifyStartPath(url.toString().trim());
        if (!isPassIconUrl) {
            appUrlInputLayout.setError(getContext().getString(R.string.text_app_url_error));
        } else {
            appUrlInputLayout.setError(null);
        }
        return !TextUtils.isEmpty(name) && isPassUrl && isPassIconUrl;
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    private void refreshView() {
        etName.setText(name);
        etIconPath.setText(icon);
        etStartPath.setText(url);
    }

    public void setData(AppItem app) {
        this.name = app.getName();
        this.url = app.getUrl();
        this.icon = app.getIcon();
        this.id = app.getId();
    }

    @Override
    public void hide() {
        super.hide();
        clearData();
        clearVerifyState();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        clearData();
        clearVerifyState();
    }

    /**
     * 清除数据
     */
    public void clearData() {
        this.name = null;
        this.url = null;
        this.icon = null;
        this.id = null;
    }

    public void clearVerifyState() {
        appNameInputLayout.setError(null);
        appIconInputLayout.setError(null);
        appUrlInputLayout.setError(null);
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }
}
