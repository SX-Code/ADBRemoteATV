package com.swx.adbcontrol;

import static com.swx.adbcontrol.utils.ADBConnectUtil.callable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.swx.adbcontrol.activity.ConnectInstanceActivity;
import com.swx.adbcontrol.activity.SettingActivity;
import com.swx.adbcontrol.adapter.QuickAccessAppsAdapter;
import com.swx.adbcontrol.adapter.ViewPager2Adapter;
import com.swx.adbcontrol.components.IndicatorView;
import com.swx.adbcontrol.database.DBManager;
import com.swx.adbcontrol.entity.AppItem;
import com.swx.adbcontrol.entity.ConnectInstance;
import com.swx.adbcontrol.enums.SettingLayoutEnums;
import com.swx.adbcontrol.fragment.NumKeyboardFragment;
import com.swx.adbcontrol.fragment.RoundMenuFragment;
import com.swx.adbcontrol.utils.ADBConnectUtil;
import com.swx.adbcontrol.utils.BeanUtil;
import com.swx.adbcontrol.utils.Constant;
import com.swx.adbcontrol.utils.MetricsUtil;
import com.swx.adbcontrol.utils.RecyclerViewItemEqspa;
import com.swx.adbcontrol.utils.ScheduleUtil;
import com.swx.adbcontrol.utils.SharedData;
import com.swx.adbcontrol.utils.ThreadPoolService;
import com.swx.adbcontrol.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int WHAT_LIST_QUICK_ACCESS = -1;
    private ViewPager2 viewPagerPanel;
    private ImageButton btnSwitchPanel;
    private TextView tvChooseTvConnect;
    private int settingQuickAccess;
    private boolean isHapticFeedback;
    private boolean quickAccessOrderChange;
    private Map<Integer, ViewGroup> layoutQuickAccessMap;
    private ScheduledExecutorService executorService;
    private QuickAccessAppsAdapter mQuickAccessAppsAdapter;
    ScheduledFuture<?> scheduledFuture;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initEvent();
        ToastUtil.init(getApplication());
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSetting();
        ConnectInstance bean = ADBConnectUtil.getConnectedBean();
        if (bean != null) {
            tvChooseTvConnect.setText(bean.getAlias());
        } else {
            tvChooseTvConnect.setText(this.getString(R.string.text_connect_android_tv));
        }
        if (settingQuickAccess == SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code()) {
            // 获取快速访问应用
            getQuickAccessData();
        }
    }

    private void init() {
        btnSwitchPanel = findViewById(R.id.btn_switch_panel);
        executorService = ScheduleUtil.getExecutorService();
        // 设置Fragment容器
        ArrayList<Fragment> list = new ArrayList<>();
        list.add(new RoundMenuFragment());
        list.add(new NumKeyboardFragment());
        // 获取ViewPager2
        viewPagerPanel = findViewById(R.id.viewPagerPanel);
        // 绑定适配器
        viewPagerPanel.setAdapter(new ViewPager2Adapter(this, list));
        IndicatorView indicator = findViewById(R.id.indicator);
        indicator.setIndicatorCount(2);
        viewPagerPanel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // 显示在哪个页面就重绘对应的指示器
                indicator.setCurrentSelectedPosition(position);
                indicator.postInvalidate();
                // 切换图标
                if (position == 1) {
                    btnSwitchPanel.setImageResource(R.drawable.ic_tel_keyboard);
                } else {
                    btnSwitchPanel.setImageResource(R.drawable.ic_round_menu);
                }
            }
        });
        tvChooseTvConnect = findViewById(R.id.tv_choose_connect);
        // 初始化快速访问布局容器
        RecyclerView mRvQuickAccess = findViewById(R.id.layout_quick_access_app);
        mQuickAccessAppsAdapter = new QuickAccessAppsAdapter(LayoutInflater.from(this), LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL); // 水平布局
        mRvQuickAccess.setLayoutManager(layoutManager);
        mRvQuickAccess.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int unit = MetricsUtil.dp2px(4);

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                RecyclerViewItemEqspa.equilibriumAssignmentOfLinear(unit, outRect, view, parent);
            }
        });
        mRvQuickAccess.setAdapter(mQuickAccessAppsAdapter);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == WHAT_LIST_QUICK_ACCESS) {
                    mQuickAccessAppsAdapter.setData(BeanUtil.castList(msg.obj, AppItem.class));
                }
            }
        };

        layoutQuickAccessMap = new HashMap<>();
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code(), (LinearLayout) findViewById(R.id.layout_quick_access_media_button));
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code(), mRvQuickAccess);
        layoutQuickAccessMap.put(SettingLayoutEnums.QUICK_ACCESS_NONE.code(), (LinearLayout) findViewById(R.id.layout_quick_access_none));
    }

    private void initEvent() {
        mQuickAccessAppsAdapter.setOnItemClickListener(this::handleQuickAccessRvItemClick);
        tvChooseTvConnect.setOnClickListener(this);
        // 绑定事件，如何批量?
        findViewById(R.id.btn_mute).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btn_switch_panel).setOnClickListener(this);
        findViewById(R.id.btn_tv_home).setOnClickListener(this);
        findViewById(R.id.btn_tv_back).setOnClickListener(this);
        findViewById(R.id.btn_tv_shutdown).setOnClickListener(this);
        findViewById(R.id.btn_tv_media_play_pause).setOnClickListener(this);
        findViewById(R.id.btn_tv_media_prev).setOnClickListener(this);
        findViewById(R.id.btn_tv_media_next).setOnClickListener(this);
        findViewById(R.id.btn_tv_media_fast_forward).setOnClickListener(this);
        findViewById(R.id.btn_tv_media_rewind).setOnClickListener(this);
        findViewById(R.id.btn_tv_menu).setOnClickListener(this);

        // 音量调节按钮
        ImageView btnTurnUpVolume = findViewById(R.id.btn_turn_up_volume);
        ImageView btnTurnDownVolume = findViewById(R.id.btn_turn_down_volume);
        // 长按事件单独处理
        btnTurnUpVolume.setOnLongClickListener(this::handleVolumeLongClick);
        btnTurnDownVolume.setOnLongClickListener(this::handleVolumeLongClick);
        // 触摸事件
        btnTurnUpVolume.setOnTouchListener(this::handleVolumeKeyEvent);
        btnTurnDownVolume.setOnTouchListener(this::handleVolumeKeyEvent);
        btnTurnUpVolume.setOnClickListener(this);
        btnTurnDownVolume.setOnClickListener(this);
    }

    private void initSetting() {
        settingQuickAccess = SharedData.getInstance().
                getInt(Constant.KEY_SETTING_LAYOUT_QUICK_ACCESS, SettingLayoutEnums.QUICK_ACCESS_APPLICATIONS.code());
        isHapticFeedback = SharedData.getInstance().
                getBoolean(Constant.KEY_SETTING_BEHAVIOR_HAPTIC_FEEDBACK, true);
        quickAccessOrderChange = SharedData.getInstance().getBoolean(Constant.KEY_QUICK_ACCESS_ORDER_CHANGE, false);
        for (Map.Entry<Integer, ViewGroup> entry : layoutQuickAccessMap.entrySet()) {
            Integer key = entry.getKey();
            if (key == settingQuickAccess) {
                entry.getValue().setVisibility(View.VISIBLE);
            } else {
                entry.getValue().setVisibility(View.GONE);
            }
        }
    }

    private void getQuickAccessData() {
        if (mQuickAccessAppsAdapter.getItemCount() > 0 && !quickAccessOrderChange) {
            return;
        }
        SharedData.getInstance().put(Constant.KEY_QUICK_ACCESS_ORDER_CHANGE, false).commit();
        ThreadPoolService.newTask(() -> {
            List<AppItem> apps = DBManager.getInstance().getAppManager().list();
            Collections.sort(apps, (a1, a2) -> a1.getPriority() - a2.getPriority());
            Message message = new Message();
            message.what = WHAT_LIST_QUICK_ACCESS;
            message.obj = apps;
            handler.sendMessage(message);
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        // Resource IDs will be non-final, so, no switch. the performance is the same as switch
        if (id == R.id.tv_choose_connect) {
            // 选中连接
            Intent intent = new Intent(this, ConnectInstanceActivity.class);
            startActivity(intent);
        } else if (id == R.id.btn_setting) {
            // 设置按钮
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.btn_switch_panel) {
            int currentItem = viewPagerPanel.getCurrentItem();
            currentItem = currentItem == 0 ? 1 : 0;
            viewPagerPanel.setCurrentItem(currentItem);
        } else {
            handleTvKeyEvent(id, view);
        }
    }

    public void handleQuickAccessRvItemClick(int position, AppItem app) {
        if (TextUtils.isEmpty(app.getUrl())) return;
        ADBConnectUtil.startApp(app.getUrl(), callable);
    }

    /**
     * 处理触摸事件，松开或者取消时，取消定时任务
     *
     * @param view  视图
     * @param event 事件
     */
    @SuppressWarnings("all")
    private boolean handleVolumeKeyEvent(View view, MotionEvent event) {
        if (event == null) return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        }
        return false;
    }

    /**
     * 处理音量按键长按，这里长按是指一直按
     *
     * @param view 视图
     * @return true，让单击click不执行
     */
    private boolean handleVolumeLongClick(View view) {
        ADBConnectUtil.ShellExecCallable callable = result -> {
            if (!result) {
                ToastUtil.showToastThread("连接失败");
            }
        };
        // 定时任务
        scheduledFuture = executorService.scheduleWithFixedDelay(() -> {
            hapticFeedback(view);
            if (view.getId() == R.id.btn_turn_up_volume) {
                ADBConnectUtil.turnUpVolume(callable);
            } else if (view.getId() == R.id.btn_turn_down_volume) {
                ADBConnectUtil.turnDownVolume(callable);
            }
        }, 200L, 200L, TimeUnit.MILLISECONDS);
        // 300s后开始执行，如果是立即执行，用户启用了震动反馈，就会导致两次震动反馈
        // 这里立即执行一次，但是不主动触发震动反馈
        if (view.getId() == R.id.btn_turn_up_volume) {
            ADBConnectUtil.turnUpVolume(callable);
        } else if (view.getId() == R.id.btn_turn_down_volume) {
            ADBConnectUtil.turnDownVolume(callable);
        }
        return true;
    }

    /**
     * 处理 Android TV相关按键
     *
     * @param id 控件id
     */
    private void handleTvKeyEvent(int id, View view) {
        // 震动反馈
        hapticFeedback(view);
        if (id == R.id.btn_tv_home) {
            ADBConnectUtil.pressHome(callable);
        } else if (id == R.id.btn_tv_back) {
            ADBConnectUtil.pressBack(callable);
        } else if (id == R.id.btn_turn_up_volume) {
            ADBConnectUtil.turnUpVolume(callable);
        } else if (id == R.id.btn_turn_down_volume) {
            ADBConnectUtil.turnDownVolume(callable);
        } else if (id == R.id.btn_tv_shutdown) {
            ADBConnectUtil.pressPower(callable);
        } else if (id == R.id.btn_tv_media_play_pause) {
            ADBConnectUtil.pressMediaPlayPause(callable);
        } else if (id == R.id.btn_tv_media_prev) {
            ADBConnectUtil.pressMediaPrev(callable);
        } else if (id == R.id.btn_tv_media_next) {
            ADBConnectUtil.pressMediaNext(callable);
        } else if (id == R.id.btn_tv_media_fast_forward) {
            ADBConnectUtil.pressMediaFastForward(callable);
        } else if (id == R.id.btn_tv_media_rewind) {
            ADBConnectUtil.pressMediaRewind(callable);
        } else if (id == R.id.btn_tv_menu) {
            ADBConnectUtil.pressMenu(callable);
        } else if (id == R.id.btn_mute) {
            ADBConnectUtil.pressMute(callable);
        }

    }

    private void hapticFeedback(View view) {
        if (isHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }
}