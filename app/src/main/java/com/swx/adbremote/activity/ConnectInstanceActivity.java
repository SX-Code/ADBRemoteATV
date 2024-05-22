package com.swx.adbremote.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swx.adbremote.MainApplication;
import com.swx.adbremote.R;
import com.swx.adbremote.adapter.ConnectInstanceAdapter;
import com.swx.adbremote.components.ConnectOperateDialog;
import com.swx.adbremote.components.QuestionDialog;
import com.swx.adbremote.database.DBManager;
import com.swx.adbremote.database.connect.ConnectManager;
import com.swx.adbremote.entity.ConnectInstance;
import com.swx.adbremote.utils.ADBConnectUtil;
import com.swx.adbremote.utils.BeanUtil;
import com.swx.adbremote.utils.MetricsUtil;
import com.swx.adbremote.utils.RecyclerViewItemEqspa;
import com.swx.adbremote.utils.ThreadPoolService;
import com.swx.adbremote.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class ConnectInstanceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SELECTION = 1;
    private static final int STATE_CREATE = 2;
    public static final int STATE_EDIT = 3;
    private static final int WHET_ALL_CHANGE = -1;
    private static final int WHET_INSERT = -2;
    private static final int WHAT_DELETE = -3;
    public static final int WHAT_UPDATE = -4;
    public static final int WHAT_STATE_CHANGE = -5;
    private int mEditMode = STATE_DEFAULT;
    private boolean hasOneConnecting = false;
    private boolean isSelection = false; // 是否为多选状态
    private int index = 0; // 当前选中的item数量
    private boolean isEdit = false;
    private LinearLayout selectTools;
    private ImageButton btnBackTo;
    private TextView textTitle;
    private ImageButton btnEdit;
    private RecyclerView rvConnect;
    private ImageButton btnNewConnect;
    private ConnectInstanceAdapter mDataAdapter;
    private Handler handler;
    private ConnectOperateDialog dialog;
    private QuestionDialog questionDialog;
    ConnectManager connectManager;
    ConnectInstance connectedBean;

    public ConnectInstanceActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_instance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_connect_instance), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }

    private void init() {
        // 获取正在连接中的Bean
        connectedBean = ADBConnectUtil.getConnectedBean();
        rvConnect = findViewById(R.id.rv_connect);
        rvConnect.setLayoutManager(new LinearLayoutManager(this));
        // RecyclerView Item等间距
        rvConnect.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int unit = MetricsUtil.dp2px(4);

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                RecyclerViewItemEqspa.equilibriumAssignmentOfLinear(unit, outRect, view, parent);
            }
        });
        mDataAdapter = new ConnectInstanceAdapter(this);
        rvConnect.setAdapter(mDataAdapter);
        mDataAdapter.setOnItemClickListener(this::handleConnectItemClick);
        mDataAdapter.setOnItemLongClickListener(this::handleConnectItemLongClick);
        // 设置监听
        findViewById(R.id.btn_trash).setOnClickListener(this);
        btnNewConnect = findViewById(R.id.btn_new_connect);
        btnNewConnect.setOnClickListener(this);
        textTitle = findViewById(R.id.text_title);
        btnBackTo = findViewById(R.id.btn_back_to);
        btnBackTo.setOnClickListener(this);
        btnEdit = findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(this);
        // 使用 Handle 在子线程中更新 UI
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == WHET_ALL_CHANGE) {
                    mDataAdapter.setData(BeanUtil.castList(msg.obj, ConnectInstance.class));
                } else if (msg.what == WHET_INSERT) {
                    // 更新RecycleView
                    mDataAdapter.addData((ConnectInstance) msg.obj);
                    // 隐藏Dialog
                    dialog.hide();
                } else if (msg.what == WHAT_DELETE) {
                    // 注意删除中有重新从数据库中拉取的动作，所以这里重新设置
                    mDataAdapter.setData(BeanUtil.castList(msg.obj, ConnectInstance.class));
                    questionDialog.hide();
                } else if (msg.what == WHAT_UPDATE) {
                    mDataAdapter.updateData((ConnectInstance) msg.obj);
                    if (dialog != null) dialog.hide();
                } else if (msg.what == WHAT_STATE_CHANGE) {
                    mDataAdapter.clearData((Integer) msg.obj);
                } else {
                    mDataAdapter.notifyItemChanged((Integer) msg.obj);
                }
            }
        };
        connectManager = DBManager.getInstance().getConnectManager();
        selectTools = findViewById(R.id.layout_bottom_tools);
        // 获取列表数据
        ThreadPoolService.newTask(this::initDataList);
    }

    /**
     * 初始化数据，从SQL lite中查询
     */
    private void initDataList() {
        List<ConnectInstance> data = mDataAdapter.getData();
        if (data.isEmpty()) {
            // 没有数据就从数据库中查询
            data = connectManager.list();
        }
        // 更改正在连接的item状态
        if (connectedBean != null) {
            for (ConnectInstance connect : data) {
                if (connect.equals(connectedBean)) {
                    connect.setActive(true);
                }
            }
        }
        if (!data.isEmpty()) {
            Message message = new Message();
            message.what = WHET_ALL_CHANGE;
            message.obj = data;
            handler.sendMessage(message);
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_back_to) {
            if (isSelection) {
                exitSelect();
            } else {
                // 返回上一个页面
                finish();
            }
        } else if (id == R.id.btn_new_connect) {
            if (isSelection) {
                if (index > 1) {
                    // 选择多个，等同于退出选中模式
                    exitSelect();
                } else {
                    connectSelected(mDataAdapter.getSelectedIndex());
                }
            } else {
                // 添加连接
                showDialog();
            }
        } else if (id == R.id.btn_edit) {
            // 编辑连接
            showDialog();
        } else if (id == R.id.btn_trash) {
            // 删除选中
            showRemoveQuestionDialog();
        }

    }

    /**
     * 退出选择模式，重置Recycler和界面
     */
    private void exitSelect() {
        // 重置Recycler的选中
        mDataAdapter.clearSelection();
        // 防止多选时，直接点击取消，导致长按后不出现编辑按钮
        btnEdit.setVisibility(View.VISIBLE);
        // 重置界面的选中状态
        switchSelection();
        index = 0;
    }

    /**
     * 切换选中状态，主要是本界面的变化
     */
    private void switchSelection() {
        isSelection = !isSelection;
        if (!isSelection) {
            // 显示多选工具箱
            selectTools.setVisibility(View.GONE);
            btnBackTo.setImageResource(R.drawable.ic_arrow_left);
            // 重置添加按钮
            switchRBBtn("default");
            textTitle.setText(R.string.text_select_connect);
        } else {
            // 返回按钮更改为取消选中模式
            btnBackTo.setImageResource(R.drawable.ic_x);
            // 隐藏选择模式工具箱
            selectTools.setVisibility(View.VISIBLE);
            // 添加按钮更改为连接按钮，交给长按其他地方做
            // 更改标题
            textTitle.setText("已选中1个");
        }
    }

    /**
     * 切换右下角按钮的演示
     *
     * @param type 样式代码，这里应该使用枚举
     */
    private void switchRBBtn(String type) {
        switch (type) {
            case "exit": {
                btnNewConnect.setImageResource(R.drawable.ic_x);
                btnNewConnect.setBackgroundResource(R.drawable.background_btn_r16_ps);
                break;
            }
            case "connect": {
                btnNewConnect.setImageResource(R.drawable.ic_link);
                btnNewConnect.setBackgroundResource(R.drawable.background_btn_r16_ps);
                break;
            }
            case "disconnect": {
                btnNewConnect.setImageResource(R.drawable.ic_dis_link);
                btnNewConnect.setBackgroundResource(R.drawable.background_error_btn_r16_ps);
                break;
            }
            default: {
                btnNewConnect.setImageResource(R.drawable.ic_plus);
                btnNewConnect.setBackgroundResource(R.drawable.background_btn_r16_ps);
            }
        }
    }

    private void connectSelected(int position) {
        if (hasOneConnecting) { // 禁止多实例连接
            ToastUtil.showShort("已有实例连接中");
            return;
        }
        hasOneConnecting = true;
        if (connectedBean != null) {
            // 切换连接，先断开连接
            ADBConnectUtil.disconnect();
            mDataAdapter.clearActive();
        }
        ConnectInstance bean = mDataAdapter.getData(position);
        // 断开连接
        if (bean.isActive()) {
            ADBConnectUtil.disconnect();
            bean.setActive(false);
            if (isSelection) {
                // 退出选择模式
                exitSelect();
            } else {
                Message message = new Message();
                message.obj = position;
                handler.sendMessage(message);
            }
            return;
        }
        // 获取连接
        ADBConnectUtil.bean = bean;
        mDataAdapter.connectData(position, true);
        ADBConnectUtil.connection(res -> {
            if (res) {
                // 获取连接成功，返回页面
                finish();
                ToastUtil.showToastThread(MainApplication.getContext().getString(R.string.text_connection_successful));
            } else {
                ToastUtil.showToastThread(MainApplication.getContext().getString(R.string.text_connection_failed));
                hasOneConnecting = false; // 重置状态
                // 发送消息更新RecyclerView
                Message message = new Message();
                message.what = WHAT_STATE_CHANGE;
                message.obj = position;
                handler.sendMessage(message);
                Log.d("ConnectInstanceActivity", "handleConnectItemClick: 获取连接失败，请重试");
            }
        });
    }

    /**
     * RecyclerView Item点击事件处理
     *
     * @param position item下标
     */
    private void handleConnectItemClick(int position) {
        if (isSelection) {
            // 处于编辑状态
            ConnectInstance item = mDataAdapter.getData(position);
            boolean select = item.isSelect();
            if (!select) {
                index++;
                item.setSelect(true);
            } else {
                item.setSelect(false);
                index--;
            }
            if (index == 0) {
                // 未选中，切换到原始状态
                switchSelection();
            } else {
                // 选中若干
                if (index > 1) {
                    // 隐藏编辑按钮
                    btnEdit.setVisibility(View.GONE);
                    // 连接按钮变退出模式
                    switchRBBtn("exit");
                } else { // 只有一个被选中，单并非 item，而是剩余的
                    btnEdit.setVisibility(View.VISIBLE);
                    // 只选中一个时，要么是连接，要么是断开连接
                    switchRBBtn(mDataAdapter.getSelected().isActive() ? "disconnect" : "connect");
                }
                textTitle.setText(String.format("已选中%s个", index));
            }
            mDataAdapter.notifyItemChanged(position);
        } else {
            // 点击状态，连接
            connectSelected(position);
        }
    }

    /**
     * RecyclerView Item长按事件处理
     *
     * @param position item下标
     */
    private void handleConnectItemLongClick(int position) {
        if (isSelection) return;
        // 切换状态
        switchSelection();
        switchRBBtn(mDataAdapter.getData(position).isActive() ? "disconnect" : "connect");
        // 显示多选模式
        mDataAdapter.selectData(position, true);
        index++;
    }

    /**
     * 显示Dialog
     */
    private void showDialog() {
        if (dialog == null) {
            // 初始化弹窗
            dialog = new ConnectOperateDialog(this);
            dialog.setOnButtonClickListener(new ConnectOperateDialog.OnButtonClickListener() {
                @Override
                public void onPositiveClick(Integer id, String alias, String ip, Integer port) {
                    handleConnectOperateDialogConfirm(id, alias, ip, port);
                }

                @Override
                public void onNegativeClick(View view) {
                    dialog.hide();
                }
            });
        }
        if (isSelection) {
            ConnectInstance instance = mDataAdapter.getSelected();
            if (instance == null) return;
            dialog.setData(instance);
        }
        dialog.show();
    }

    /**
     * 处理编辑/创建连接弹窗确认
     */
    private void handleConnectOperateDialogConfirm(Integer id, String alias, String ip, Integer port) {
        ConnectInstance connectInstance = new ConnectInstance(id, alias, ip, port);
        if (id != null && isSelection) {
            ThreadPoolService.newTask(() -> {
                boolean exist = connectManager.isExist(ip, port);
                if (exist) {
                    ToastUtil.showToastThread("已存在");
                    return;
                }
                connectInstance.setId(id);
                // 更新ADBConnectUtil
                ADBConnectUtil.bean = connectInstance;
                connectManager.update(connectInstance);
                // 通知更新
                Message message = new Message();
                message.what = WHAT_UPDATE;
                message.obj = connectInstance;
                handler.sendMessage(message);
            });
        } else {
            // 使用线程更新
            ThreadPoolService.newTask(() -> {
                boolean exist = connectManager.isExist(ip, port);
                if (exist) {
                    ToastUtil.showToastThread("已存在");
                    return;
                }
                Message message = new Message();
                ConnectInstance instance = connectManager.insert(connectInstance);
                message.what = WHET_INSERT;
                message.obj = instance;
                handler.sendMessage(message);
            });
        }
    }

    /**
     * 询问是否删除
     */
    private void showRemoveQuestionDialog() {
        if (questionDialog == null) {
            questionDialog = QuestionDialog.build(this,
                    this.getString(R.string.text_remove_connect),
                    this.getString(R.string.text_remove_connect_info)
            );
            questionDialog.setOnButtonClickListener(new QuestionDialog.OnButtonClickListener() {
                @Override
                public void onPositiveClick(View view) {
                    // 删除
                    handleConnectsRemove();
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
     * 删除所有选中的item
     */
    private void handleConnectsRemove() {
        // index > 0 always true
        switchSelection();
        ThreadPoolService.newTask(() -> {
            // 删除
            ArrayList<Integer> ids = new ArrayList<>();
            boolean containConnected = mDataAdapter.getSelectedIds(ids);
            if (containConnected) {
                // 如果删除了正在连接中的item，则断联
                ADBConnectUtil.disconnect();
            }
            Integer[] res = new Integer[ids.size()];
            res = ids.toArray(res);
            connectManager.delete(res);
            // 重新
            List<ConnectInstance> data = connectManager.list();
            // 试图标记已连接的
            if (connectedBean != null) {
                for (ConnectInstance connect : data) {
                    if (connect.equals(connectedBean)) {
                        connect.setActive(true);
                    }
                }
            }
            Message message = new Message();
            message.what = WHAT_DELETE;
            message.obj = data;
            handler.sendMessage(message);
        });

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isSelection && keyCode == KeyEvent.KEYCODE_BACK) {
            exitSelect();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}