package com.swx.adbremote.utils;

import android.util.Log;
import android.view.KeyEvent;

import com.swx.adbremote.MainActivity;
import com.swx.adbremote.MainApplication;
import com.swx.adbremote.R;
import com.swx.adbremote.entity.ConnectInstance;
import com.swx.adbremote.adblib.AdbConnection;
import com.swx.adbremote.adblib.AdbCrypto;
import com.swx.adbremote.adblib.AdbStream;

import org.androidannotations.api.BackgroundExecutor;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class ADBConnectUtil {
    private static final String TAG = "ADBConnectUtil";
    public static AdbConnection adbConnection;
    public static ConnectInstance bean;
    private static AdbCrypto crypto;

    public interface ConnectCallable {
        public void done(boolean result);
    }

    public interface ShellExecCallable {
        public void done(boolean result, String msg);
    }

    public static void disconnect() {
        adbConnection = null;
        bean = null;
    }

    public static ADBConnectUtil.ShellExecCallable callable = (result, msg) -> {
        if (!result) {
            ToastUtil.showToastThread(MainApplication.getContext().getString(R.string.text_connection_failed));
        }
    };

    /**
     * 获取正在连接的实例
     */
    public static ConnectInstance getConnectedBean() {
        if (bean == null || adbConnection == null) {
            return null;
        }
        return bean;
    }

    public static void connection(ConnectCallable callback) {
        BackgroundExecutor.execute(() -> {
            if (adbConnection != null) {
                try {
                    adbConnection.open("shell");
                } catch (IOException | InterruptedException e) {
                    callback.done(false);
                    return;
                }
                callback.done(true);
                return;
            }
            callback.done(generatorConnection());
        });
    }

    private static boolean generatorConnection() {
        Socket socket = null;
        try {
            if (null != bean) {
                socket = new Socket(bean.getIp(), bean.getPort());
            } else {
                Log.d("ADBConnectUtil", "未配置连接参数");
            }
        } catch (IOException e) {
            Log.d("ADBConnectUtil", e.toString());
        }
        if (socket == null) {
            Log.d("ADBConnectUtil", "socket is null");
            return false;
        }
        if (crypto == null) {
            try {
                crypto = AdbCrypto.generateAdbKeyPair(DatatypeConverter::printBase64Binary);
            } catch (NoSuchAlgorithmException e) {
                Log.d("ADBConnectUtil", "AdbCrypto NoSuchAlgorithmException");
                return false;
            }
        }
        try {
            adbConnection = AdbConnection.create(socket, crypto);
        } catch (IOException e) {
            Log.d("ADBConnectUtil", "generatorConnection: adbConnection is null");
            return false;
        }

        try {
            adbConnection.connect();
        } catch (IOException | InterruptedException e) {
            Log.d("ADBConnectUtil", "generatorConnection: 连接失败");
            return false;
        }
        return true;
    }

    private static void execShellCMD(String shellCmd, ShellExecCallable callable) {
        if (adbConnection == null) {
            callable.done(false, null);
            return;
        }
        ThreadPoolService.newTask(() -> {
            String responseText = "";
            AdbStream stream = null;
            try {
                stream = adbConnection.open(shellCmd);
                byte[] response = stream.read();
                responseText = new String(response, StandardCharsets.UTF_8);
                Log.d("ADBConnectUtil: ", responseText);
            } catch (IOException e) {
                callable.done(true, "");
                Log.e(TAG, "execShellCMD: e", e);
            } catch (Exception e) {
                Log.e("ADBConnectUtil", "execShellCMD: cmd=" + shellCmd + "error: " + e);
                // 连接失败，重置连接
                adbConnection = null;
                callable.done(false, null);
            }
            if (stream == null) {
                callable.done(false, null);
            }
            callable.done(true, responseText);
        });
    }

    public static void startApp(String appUrl, ShellExecCallable callable) {
        String shellCmd = String.format("shell:am start %s", appUrl);
        execShellCMD(shellCmd, callable);
    }

    /**
     * 推送事件到Android TV
     *
     * @param keycode  事件代码
     * @param callable 回调方法
     */
    private static void pushKeyEvent(int keycode, ShellExecCallable callable) {
        String shellCmd = String.format("shell:input keyevent %s", keycode);
        execShellCMD(shellCmd, callable);
    }

    /**
     * 文本输入事件
     *
     * @param text 输入文本
     */
    public static void inputText(String text, ShellExecCallable callable) {
        String shellCmd = String.format("shell:input text %s", text);
        execShellCMD(shellCmd, callable);
    }

    public static void getDefaultKeyboard(ShellExecCallable callable) {
        String shellCmd = "shell:settings get secure default_input_method";
        execShellCMD(shellCmd, callable);
    }

    // 切换输入法为Keyboard
    public static void switch2ADBKeyboard(ShellExecCallable callable) {
        switchKeyboard("com.android.adbkeyboard/.AdbIME", callable);
    }

    public static void switchKeyboard(String keyboard, ShellExecCallable callable) {
        String shellCmd = "shell:ime set " + keyboard;
        execShellCMD(shellCmd, callable);
    }

    public static void inputTextWithADBKeyboard(String text, ShellExecCallable callable) {
        String shellCmd = String.format("shell:am broadcast -a ADB_INPUT_TEXT --es msg \"%s\"", text);
        execShellCMD(shellCmd, callable);
    }

    public static void installApk(String apkPath, ShellExecCallable callable) {
        String shellCmd = String.format("shell:pm install %s", apkPath);
        execShellCMD(shellCmd, callable);
    }

    /**
     * 打开指定网页
     * "adb shell am start -a android.intent.action.VIEW -d http://www.stackoverflow.com"
     *
     * @param url      网页地址
     * @param callable 回调函数
     */
    public static void openWebUrl(String url, ShellExecCallable callable) {
        String shellCmd = String.format("shell:am start -a android.intent.action.VIEW -d %s", url);
        execShellCMD(shellCmd, callable);
    }

    /**
     * 奇怪的写法?, 是我懒得改了, haha
     *
     * @param keycode  事件代码
     * @param callable 回调方法
     */
    private static void pushLongPressKeyEvent(int keycode, ShellExecCallable callable) {
        String shellCmd = String.format("shell:input keyevent --longpress %s ", keycode);
        execShellCMD(shellCmd, callable);
    }


    // 按下 上方向键
    public static void pressUp(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_DPAD_UP, callable);
    }

    // 按下 下方向键
    public static void pressDown(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, callable);
    }

    // 按下 左方向键
    public static void pressLeft(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, callable);
    }

    // 按下 右方向键
    public static void pressRight(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, callable);
    }

    // 按下 中间键，即确定键
    public static void pressOk(ShellExecCallable callable, boolean longPress) {
        if (longPress) {
            pushLongPressKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, callable);
        } else {
            pushKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, callable);
        }
    }

    // 按下 返回键
    public static void pressBack(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_BACK, callable);
    }

    // 增大音量
    public static void turnUpVolume(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_VOLUME_UP, callable);
    }

    // 减小音量
    public static void turnDownVolume(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN, callable);
    }

    // 按下 主页键
    public static void pressHome(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_HOME, callable);
    }

    // 按下 菜单键
    public static void pressMenu(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_MENU, callable);
    }

    // 按下 静音键盘
    public static void pressMute(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE, callable);
    }

    // 按下 TV键
    public static void pressTv(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_TV, callable);
    }

    public static void pressDel(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_DEL, callable);
    }


    public static void pressEnter(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_ENTER, callable);
    }


    // Android Tv 电源键
    public static void pressPower(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_TV_POWER, callable);
    }

    // 多媒体键 播放/暂停
    public static void pressMediaPlayPause(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, callable);
    }

    // 多媒体键 快进
    public static void pressMediaFastForward(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, callable);
    }

    // 多媒体键 快退
    public static void pressMediaRewind(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_MEDIA_REWIND, callable);
    }

    // 多媒体键 下一首
    public static void pressMediaNext(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT, callable);
    }

    // 多媒体键 上一首
    public static void pressMediaPrev(ShellExecCallable callable) {
        pushKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, callable);
    }


    /**
     * 按下数字键
     *
     * @param num      数字，从0开始
     * @param callable 回调函数
     */
    public static void pressNum(int num, ShellExecCallable callable) {
        int keycode = KeyEvent.KEYCODE_0 + num;
        pushKeyEvent(keycode, callable);
    }


}
