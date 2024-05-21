package com.swx.adbremote.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.swx.adbremote.MainApplication;
import com.swx.adbremote.R;
import com.swx.adbremote.enums.InputLanguageEnums;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.SharedData;

/**
 * @Author sxcode
 * @Date 2024/5/20 13:45
 * <a href="https://blog.csdn.net/klxh2009/article/details/80393245">...</a>
 */
public class InputKeyboardDialog extends BottomSheetDialog {
    private int topOffset = 0;
    private BottomSheetBehavior<FrameLayout> behavior;
    private EditText mEditText;
    private TextView mTextView;
    private ToggleButton mToggleButton;
    private int inputLanguage;

    private OnClickListener listener;

    public interface OnClickListener {
        void onConfirmClick(View view, String text, int type);

        void onErrorClick();

        void onLanguageToggleClick(int type);

        boolean onBackspaceLongClick(View view);

        boolean onBackspaceTouch(View view, MotionEvent event);

        void onBackspaceClick(View view);

        void onEnterClick(View view);
    }

    public InputKeyboardDialog(Activity context) {
        super(context, R.style.TransparentBottomSheetStyle);
        View view = View.inflate(context, R.layout.bottom_sheet_layout, null);
        setContentView(view);
        init(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(View root) {
        root.findViewById(R.id.btn_close_bottom_dialog).setOnClickListener(view -> {
            this.dismiss();
        });
        root.findViewById(R.id.btn_input_enter).setOnClickListener(view -> listener.onEnterClick(view));

        ImageButton mImageButton = root.findViewById(R.id.btn_input_backspace);
        mImageButton.setOnTouchListener((view, event) -> listener.onBackspaceTouch(view, event));
        mImageButton.setOnLongClickListener(view -> listener.onBackspaceLongClick(view));
        mImageButton.setOnClickListener(view -> listener.onBackspaceClick(view));

        root.findViewById(R.id.tv_input_chinese_unless).setOnClickListener(view -> listener.onErrorClick());
        mToggleButton = root.findViewById(R.id.tb_switch_language);
        mToggleButton.setTextOff(getToggleButtonFont("英/中"));
        mToggleButton.setTextOn(getToggleButtonFont("中/英"));
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    inputLanguage = InputLanguageEnums.CHINESE.code();
                } else {
                    inputLanguage = InputLanguageEnums.ENGLISH.code();
                }
                listener.onLanguageToggleClick(inputLanguage);
            }
        });

        mEditText = root.findViewById(R.id.et_input_text);
        // 监听确认按钮点击事件
        root.findViewById(R.id.btn_confirm_input).setOnClickListener(view -> {
            String text = mEditText.getText().toString();
            listener.onConfirmClick(root, text, inputLanguage);
        });
        initSetting();
    }


    private SpannableStringBuilder getToggleButtonFont(String text) {
        SpannableStringBuilder textSpan = new SpannableStringBuilder(text);
        textSpan.setSpan(new AbsoluteSizeSpan(35), 0, text.indexOf('/') + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textSpan.setSpan(new AbsoluteSizeSpan(25), text.indexOf('/') + 1, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textSpan.setSpan(new ForegroundColorSpan(MainApplication.getContext().getColor(R.color.text_color_gray)), text.indexOf('/') + 1, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return textSpan;
    }

    private void initSetting() {
        inputLanguage = SharedData.getInstance().
                getInt(Constant.KEY_INPUT_LANGUAGE, InputLanguageEnums.CHINESE.code());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToggleButton.setChecked(inputLanguage == InputLanguageEnums.CHINESE.code());
    }

    @Override
    public void onStart() {
        super.onStart();
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();
        FrameLayout bottomSheet = this.getDelegate().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            behavior = BottomSheetBehavior.from(bottomSheet);
        }
        // 初始为展开状态
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public int getTopOffset() {
        return topOffset;
    }

    public void setTopOffset(int topOffset) {
        this.topOffset = topOffset;
    }

    public BottomSheetBehavior<FrameLayout> getBehavior() {
        return behavior;
    }

    public void setOnConfirmClickListener(OnClickListener listener) {
        this.listener = listener;
    }

}
