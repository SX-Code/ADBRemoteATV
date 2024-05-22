package com.swx.adbremote.activity.settings;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swx.adbremote.R;
import com.swx.adbremote.adapter.QuickAccessAppsAdapter;
import com.swx.adbremote.components.OnlineUrlDialog;
import com.swx.adbremote.database.DBManager;
import com.swx.adbremote.database.app.AppManager;
import com.swx.adbremote.entity.AppItem;
import com.swx.adbremote.utils.BeanUtil;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.HttpUtil;
import com.swx.adbremote.utils.MetricsUtil;
import com.swx.adbremote.utils.RecyclerViewItemEqspa;
import com.swx.adbremote.utils.SharedData;
import com.swx.adbremote.utils.ThreadPoolService;
import com.swx.adbremote.utils.ToastUtil;

import java.util.ArrayList;

/**
 * @Author sxcode
 * @Date 2024/5/18 13:43
 */
public class SettingOnlineAppsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int WHAT_PARSE = -1;
    private static final int WHAT_INIT = -2;
    private static final int SPAN_COUNT = 3;
    private String configUrl;
    private int priority;
    private QuickAccessAppsAdapter mAppsAdapter;
    private Handler handler;
    private OnlineUrlDialog onlineUrlDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_online_apps);
        priority = getIntent().getIntExtra(Constant.VALUE_ACTIVITY_APP_PRIORITY, 100);
        init();
        initEvent();
        initData();
    }

    private void init() {
        findViewById(R.id.btn_setting_online_apps_back).setOnClickListener(this);
        RecyclerView mOnlineAppsRV = findViewById(R.id.rv_setting_online_applications);
        mOnlineAppsRV.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        mOnlineAppsRV.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int unit = MetricsUtil.dp2px(10);

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                RecyclerViewItemEqspa.equilibriumAssignmentOfGrid(unit, outRect, view, parent);
            }
        });
        mAppsAdapter = new QuickAccessAppsAdapter(LayoutInflater.from(this), -1);
        mOnlineAppsRV.setAdapter(mAppsAdapter);
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                String json = (String) msg.obj;
                if (TextUtils.isEmpty(json)) {
                    ToastUtil.showShort("没获取到");
                    return;
                }
                ArrayList<AppItem> apps = BeanUtil.getAppsByJsonArray(json);
                mAppsAdapter.setData(apps);
                if (msg.what == WHAT_PARSE) {
                    SharedData.getInstance().put(Constant.KEY_QUICK_ACCESS_APP_ONLINE_URL, configUrl);
                    onlineUrlDialog.hide();
                }
            }
        };
    }

    private void initEvent() {
        findViewById(R.id.btn_setting_add_json).setOnClickListener(this);
        // 添加到展示列表
        mAppsAdapter.setOnItemClickListener(this::addToDB);
    }

    private void initData() {
        configUrl = SharedData.getInstance().getString(Constant.KEY_QUICK_ACCESS_APP_ONLINE_URL, Constant.DEFAULT_QUICK_ACCESS_APP_ONLINE_URL);
        if (TextUtils.isEmpty(configUrl)) return;
        // 拉取数据
        fetchData(WHAT_INIT);
    }

    private void fetchData(int what) {
        ThreadPoolService.newTask(() -> {
            String jsonContent = HttpUtil.getStringContent(configUrl);
            Message message = new Message();
            message.what = what;
            message.obj = jsonContent;
            handler.sendMessage(message);
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_setting_online_apps_back) {
            finish();
        } else if (id == R.id.btn_setting_add_json) {
            // 添加在线JSON
            showOnlineUrlDialog();
        }
    }

    /**
     * 添加到首页展示，即到数据库
     */
    private void addToDB(int position, AppItem app) {
        ThreadPoolService.newTask(() -> {
            AppManager appManager = DBManager.getInstance().getAppManager();
            boolean exist = appManager.isExist(app.getUrl());
            if (exist) {
                ToastUtil.showToastThread(app.getName() + " 已被添加");
                return;
            }
            app.setPriority(priority++);
            appManager.insert(app);
            ToastUtil.showToastThread(app.getName() + " 已添加");
        });
    }

    private void showOnlineUrlDialog() {
        if (onlineUrlDialog == null) {
            onlineUrlDialog = new OnlineUrlDialog(this);
            onlineUrlDialog.setOnButtonClickListener(new OnlineUrlDialog.OnButtonClickListener() {
                @Override
                public void onPositiveClick(String url) {
                    handlerOnlineUrlParse(url);
                }

                @Override
                public void onNegativeClick(View view) {
                    onlineUrlDialog.hide();
                }
            });
        }
        if (!TextUtils.isEmpty(configUrl)) {
            onlineUrlDialog.setUrl(configUrl);
        }
        onlineUrlDialog.show();
    }

    private void handlerOnlineUrlParse(String url) {
        if (TextUtils.isEmpty(url)) return;
        configUrl = url;
        fetchData(WHAT_PARSE);
    }

}
