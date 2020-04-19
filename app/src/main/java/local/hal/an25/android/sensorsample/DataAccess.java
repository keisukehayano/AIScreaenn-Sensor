package local.hal.an25.android.sensorsample;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;


public class DataAccess {



    /**
     * 主キーによる検索
     *
     * @param db　　SQLiteDatabaseオブジェクト
     * @param id　主キー値
     * @return  主キーに対応するurl。対応するデータない場合はnull
     */
    public static String findByPK(SQLiteDatabase db, long id){
        String sql = "SELECT _id,url FROM urls WHERE _id = " + id;

        Cursor cursor = db.rawQuery(sql,null);
        String result = "";
        if(cursor.moveToFirst()){
            int idxUrl = cursor.getColumnIndex("url");

            String url = cursor.getString(idxUrl) ;
            result = url;

        }
        return result;
    }

    /**
     * 主キーによる検索
     *
     * @param db　　SQLiteDatabaseオブジェクト
     * @param id　主キー値
     * @return  主キーが存在するかの判断。対応するデータない場合は0
     */
    public static Integer findByPKPK(SQLiteDatabase db, long id){
        String sql = "SELECT _id,url FROM urls WHERE _id = " + id;

        Cursor cursor = db.rawQuery(sql,null);
        int result = 0;
        if(cursor.moveToFirst()){
            int idxId = cursor.getColumnIndex("_id");

            int intId = cursor.getInt(idxId);
            result = intId;

        }
        return result;
    }


    /**
     *情報を更新するメソッド
     *
     * @param db　SQLiteDatabaseオブジェクト
     * @param _id  主キー
     * @param url 　タイトル
     * @return  更新件数
     */
    public static int update(SQLiteDatabase db,long _id, String url){
        String sql = "UPDATE urls SET url = ? WHERE _id = ?";

        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1,url);
        stmt.bindLong(2,_id);

        int result = stmt.executeUpdateDelete();

        return result;
    }

    /**
     * 情報を新規登録するメソッド
     *
     * @param db        SQLiteDatabaseオブジェクト
     * @param url　url
     * @return   登録したメモの主キー
     */
    public static long insert(SQLiteDatabase db,long _id, String url){
        String sql = "INSERT INTO urls (_id,url)VALUES(?,?)";

        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1,_id);
        stmt.bindString(2,url);


        long id = stmt.executeInsert();
        return id;
    }



    public static Integer findByUserPk(SQLiteDatabase db,long _id) {
        String sql = "SELECT _id FROM users WHERE _id = " + _id;
        int result = 0;
        Cursor cursor = db.rawQuery(sql,null);
        if(cursor.moveToFirst()){
            int idxId = cursor.getColumnIndex("_id");

            int intId = cursor.getInt(idxId);
            result = intId;
        }

        return result;
    }

    public static void userUpdate(SQLiteDatabase db,long _id,long user_id,String userName,String gender) {
        String sql = "UPDATE users SET user_id = ?,name = ?,gender = ? WHERE _id = ?";

        SQLiteStatement stmt = db.compileStatement(sql);

        stmt.bindLong(1,user_id);
        stmt.bindString(2,userName);
        stmt.bindString(3,gender);
        stmt.bindLong(4,_id);

        stmt.executeUpdateDelete();

    }

    public static void userInsert(SQLiteDatabase db,long _id,long user_id,String userName,String gender) {
        String sql = "INSERT INTO users (_id,user_id,name,gender)VALUES(?,?,?,?)";

        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1,_id);
        stmt.bindLong(2,user_id);
        stmt.bindString(3,userName);
        stmt.bindString(4,gender);

        stmt.executeInsert();

    }


    public static UserInfo getUserInfo(SQLiteDatabase db,long _id) {
        UserInfo result = null;
        String sql = "SELECT * FROM users WHERE _id = " + _id;

        Cursor cursor = db.rawQuery(sql,null);
        if(cursor.moveToFirst()){
            int idxId = cursor.getColumnIndex("_id");
            long longId = cursor.getLong(idxId);
            int idxUserId = cursor.getColumnIndex("user_id");
            int intUserId = cursor.getInt(idxUserId);
            int idxName = cursor.getColumnIndex("name");
            String strName = cursor.getString(idxName);
            int idxGender = cursor.getColumnIndex("gender");
            String strGender = cursor.getString(idxGender);

            result = new UserInfo();

            result.set_id(longId);
            result.set_userId(intUserId);
            result.set_name(strName);
            result.set_gender(strGender);
        }


        return result;
    }



}
