package com.swx.adbcontrol.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.textfield.TextInputLayout;

/**
 * @Author sxcode
 * @Date 2024/5/18 21:38
 * <a href="https://blog.csdn.net/qingdaoksbk/article/details/52389033">extInputLayout警告时的EditText背景处理</a>
 */
public class NewTextInputLayout extends TextInputLayout {

    public NewTextInputLayout(@NonNull Context context) {
        super(context);
    }

    public NewTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NewTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        recoverEditTextBackGround();
    }

    @Override
    public void setError(@Nullable CharSequence errorText) {
        super.setError(errorText);
        recoverEditTextBackGround();
    }

    /**
     * 将背景颜色重置
     */
    @SuppressLint("RestrictedApi")
    private void recoverEditTextBackGround() {
        if (getEditText() == null) {
            return;
        }
        Drawable editTextBackground = getEditText().getBackground();
        if (editTextBackground == null) {
            return;
        }
        if (androidx.appcompat.widget.DrawableUtils.canSafelyMutateDrawable(editTextBackground)) {
            editTextBackground = editTextBackground.mutate();
        }
        DrawableCompat.clearColorFilter(editTextBackground);
    }
}
