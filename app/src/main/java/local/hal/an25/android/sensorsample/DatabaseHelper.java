package local.hal.an25.android.sensorsample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * データベースファイル名の定数フィールド
     */
    private static final String DATABASE_NAME = "url.db";

    /**
     *バージョン情報の定数フィールド
     */
    private static final int DATABASE_VERSON = 1;

    /**
     * コンストラクタ。
     *
     * @param context コンテキスト
     */
    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSON);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        StringBuffer urlsSb = new StringBuffer();

        urlsSb.append("CREATE TABLE urls (");
        urlsSb.append("_id INTEGER PRIMARY KEY,");
        urlsSb.append("url TEXT NOT NULL");
        urlsSb.append(");");
        String urlsSql = urlsSb.toString();
        System.out.println(urlsSql + "db作成成功");

        db.execSQL(urlsSql);

        StringBuffer userInfoSb = new StringBuffer();

        userInfoSb.append("CREATE TABLE users (");
        userInfoSb.append("_id INTEGER PRIMARY KEY,");
        userInfoSb.append("user_id INTEGER NOT NULL,");
        userInfoSb.append("name TEXT NOT NULL,");
        userInfoSb.append("gender TEXT NOT NULL");
        userInfoSb.append(");");
        String userInfoSql = userInfoSb.toString();
        System.out.println(userInfoSql + "db作成成功");
        db.execSQL(userInfoSql);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }
}
