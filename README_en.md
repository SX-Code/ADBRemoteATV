[English](README_en.md) / [中文](README.md)

# ADB Remote ATV

Remote control for Android TV, based on ADB Shell commands

ADB Remote ATV is an Android TV remote control, based on the [ADB Shell](https://github.com/cgutman/AdbLib) command, which is more versatile.

The following shell command is the basic principle of the software. Through the shell command, you can simulate the basic buttons of the physical remote control. In addition, you can quickly launch the designated APP, input Chinese/English characters with the help of the mobile phone's soft keyboard, etc.

```shell
# input event
adb shell input text <string>   # Input characters into the device
adb shell input keyevent <key_code>   # Input key events to the device
# Start app
adb shell am start <package/activity>	# Launch app on your device
shell ime set <app>	# Switch input method
shell settings get secure default_input_method	# Get the current input method
```



## Application function

### Buttons

The corresponding shell commands are as follows:

```shell
adb shell input keyevent <key_code>   # Input key events to the device
```

The keycode is as follows:

| Function           | Constant                   | keycode |
|--------------------|----------------------------|---------|
| BACK               | KEYCODE_BACK               | 4       |
| Home               | KEYCODE_HOME               | 3       |
| Menu               | KEYCODE_MENU               | 82      |
| Volume mute        | KEYCODE_VOLUME_MUTE        | 164     |
| Volume up          | KEYCODE_VOLUME_UP          | 24      |
| Volume down        | KEYCODE_VOLUME_DOWN        | 25      |
| Dpad up            | KEYCODE_DPAD_UP            | 19      |
| Dpad down          | KEYCODE_DPAD_DOWN          | 20      |
| Dpad left          | KEYCODE_DPAD_LEFT          | 21      |
| Dpad right         | KEYCODE_DPAD_RIGHT         | 22      |
| Dpad center        | KEYCODE_DPAD_CENTER        | 23      |
| Numbers            | KEYCODE_0 - KEYCODE_9      | 7 - 16  |
| Backspace          | KEYCODE_DEL                | 67      |
| TV                 | KEYCODE_TV                 | 170     |
| Power              | KEYCODE_TV_POWER           | 177     |
| Media Play/Pause   | KEYCODE_MEDIA_PLAY_PAUSE   | 85      |
| Media fast forward | KEYCODE_MEDIA_FAST_FORWARD | 95      |
| Media rewind       | KEYCODE_MEDIA_REWIND       | 89      |
| Media previous     | KEYCODE_MEDIA_PREVIOUS     | 88      |
| media next         | KEYCODE_MEDIA_NEXT         | 87      |



### Character input

Supports Chinese/English characters, input characters into Android TV with the help of mobile phone soft keyboard. Note that Chinese characters require [ADBKeyboard](https://github.com/senzhk/ADBKeyBoard?tab=readme-ov-file) support.

The shell command for English characters is as follows:

```shell
shell input text <string>  # Input characters into the device
```

The shell command for Chinese characters is as follows, which requires the support of [ADBKeyboard](https://github.com/senzhk/ADBKeyBoard):

```shell
shell am start -a android.intent.action.VIEW -d <string>
```

The command to switch the input method is as follows:

```shell
shell ime set <app>

# 例如
shell ime set com.android.adbkeyboard/.AdbIME  # Switch to ADBKeyboard
```

Get the current input method:

```shell
shell settings get secure default_input_method
```



### Quick access

The shell command for this is as follows:

```shell
shell am start <package/activity>  # Input key events to the device

# 例如
shell am start com.github.tvbox.osc/.ui.activity.HomeActivity	# Start TVBox
```

Quick start software can be added from the software warehouse. The warehouse is resolved from the online address. The json file is `apps.json` in the project root directory. For example

```json
[
    {
        "name": "TVBox",
        "icon": "https://raw.githubusercontent.com/SX-Code/ADBRemoteATV/main/icons/tvbox.png",
        "url": "com.github.tvbox.osc/.ui.activity.HomeActivity"
    },
    {
        "name": "TVBox UI美化版",
        "icon": "https://raw.githubusercontent.com/SX-Code/ADBRemoteATV/main/icons/tvbox.png",
        "url": "com.github.tvbox.osc.tk/com.github.tvbox.osc.ui.activity.HomeActivity"
    },
]
```

In：

- `name`：is the name of the software
- `icon`：is the icon of the software for ease of display.
- `url`：is the startup path of the software. Format reference `com.github.tvbox.osc/.ui.activity.HomeActivity`



**How to get the startup path of an APP** can be obtained from the AndroidManifest.xml file of the software. The following is a sample file,

- Find the `package` attribute from the `manifest` tag, which is the package of the startup path.
- Find the `android:name` attribute from the `activity` with `LAUNCHER`, which is the activity of the startup path
- The combination of `package/activity` is the path parameter of the startup command.

```xml
<?xml version="1.0" encoding="utf-8" standalone="no"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" 
          android:compileSdkVersion="33" 
          android:compileSdkVersionCodename="13" 
          package="tech.simha.androidtvremote" 
          platformBuildVersionCode="33" platformBuildVersionName="13">
    
    <application 
         android:appComponentFactory="androidx.core.app.CoreComponentFactory" 
         android:hardwareAccelerated="true" 
         android:icon="@mipmap/launcher_icon" 
         android:label="Remote ATV" 
         android:name="android.app.Application">
        
        <activity 
          android:exported="true" 
          android:hardwareAccelerated="true" 
          android:launchMode="singleTop" 
          android:name="tech.simha.androidtvremote.MainActivity" 
          android:screenOrientation="portrait" 
          android:theme="@style/LaunchTheme" 
          android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```





## Application UI

![](https://raw.githubusercontent.com/SX-Code/ADBRemoteATV/main/icons/appscreen.jpg)



## Acknowledgments

**ADBlib**：https://github.com/cgutman/AdbLib

**ADBKeyboard**：https://github.com/senzhk/ADBKeyBoard?tab=readme-ov-file

**Round menu button**：https://github.com/D10NGYANG/DL10RoundMenuView

**Digital progress bar**：https://github.com/daimajia/NumberProgressBar
