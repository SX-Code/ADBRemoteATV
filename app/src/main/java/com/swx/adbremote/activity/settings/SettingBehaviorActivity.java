package com.swx.adbremote.activity.settings;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.swx.adbremote.R;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.SharedData;

public class SettingBehaviorActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean behaviorHapticFeedback;
    private SwitchCompat switchBehaviorHapticFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_behavior);
        initSetting();
        init();
        initEvent();
    }

    /**
     * 初始化配置数据
     */
    private void initSetting() {
        behaviorHapticFeedback = SharedData.getInstance().
                getBoolean(Constant.KEY_SETTING_BEHAVIOR_HAPTIC_FEEDBACK, true);
    }

    private void init() {
        switchBehaviorHapticFeedback = findViewById(R.id.switch_behavior_haptic_feedback);

        // 设置样式
        switchBehaviorHapticFeedback.setChecked(behaviorHapticFeedback);
    }

    private void initEvent() {
        findViewById(R.id.btn_setting_behavior_back).setOnClickListener(this);
        switchBehaviorHapticFeedback.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.switch_behavior_haptic_feedback) {
            SharedData.getInstance().put(Constant.KEY_SETTING_BEHAVIOR_HAPTIC_FEEDBACK, switchBehaviorHapticFeedback.isChecked()).commit();
        } else if (id == R.id.btn_setting_behavior_back) {
            finish();
        }
    }

}