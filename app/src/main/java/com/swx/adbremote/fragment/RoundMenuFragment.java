package com.swx.adbremote.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.dlong.rep.dlroundmenuview.DLRoundMenuView;
import com.swx.adbremote.R;
import com.swx.adbremote.utils.ADBConnectUtil;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.ScheduleUtil;
import com.swx.adbremote.utils.SharedData;
import com.swx.adbremote.utils.ToastUtil;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoundMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoundMenuFragment extends Fragment {
    public static final String TAG = "RoundMenuFragment";
    private boolean isHapticFeedback;
    private DLRoundMenuView dlRmv;
    ScheduledExecutorService executorService = null;
    ScheduledFuture<?> scheduledFuture;

    public RoundMenuFragment() {
        // Required empty public constructor
    }

    public static RoundMenuFragment newInstance(String param1, String param2) {
        RoundMenuFragment fragment = new RoundMenuFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_round_menu, container, false);
        ADBConnectUtil.ShellExecCallable callable = (result, msg) -> {
            if (!result) {
                ToastUtil.showToastThread("连接失败");
            }
        };
        // 获取调度服务
        executorService = ScheduleUtil.getExecutorService();
        dlRmv = root.findViewById(R.id.dl_rmv);
        dlRmv.setOnMenuClickListener(position -> {
            handleRoundMenuClick(root, position, false, callable);
        });
        dlRmv.setOnMenuLongClickListener(position -> {
            handleRoundMenuClick(root, position, true, callable);
        });
        dlRmv.setOnMenuTouchListener((event, p) -> {
            if (event == null) return;
            int action = event.getAction();
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                }
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        isHapticFeedback = SharedData.getInstance().
                getBoolean(Constant.KEY_SETTING_BEHAVIOR_HAPTIC_FEEDBACK, true);
    }

    /**
     * 处理元宵按钮单击/长按事件
     *
     * @param view fragment view
     */
    private void handleRoundMenuClick(View view, int position, boolean longPress, ADBConnectUtil.ShellExecCallable callable) {
        if (longPress) { // 长按
            if (position == -1) {
                // 试图长按OK键
                ToastUtil.showShort("OK键不支持长按");
                eventDispatcher(view, position, callable);
            } else {
                scheduledFuture = executorService.scheduleWithFixedDelay(() -> {
                    eventDispatcher(view, position, callable);
                }, 0, 400L, TimeUnit.MILLISECONDS);
            }
        } else {
            eventDispatcher(view, position, callable);
        }
    }

    /**
     * 按钮事件分发器
     *
     * @param position 按钮位置
     * @param callable 回调
     */
    private void eventDispatcher(View view, int position, ADBConnectUtil.ShellExecCallable callable) {
        // 震动反馈
        hapticFeedback(view);
        switch (position) {
            case 0: {
                // UP
                ADBConnectUtil.pressUp(callable);
                break;
            }
            case 1: {
                // RIGHT
                ADBConnectUtil.pressRight(callable);
                break;
            }
            case 2: {
                // DOWN
                ADBConnectUtil.pressDown(callable);
                break;
            }
            case 3: {
                // LEFT
                ADBConnectUtil.pressLeft(callable);
                break;
            }
            case -1: {
                // CENTER, OK
                ADBConnectUtil.pressOk(callable, false);
                break;
            }
        }
    }

    private void hapticFeedback(View view) {
        if (isHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }
}