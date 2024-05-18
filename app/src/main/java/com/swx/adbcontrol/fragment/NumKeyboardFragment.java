package com.swx.adbcontrol.fragment;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swx.adbcontrol.R;
import com.swx.adbcontrol.utils.ADBConnectUtil;
import com.swx.adbcontrol.utils.Constant;
import com.swx.adbcontrol.utils.SharedData;
import com.swx.adbcontrol.utils.ToastUtil;

public class NumKeyboardFragment extends Fragment implements View.OnClickListener {

    private boolean isHapticFeedback;

    public NumKeyboardFragment() {
        // Required empty public constructor
    }

    public static NumKeyboardFragment newInstance(String param1, String param2) {
        NumKeyboardFragment fragment = new NumKeyboardFragment();
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
        View root = inflater.inflate(R.layout.fragment_num_keyboard, container, false);
        init(root);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        isHapticFeedback = SharedData.getInstance().
                getBoolean(Constant.KEY_SETTING_BEHAVIOR_HAPTIC_FEEDBACK, true);
    }

    private void init(View view) {
        Resources res = getResources();
        for (int i = 0; i < 10; i++) {
            int id = res.getIdentifier("buttonNum" + i, "id", requireContext().getPackageName());
            view.findViewById(id).setOnClickListener(this);
        }
        view.findViewById(R.id.buttonDel).setOnClickListener(this);
        view.findViewById(R.id.buttonTv).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        hapticFeedback(view);
        int id = view.getId();
        int num = -1;
        if (id == R.id.buttonNum0) {
            num = 0;
        } else if (id == R.id.buttonNum1) {
            num = 1;
        } else if (id == R.id.buttonNum2) {
            num = 2;
        } else if (id == R.id.buttonNum3) {
            num = 3;
        } else if (id == R.id.buttonNum4) {
            num = 4;
        } else if (id == R.id.buttonNum5) {
            num = 5;
        } else if (id == R.id.buttonNum6) {
            num = 6;
        } else if (id == R.id.buttonNum7) {
            num = 7;
        } else if (id == R.id.buttonNum8) {
            num = 8;
        } else if (id == R.id.buttonNum9) {
            num = 9;
        } else if (id == R.id.buttonTv) {
            ADBConnectUtil.pressTv(ADBConnectUtil.callable);
        } else if (id == R.id.buttonDel) {
            ADBConnectUtil.pressDel(ADBConnectUtil.callable);
        }
        if (num != -1) {
            ADBConnectUtil.pressNum(num, ADBConnectUtil.callable);
        }
    }

    private void hapticFeedback(View view) {
        if (isHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }
}