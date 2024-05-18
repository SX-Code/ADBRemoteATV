package com.swx.adbcontrol.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.swx.adbcontrol.database.app.AppContact;
import com.swx.adbcontrol.database.connect.ConnectContact;

/**
 * @Author sxcode
 * @Date 2024/5/15 21:29
 * no room, no orm，the most primitive way
 * <a href="https://zhuanlan.zhihu.com/p/29472803">...</a>
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "adb_control.db";
    private static int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 用户首次安装应用程序，会调用该方法
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ConnectContact.CREATE_TABLE);
        db.execSQL(AppContact.CREATE_TABLE);
    }

    /**
     * 版本号发生变化，会调用该方法
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //通过版本号来决定如何升级数据库
    }
}
