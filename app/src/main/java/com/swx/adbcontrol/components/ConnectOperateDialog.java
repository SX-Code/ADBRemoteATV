package com.swx.adbcontrol.components;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.swx.adbcontrol.R;
import com.swx.adbcontrol.entity.ConnectInstance;
import com.swx.adbcontrol.utils.ValidationUtil;

public class ConnectOperateDialog extends Dialog {
    private EditText textAlias;
    private EditText textIp;
    private EditText textPort;
    private Button positiveBtn;
    private Button negativeBtn;
    private TextInputLayout ipInputLayout;
    private TextInputLayout portInputLayout;
    private Integer id;
    private String alias;
    private String port = "5555";
    private String ip;
    private OnButtonClickListener listener;

    public ConnectOperateDialog(@NonNull Context context) {
        super(context);
    }

    public ConnectOperateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public ConnectOperateDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public interface OnButtonClickListener {
        public void onPositiveClick(Integer id, String alias, String ip, Integer port);

        public void onNegativeClick(View view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_connect_operate);
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
        negativeBtn = findViewById(R.id.btn_negative);
        positiveBtn = findViewById(R.id.btn_positive);
        textIp = findViewById(R.id.text_ip);
        textAlias = findViewById(R.id.text_alias);
        textPort = findViewById(R.id.text_port);
        ipInputLayout = findViewById(R.id.input_layout_text_ip);
        portInputLayout = findViewById(R.id.input_layout_text_port);
    }

    private void initEvent() {
        positiveBtn.setOnClickListener(view -> {
            Editable alias = textAlias.getText();
            Editable ip = textIp.getText();
            Editable port = textPort.getText();
            boolean isPass = checkParams(ip, port);
            if (!isPass) return;
            if (TextUtils.isEmpty(alias)) {
                // 如果没有填写别名，则使用 ip + port
                alias.append(ip);
            }
            listener.onPositiveClick(id, alias.toString().trim(), ip.toString().trim(), Integer.valueOf(port.toString()));
        });
        negativeBtn.setOnClickListener(view -> {
            listener.onNegativeClick(view);
        });
    }

    /**
     * 检查数据
     *
     * @param ip   IP/Host
     * @param port 端口
     */
    private boolean checkParams(Editable ip, Editable port) {
        // 校验IPV4地址
        boolean isPassIPV4 = ValidationUtil.verifyIpv4(ip.toString().trim());
        if (!isPassIPV4) {
            ipInputLayout.setError(getContext().getString(R.string.text_ip_error));
        } else {
            ipInputLayout.setError(null);
        }
        boolean isPassPort = ValidationUtil.verifyPort(port.toString().trim());
        if (!isPassPort) {
            portInputLayout.setError(getContext().getString(R.string.text_port_error));
        } else {
            portInputLayout.setError(null);
        }
        return isPassIPV4 && isPassPort;
    }

    public void setData(ConnectInstance connect) {
        this.id = connect.getId();
        this.alias = connect.getAlias();
        this.ip = connect.getIp();
        this.port = connect.getPort().toString();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        clearData();
        clearVerifyStatus();
    }

    @Override
    public void hide() {
        super.hide();
        clearData();
        clearVerifyStatus();
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    private void refreshView() {
        textAlias.setText(alias);
        textIp.setText(ip);
        textPort.setText(port);

    }

    /**
     * 清除数据
     */
    public void clearData() {
        this.port = "5555";
        this.alias = "";
        this.ip = "";
        this.id = null;
    }

    public void clearVerifyStatus() {
        ipInputLayout.setError(null);
        portInputLayout.setError(null);
    }


    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }
}
