package com.swx.adbremote;

import static com.swx.adbremote.utils.ADBConnectUtil.callable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

import com.swx.adbremote.activity.ConnectInstanceActivity;
import com.swx.adbremote.activity.SettingActivity;
import com.swx.adbremote.adapter.QuickAccessAppsAdapter;
import com.swx.adbremote.adapter.ViewPager2Adapter;
import com.swx.adbremote.components.InputKeyboardDialog;
import com.swx.adbremote.components.IndicatorView;
import com.swx.adbremote.components.QuestionDialog;
import com.swx.adbremote.database.DBManager;
import com.swx.adbremote.entity.AppItem;
import com.swx.adbremote.entity.ConnectInstance;
import com.swx.adbremote.enums.InputLanguageEnums;
import com.swx.adbremote.enums.SettingLayoutEnums;
import com.swx.adbremote.fragment.NumKeyboardFragment;
import com.swx.adbremote.fragment.RoundMenuFragment;
import com.swx.adbremote.utils.ADBConnectUtil;
import com.swx.adbremote.utils.BeanUtil;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.MetricsUtil;
import com.swx.adbremote.utils.RecyclerViewItemEqspa;
import com.swx.adbremote.utils.ScheduleUtil;
import com.swx.adbremote.utils.SharedData;
import com.swx.adbremote.utils.ThreadPoolService;
import com.swx.adbremote.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Comparator;
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
    InputKeyboardDialog textInputDialog;
    QuestionDialog chineseInputQuestionDialog;
    private String originKeyboard;

    private ADBConnectUtil.ShellExecCallable switchKeyboardCallback;

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
        switchKeyboardCallback = (result, msg) -> {
            if (!result) {
                ToastUtil.showToastThread(MainActivity.this.getString(R.string.text_connection_failed));
                return;
            }
            if (msg.contains("cannot be selected for")) {
                ToastUtil.showToastThread(MainActivity.this.getString(R.string.text_switch_failed));
            } else {
                ToastUtil.showToastThread(MainActivity.this.getString(R.string.text_switch_successful));
            }
        };
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
        findViewById(R.id.btn_text_input).setOnClickListener(this);
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
        btnTurnUpVolume.setOnLongClickListener(this::handleLongClick);
        btnTurnDownVolume.setOnLongClickListener(this::handleLongClick);
        // 触摸事件
        btnTurnUpVolume.setOnTouchListener(this::handleLongpressBtnKeyEvent);
        btnTurnDownVolume.setOnTouchListener(this::handleLongpressBtnKeyEvent);
        btnTurnUpVolume.setOnClickListener(this);
        btnTurnDownVolume.setOnClickListener(this);
    }

    private void initSetting() {
        settingQuickAccess = SharedData.getInstance().
                getInt(Constant.KEY_SETTING_LAYOUT_QUICK_ACCESS, SettingLayoutEnums.QUICK_ACCESS_MEDIA_BUTTONS.code());
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
            apps.sort(Comparator.comparingInt(AppItem::getPriority));
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
        } else if (id == R.id.btn_text_input) {
            showBottomSheetDialog();
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
    private boolean handleLongpressBtnKeyEvent(View view, MotionEvent event) {
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
     * 处理按键长按，这里长按是指一直按
     *
     * @param view 视图
     * @return true，让单击click不执行
     */
    private boolean handleLongClick(View view) {
        // 定时任务
        scheduledFuture = executorService.scheduleWithFixedDelay(() -> {
            hapticFeedback(view);
            if (view.getId() == R.id.btn_turn_up_volume) {
                ADBConnectUtil.turnUpVolume(callable);
            } else if (view.getId() == R.id.btn_turn_down_volume) {
                ADBConnectUtil.turnDownVolume(callable);
            } else if (view.getId() == R.id.btn_input_backspace) {
                ADBConnectUtil.pressDel(callable);
            }
        }, 200L, 200L, TimeUnit.MILLISECONDS);
        // 300s后开始执行，如果是立即执行，用户启用了震动反馈，就会导致两次震动反馈
        // 这里立即执行一次，但是不主动触发震动反馈
        if (view.getId() == R.id.btn_turn_up_volume) {
            ADBConnectUtil.turnUpVolume(callable);
        } else if (view.getId() == R.id.btn_turn_down_volume) {
            ADBConnectUtil.turnDownVolume(callable);
        } else if (view.getId() == R.id.btn_input_backspace) {
            ADBConnectUtil.pressDel(callable);
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

    private void showBottomSheetDialog() {
        if (textInputDialog == null) {
            textInputDialog = new InputKeyboardDialog(this);
            textInputDialog.setOnConfirmClickListener(new InputKeyboardDialog.OnClickListener() {
                @Override
                public void onConfirmClick(View view, String text, int type) {
                    hapticFeedback(view);
                    if (type == InputLanguageEnums.CHINESE.code()) {
                        ADBConnectUtil.inputTextWithADBKeyboard(text, callable);
                    } else {
                        // 将文本输入到屏幕
                        ADBConnectUtil.inputText(text, callable);
                    }
                }

                @Override
                public void onErrorClick() {
                    showChineseInputQuestion();
                }

                @Override
                public void onLanguageToggleClick(int type) {
                    if (type == InputLanguageEnums.CHINESE.code()) {
                        // 尝试获取默认的输入法，方便后续还原
                        ADBConnectUtil.getDefaultKeyboard((result, msg) -> {
                            if (!result) {
                                ToastUtil.showToastThread(MainActivity.this.getString(R.string.text_connection_failed));
                            } else {
                                // 获取成功，尝试切换到ADBKeyboard
                                String currentKeyboard = msg.replace("\n", "");
                                if (Constant.VALE_ADB_KEYBOARD_URL.equals(currentKeyboard)) {
                                    ToastUtil.showToastThread("已是ADBKeyboard");
                                    return;
                                }
                                ;
                                originKeyboard = currentKeyboard;
                                ADBConnectUtil.switch2ADBKeyboard(switchKeyboardCallback);
                            }
                        });

                    } else {
                        if (TextUtils.isEmpty(originKeyboard)) return;
                        ADBConnectUtil.switchKeyboard(originKeyboard, switchKeyboardCallback);
                    }
                    SharedData.getInstance().put(Constant.KEY_INPUT_LANGUAGE, type).commit();
                }

                @Override
                public void onEnterClick(View view) {
                    hapticFeedback(view);
                    ADBConnectUtil.pressEnter(callable);
                }

                @Override
                public void onBackspaceClick(View view) {
                    hapticFeedback(view);
                    ADBConnectUtil.pressDel(callable);
                }

                @Override
                public boolean onBackspaceLongClick(View view) {
                    // 这里直接调用了MainActivity中长按事件处理方法，并在其中添加了Dialog中组件的判断，感觉不太妥当
                    return handleLongClick(view);
                }

                @Override
                public boolean onBackspaceTouch(View view, MotionEvent event) {
                    return handleLongpressBtnKeyEvent(view, event);
                }
            });
        }
        textInputDialog.show();
    }

    private void showChineseInputQuestion() {
        // 为了防止和底部弹出Dialog的顺序出现问题，这里每次都采用新建方式
        chineseInputQuestionDialog = new QuestionDialog(this,
                this.getString(R.string.text_input_chinese),
                this.getString(R.string.text_input_chinese_tip));
        chineseInputQuestionDialog.setPositiveBtnText(this.getString(R.string.text_download));
        chineseInputQuestionDialog.setOnButtonClickListener(new QuestionDialog.OnButtonClickListener() {
            @Override
            public void onPositiveClick(View view) {
                // 在Android TV上打开 ADBKeyBoard 下载网址
                ADBConnectUtil.openWebUrl(Constant.URL_ADB_KEYBOARD_DOWNLOAD, callable);
                chineseInputQuestionDialog.dismiss();
            }

            @Override
            public void onNegativeClick(View view) {
                chineseInputQuestionDialog.dismiss();
            }
        });
        chineseInputQuestionDialog.show();
    }
}