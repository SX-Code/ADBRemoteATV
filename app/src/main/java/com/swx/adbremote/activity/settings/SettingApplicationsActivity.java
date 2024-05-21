package com.swx.adbremote.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swx.adbremote.R;
import com.swx.adbremote.adapter.SettingApplicationsAdapter;
import com.swx.adbremote.components.AppOperateDialog;
import com.swx.adbremote.components.QuestionDialog;
import com.swx.adbremote.database.DBManager;
import com.swx.adbremote.entity.AppItem;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.BeanUtil;
import com.swx.adbremote.utils.MetricsUtil;
import com.swx.adbremote.utils.RecyclerViewItemEqspa;
import com.swx.adbremote.utils.SharedData;
import com.swx.adbremote.utils.ThreadPoolService;

import java.util.Collections;
import java.util.List;

/**
 * <a href="https://www.cnblogs.com/wjtaigwh/p/6543354.html">Android -- 实现RecyclerView可拖拽Item</a>
 */
public class SettingApplicationsActivity extends AppCompatActivity implements View.OnClickListener {
    private SettingApplicationsAdapter mRvAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private QuestionDialog questionDialog;
    private AppOperateDialog operateDialog;

    public static final int WHAT_DELETE = -1;
    public static final int WHAT_ADD = -2;
    public static final int WHAT_LIST = -3;
    public static final int WHAT_UPDATE = -4;
    private int selectPosition = 0;
    private Integer selectAppId;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_applications);
        init();
        initEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    private void init() {
        RecyclerView mRv = findViewById(R.id.rv_setting_application);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRvAdapter = new SettingApplicationsAdapter(this);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mRv.setAdapter(mRvAdapter);
        mItemTouchHelper.attachToRecyclerView(mRv);

        mRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int unit = MetricsUtil.dp2px(4);

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                RecyclerViewItemEqspa.equilibriumAssignmentOfLinear(unit, outRect, view, parent);
            }
        });

        handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == WHAT_LIST) {
                    List<AppItem> appItems = BeanUtil.castList(msg.obj, AppItem.class);
                    mRvAdapter.setData(appItems);
                } else if (msg.what == WHAT_ADD) {
                    mRvAdapter.addData((AppItem) msg.obj);
                    operateDialog.hide();
                } else if (msg.what == WHAT_DELETE) {
                    mRvAdapter.remove(selectPosition);
                    questionDialog.hide();
                } else if (msg.what == WHAT_UPDATE) {
                    mRvAdapter.updateData(msg.arg1, (AppItem) msg.obj);
                    operateDialog.hide();
                }
            }
        };
    }

    private void initEvent() {
        findViewById(R.id.btn_setting_app_back).setOnClickListener(this);
        findViewById(R.id.btn_setting_add_app).setOnClickListener(this);
        findViewById(R.id.btn_setting_add_online).setOnClickListener(this);
        mRvAdapter.setOnItemLongClickListener(vh -> mItemTouchHelper.startDrag(vh));
        mRvAdapter.setOnItemClickListener(new SettingApplicationsAdapter.OnItemClickListener() {
            @Override
            public void onRemoveClick(int position, Integer id) {
                selectAppId = id;
                selectPosition = position;
                showQuestionDialog();
            }

            @Override
            public void onEditClick(int position, AppItem app) {
                selectPosition = position;
                showOperateDialog(app);
            }
        });
    }

    private void initData() {
        ThreadPoolService.newTask(() -> {
            List<AppItem> apps = DBManager.getInstance().getAppManager().list();
            if (!apps.isEmpty()) {
                Collections.sort(apps, (a1, a2) -> a1.getPriority() - a2.getPriority());
                Message message = new Message();
                message.obj = apps;
                message.what = WHAT_LIST;
                handler.sendMessage(message);
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_setting_app_back) {
            finish();
        } else if (id == R.id.btn_setting_add_app) {
            showOperateDialog(null);
        } else if (id == R.id.btn_setting_add_online) {
            // 添加在线JSON
            Intent intent = new Intent(this, SettingOnlineAppsActivity.class);
            intent.putExtra(Constant.VALUE_ACTIVITY_APP_PRIORITY, mRvAdapter.getItemCount());
            startActivity(intent);
        }
    }


    private void showOperateDialog(AppItem app) {
        if (operateDialog == null) {
            operateDialog = new AppOperateDialog(this);
            operateDialog.setOnButtonClickListener(new AppOperateDialog.OnButtonClickListener() {
                @Override
                public void onPositiveClick(Integer id, String name, String icon, String url) {
                    AppItem appItem = new AppItem(id, icon, name, url);
                    handleOperateDialogConfirm(appItem);
                }

                @Override
                public void onNegativeClick(View view) {
                    operateDialog.hide();
                }
            });
        }
        if (app != null) {
            // 更新操作
            operateDialog.setData(app);
        }
        operateDialog.show();
    }

    private void handleOperateDialogConfirm(AppItem appItem) {
        ThreadPoolService.newTask(() -> {
            Message message = new Message();
            if (appItem.getId() == null) {
                // 添加
                message.obj = DBManager.getInstance().getAppManager().insert(appItem);
                message.what = WHAT_ADD;
            } else {
                // 更新
                DBManager.getInstance().getAppManager().update(appItem);
                message.obj = appItem;
                message.what = WHAT_UPDATE;
                message.arg1 = selectPosition;
            }
            handler.sendMessage(message);
        });
    }

    private void showQuestionDialog() {
        if (questionDialog == null) {
            questionDialog = QuestionDialog.build(this, "移除应用程序", "确定要删除此应用程序?");
            questionDialog.setOnButtonClickListener(new QuestionDialog.OnButtonClickListener() {
                @Override
                public void onPositiveClick(View view) {
                    handleQuestionDialogConfirm();
                }

                @Override
                public void onNegativeClick(View view) {
                    questionDialog.hide();
                }
            });
        }
        questionDialog.show();
    }

    /**
     * 处理弹窗点击事件，删除APP
     */
    private void handleQuestionDialogConfirm() {
        ThreadPoolService.newTask(() -> {
            DBManager.getInstance().getAppManager().delete(selectAppId);
            // 不能重新拉取，注意顺序
            Message message = new Message();
            message.what = WHAT_DELETE;
            message.arg1 = selectPosition;
            handler.sendMessage(message);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 离开页面，就需要保存最新顺序
        saveAppOrder();
        SharedData.getInstance().put(Constant.KEY_QUICK_ACCESS_ORDER_CHANGE, true).commit();
    }

    private void saveAppOrder() {
        ThreadPoolService.newTask(() -> {
            List<AppItem> appItems = mRvAdapter.setOrder();
            DBManager.getInstance().getAppManager().batchUpdateOrder(appItems);
        });
    }

    ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            //得到当拖拽的viewHolder的Position
            int fromPosition = viewHolder.getAdapterPosition();
            //拿到当前拖拽到的item的viewHolder
            int toPosition = target.getAdapterPosition();
            mRvAdapter.swap(fromPosition, toPosition);
            mRvAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            if (viewHolder != null && actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                viewHolder.itemView.setBackgroundResource(R.color.btn_press_bg_color);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setBackgroundResource(R.color.btn_bg_color);
        }
    };
}