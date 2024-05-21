package com.swx.adbremote.components;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.swx.adbremote.MainApplication;
import com.swx.adbremote.R;

/**
 * @Author sxcode
 * @Date 2024/5/16 17:47
 */
public class QuestionDialog extends Dialog {
    private TextView textQuestionTitle;
    private TextView textQuestionMessage;
    private Button positiveBtn;
    private Button negativeBtn;
    private String message;
    private String title;
    private String positiveBtnText;
    private OnButtonClickListener listener;

    public QuestionDialog(@NonNull Context context) {
        super(context);
    }

    public QuestionDialog(@NonNull Context context, String title, String message) {
        super(context);
        this.message = message;
        this.title = title;
    }

    public QuestionDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public QuestionDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public interface OnButtonClickListener {
        public void onPositiveClick(View view);

        public void onNegativeClick(View view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_question);
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

    public static QuestionDialog build(Context context, String title, String message) {
        return new QuestionDialog(context, title, message);
    }

    private void initView() {
        negativeBtn = findViewById(R.id.btn_question_negative);
        positiveBtn = findViewById(R.id.btn_question_positive);
        textQuestionMessage = findViewById(R.id.text_question_message);
        textQuestionTitle = findViewById(R.id.text_question_title);
    }

    private void initEvent() {
        positiveBtn.setOnClickListener(view -> {
            listener.onPositiveClick(view);
        });
        negativeBtn.setOnClickListener(view -> {
            listener.onNegativeClick(view);
        });
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    private void refreshView() {
        if (!TextUtils.isEmpty(title)) {
            textQuestionTitle.setText(title);
        }
        if (!TextUtils.isEmpty(message)) {
            textQuestionMessage.setText(message);
        }
        if (!TextUtils.isEmpty(positiveBtnText)) {
            positiveBtn.setText(positiveBtnText);
        }
    }


    public QuestionDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public void setPositiveBtnText(String text) {
        this.positiveBtnText = text;
    }

    public QuestionDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public QuestionDialog setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
        return this;
    }
}
