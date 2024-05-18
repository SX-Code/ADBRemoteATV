package com.swx.adbcontrol.activity.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.swx.adbcontrol.R;
import com.swx.adbcontrol.enums.SettingLayoutEnums;
import com.swx.adbcontrol.utils.Constant;
import com.swx.adbcontrol.utils.SharedData;

import java.util.HashMap;
import java.util.Map;

public class SettingLayoutActivity extends AppCompatActivity implements View.OnClickListener {
    private int settingLayoutNav;
    private int settingLayoutQuickAccess;
    private Map<Integer, ConstraintLayout> layoutNavMap;
    private Map<Integer, RadioButton> rbNavMap;
    private Map<Integer, ConstraintLayout> layoutQuickAccessMap;
    private Map<Integer, RadioButton> rbQuickAccessMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initSetting();
        init();
        initEvent();
    }

    /**
     * 初始化配置数据
     */
    private void initSetting() {
        settingLayoutNav = SharedData.getInstance().
                getInt(Constant.KEY_SETTING_LAYOUT_NAV, SettingLayoutEnums.NAVIGATION_DIRECTION.code());
        settingLayoutQuickAccess = SharedData.getInstance().
                getInt(Constant.KEY_SETTING_LAYOUT_QUICK_ACCESS, SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code());
    }

    private void init() {
        layoutNavMap = new HashMap<>();
        layoutQuickAccessMap = new HashMap<>();
        layoutNavMap.put(SettingLayoutEnums.NAVIGATION_DIRECTION.code(), (ConstraintLayout) findViewById(R.id.layout_direction_key));
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code(), (ConstraintLayout) findViewById(R.id.layout_tv_app));
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code(), (ConstraintLayout) findViewById(R.id.layout_media_button));
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_NONE.code(), (ConstraintLayout) findViewById(R.id.layout_none));

        rbNavMap = new HashMap<>();
        rbQuickAccessMap = new HashMap<>();
        rbNavMap.put(SettingLayoutEnums.NAVIGATION_DIRECTION.code(), (RadioButton) findViewById(R.id.rb_direction_key));
        rbQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code(), (RadioButton) findViewById(R.id.rb_tv_app));
        rbQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code(), (RadioButton) findViewById(R.id.rb_media_button));
        rbQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_NONE.code(), (RadioButton) findViewById(R.id.rb_none));

        // 设置样式
        setBGSelect(settingLayoutNav);
        setBGSelect(settingLayoutQuickAccess);
    }

    @SuppressWarnings("all")
    private void initEvent() {
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutNavMap.entrySet()) {
            Integer key = entry.getKey();
            entry.getValue().setOnClickListener(SettingLayoutActivity.this);
            rbNavMap.get(key).setOnClickListener(SettingLayoutActivity.this);
        }
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutQuickAccessMap.entrySet()) {
            Integer key = entry.getKey();
            entry.getValue().setOnClickListener(SettingLayoutActivity.this);
            rbQuickAccessMap.get(key).setOnClickListener(SettingLayoutActivity.this);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.layout_direction_key || id == R.id.rb_direction_key) {
            setNavigation(SettingLayoutEnums.NAVIGATION_DIRECTION.code());
        } else if (id == R.id.layout_tv_app || id == R.id.rb_tv_app) {
            setQuickAccess(SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code());
        } else if (id == R.id.layout_media_button || id == R.id.rb_media_button) {
            setQuickAccess(SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code());
        } else if (id == R.id.layout_none || id == R.id.rb_none) {
            setQuickAccess(SettingLayoutEnums.QUICK_ACCESS_NONE.code());
        }
    }

    @SuppressWarnings("all")
    private void setBGSelect(int code) {
        ConstraintLayout layout = layoutNavMap.get(code);
        if (layout != null) {
            // 导航
            layoutNavMap.get(code).setBackgroundResource(R.drawable.background_4border_selected);
            rbNavMap.get(code).setChecked(true);
        } else {
            // 快速访问
            layoutQuickAccessMap.get(code).setBackgroundResource(R.drawable.background_4border_selected);
            rbQuickAccessMap.get(code).setChecked(true);
        }

    }

    @SuppressWarnings("all")
    private void setNavigation(int code) {
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutNavMap.entrySet()) {
            Integer key = entry.getKey();
            if (key == code) {
                // 选中的
                entry.getValue().setBackgroundResource(R.drawable.background_4border_selected);
                rbNavMap.get(key).setChecked(true);
                SharedData.getInstance().put(Constant.KEY_SETTING_LAYOUT_NAV, code).commit();
            } else {
                entry.getValue().setBackgroundResource(R.drawable.background_4border);
                rbNavMap.get(key).setChecked(false);
            }
        }
    }

    @SuppressWarnings("all")
    private void setQuickAccess(int code) {
        for (Map.Entry<Integer, ConstraintLayout> entry : layoutQuickAccessMap.entrySet()) {
            Integer key = entry.getKey();
            if (key == code) {
                // 选中的
                entry.getValue().setBackgroundResource(R.drawable.background_4border_selected);
                rbQuickAccessMap.get(key).setChecked(true);
                SharedData.getInstance().put(Constant.KEY_SETTING_LAYOUT_QUICK_ACCESS, code).commit();
            } else {
                entry.getValue().setBackgroundResource(R.drawable.background_4border);
                rbQuickAccessMap.get(key).setChecked(false);
            }
        }
    }
}