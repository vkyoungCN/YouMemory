package com.vkyoungcn.learningtools.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.vkyoungcn.learningtools.models.*;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class YouMemoryDbHelper extends SQLiteOpenHelper {
    //如果修改了数据库结构方案，则应当改动（增加）版本号
    private static final String TAG = "YouMemory-DbHelper";
    private static final int DATEBASE_VERSION = 1;
    private static final String DATEBASE_NAME = "YouMemory.db";
    private volatile static YouMemoryDbHelper sYouMemoryDbHelper = null;
    private SQLiteDatabase mSQLiteDatabase = null;

    public static final String DEFAULT_ITEM_SUFFIX = "default13531";

    /*建表语句的构造*/

    /* Mission、Group、MissionXGroup在程序初次运行时即创建；
     * 各任务特有的Items、GroupXItems表在添加具体任务时创建；
     * 因为各任务的Item表名后缀不同*/
    public static final String SQL_CREATE_MISSION =
            "CREATE TABLE " + YouMemoryContract.Mission.TABLE_NAME + " (" +
                    YouMemoryContract.Mission._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    YouMemoryContract.Mission.COLUMN_NAME + " TEXT, "+
                    YouMemoryContract.Mission.COLUMN_TABLE_ITEM_SUFFIX + " TEXT, "+
                    YouMemoryContract.Mission.COLUMN_DESCRIPTION + " TEXT)";

    public static final String SQL_CREATE_GROUP =
            "CREATE TABLE " + YouMemoryContract.Group.TABLE_NAME + " (" +
                    YouMemoryContract.Group._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    YouMemoryContract.Group.COLUMN_DESCRIPTION + " TEXT, "+
                    YouMemoryContract.Group.COLUMN_INIT_PICK_TIME + " DATE, "+
                    YouMemoryContract.Group.COLUMN_LAST_PICK_TIME + " DATE, "+
                    YouMemoryContract.Group.COLUMN_TIME_PICKED + " INTEGER, "+
                    YouMemoryContract.Group.COLUMN_SPECIAL_MARK + " INTEGER)";

    public static final String SQL_CREATE_MISSION_X_GROUP =
            "CREATE TABLE " + YouMemoryContract.MissionCrossGroup.TABLE_NAME + " (" +
                    YouMemoryContract.MissionCrossGroup._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    YouMemoryContract.MissionCrossGroup.COLUMN_MISSION_ID + " INTEGER, "+
                    YouMemoryContract.MissionCrossGroup.COLUMN_GROUP_ID + " INTEGER)";


    /*以下两种表需要根据具体的任务id创建，需动态生成建表语句*/
    /* 根据Mission_id创建具体的任务项目表，所需语句*/
    public String getSqlCreateItemWithSuffix(String suffix){
         return "CREATE TABLE " +
                YouMemoryContract.ItemBasic.TABLE_NAME + suffix+" (" +
                YouMemoryContract.ItemBasic._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                YouMemoryContract.ItemBasic.COLUMN_NAME + " TEXT, " +
                YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST + " TEXT, " +
                YouMemoryContract.ItemBasic.COLUMN_PICKING_TIME_LIST + " TEXT)";
    }

    /* 根据Mission_id创建具体的项目-分组交叉表，所需语句*/
    public String getSqlCreateGroupXItemWithSuffix(String suffix){
        return "CREATE TABLE " +
                YouMemoryContract.GroupCrossItem.TABLE_NAME + suffix+" (" +
                YouMemoryContract.GroupCrossItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                YouMemoryContract.GroupCrossItem.COLUMN_GROUP_ID + " INTEGER, " +
                YouMemoryContract.GroupCrossItem.COLUMN_ITEM_ID + " INTEGER)";
    }

    private static final String SQL_DROP_MISSION =
            "DROP TABLE IF EXISTS " +  YouMemoryContract.Mission.TABLE_NAME;
    private static final String SQL_DROP_GROUP =
            "DROP TABLE IF EXISTS " + YouMemoryContract.Group.TABLE_NAME;
    private static final String SQL_DROP_MISSION_X_GROUP =
            "DROP TABLE IF EXISTS " + YouMemoryContract.MissionCrossGroup.TABLE_NAME;

    /*以下两种表的删除语句动态生成*/
    public String getSqlDropItemWithMissionId(int mission_id){
        return "DROP TABLE IF EXISTS " +  YouMemoryContract.ItemBasic.TABLE_NAME + mission_id;
    }

    public String getSqlDropGroupXItemWithMissionId(int mission_id){
        return "DROP TABLE IF EXISTS " +  YouMemoryContract.GroupCrossItem.TABLE_NAME + mission_id;
    }

    public YouMemoryDbHelper(Context context) {
        super(context, DATEBASE_NAME, null, DATEBASE_VERSION);
        //Log.i(TAG,"inside YouMemoryDbHelper Constructor, after the super ");

        getWritableDatabaseIfClosedOrNull();
        //Log.i(TAG,"inside YouMemoryDbHelper Constructor, got the Wdb: "+mSQLiteDatabase.toString());
    }

    //DCL模式单例，因为静态内部类模式不支持传参
    public static YouMemoryDbHelper getInstance(Context context){
        //Log.i(TAG,"inside YouMemoryDbHelper getInstance, before any calls");
        if(sYouMemoryDbHelper == null){
            synchronized (YouMemoryDbHelper.class){
                if(sYouMemoryDbHelper == null){
                    sYouMemoryDbHelper = new YouMemoryDbHelper(context);
                }
            }
        }
        return sYouMemoryDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG,"inside YouMemoryDbHelper onCreate");
        db.execSQL(SQL_CREATE_MISSION);
        Log.i(TAG,"inside YouMemoryDbHelper.onCreate(),behind 1st CREATE");
        db.execSQL(SQL_CREATE_GROUP);
        //Log.i(TAG,"inside YouMemoryDbHelper.onCreate(),behind 2nd CREATE");
        db.execSQL(SQL_CREATE_MISSION_X_GROUP);
        //Log.i(TAG,"inside YouMemoryDbHelper.onCreate(),behind 3rd CREATE");

        //其余两个（默认）表在以下方法中建立，随后在该方法中导入相应数据。
        dataInitialization(db);
    }

    /*
    * 在本方法中，建立Item_default13531表、GroupXItem_default13531表；
    * 为Mission表增添记录：EnglishWords13531、螺旋重复式背单词，共13531词，初级简单词汇已剔除、
    * _default13531；
    * 为Item_default13531表添加全部记录。
    * */
    private void dataInitialization(SQLiteDatabase db){
        Log.i(TAG, "dataInitialization: gotW");
        db.execSQL(getSqlCreateItemWithSuffix(DEFAULT_ITEM_SUFFIX));
        Log.i(TAG,"inside YouMemoryDbHelper,CREAT ITEM_DEFAULT");

        db.execSQL(getSqlCreateGroupXItemWithSuffix(DEFAULT_ITEM_SUFFIX));

        //向Mission表增加默认记录

        Mission defaultMission  = new Mission("EnglishWords13531","螺旋式背单词",DEFAULT_ITEM_SUFFIX);

        Log.i(TAG, "dataInitialization: ready to insert default_mission,mission= "+defaultMission);
        createMission(db,defaultMission);


//      Item_default13531表数据导入暂略。




    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // 使用for实现跨版本升级数据库
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {

                default:
                    break;
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        for (int i = oldVersion; i > newVersion; i--) {
            switch (i) {

                default:
                    break;
            }
        }

    }

    /*CRUD部分需要时再写*/

    public long createMission(Mission mission){
        long l;
//        Log.i(TAG, "creatMission: before any.");

        getWritableDatabaseIfClosedOrNull();
        ContentValues values = new ContentValues();

        values.put(YouMemoryContract.Mission.COLUMN_NAME, mission.getName());
        values.put(YouMemoryContract.Mission.COLUMN_DESCRIPTION, mission.getDescription());
        values.put(YouMemoryContract.Mission.COLUMN_TABLE_ITEM_SUFFIX, mission.getTableItem_suffix());

        l = mSQLiteDatabase.insert(YouMemoryContract.Mission.TABLE_NAME, null, values);
        closeDB();

        return l;
    }


    /*
    * 此重载版本用于在数据库的onCreate()方法中使用，以免在其中调用getR/W Db而致递归调用的错误。
    * */
    public  long createMission(SQLiteDatabase db,Mission mission){
        long l;
        Log.i(TAG, "createMission +db, mission= "+mission);

        ContentValues values = new ContentValues();

        values.put(YouMemoryContract.Mission.COLUMN_NAME, mission.getName());
        values.put(YouMemoryContract.Mission.COLUMN_DESCRIPTION, mission.getDescription());
        values.put(YouMemoryContract.Mission.COLUMN_TABLE_ITEM_SUFFIX, mission.getTableItem_suffix());

        l = db.insert(YouMemoryContract.Mission.TABLE_NAME, null, values);
        closeDB();

        return l;
    }

    public List<Mission> getAllMissions(){
        List<Mission> missions = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.Mission.TABLE_NAME;

        Log.i(TAG, "getAllMissions: before any.");
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                Mission mission = new Mission();
                mission.setDb_id(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Mission._ID)));
                mission.setName(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_NAME)));
                mission.setDescription(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_DESCRIPTION)));
                mission.setTableItem_suffix(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_TABLE_ITEM_SUFFIX)));

                missions.add(mission);
            }while (cursor.moveToNext());
        }
        Log.i(TAG, "getAllMissions: after cursor iterate");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return missions;

    }

    public List<String> getAllMissionTitles(){
        List<String> missionTitles = new ArrayList<>();
        String selectQuery = "SELECT "+ YouMemoryContract.Mission.COLUMN_NAME
                +" FROM "+ YouMemoryContract.Mission.TABLE_NAME;

        Log.i(TAG, "getAllMissionTitles: before any.");
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                missionTitles.add(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_NAME)));
            }while (cursor.moveToNext());
        }
        Log.i(TAG, "getAllMissionTitles: after cursor iterate");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return missionTitles;

    }

    private void getWritableDatabaseIfClosedOrNull(){
        Log.i(TAG, "getWritableDatabaseIfClosedOrNull: before any");
        if(mSQLiteDatabase==null || !mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase = this.getWritableDatabase();
            Log.i(TAG,"inside GetWDbIfClosedOrNull(),so Got the W-DB");
        }/*else if (mSQLiteDatabase.isReadOnly()){
            //只读的不行，先关，再开成可写的。
            try{
                mSQLiteDatabase.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
            mSQLiteDatabase = this.getWritableDatabase();
        }*/
    }

    private void getReadableDatabaseIfClosedOrNull(){
        if(mSQLiteDatabase==null || !mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase = this.getReadableDatabase();
            //Log.i(TAG,"inside GetWDbIfClosedOrNull(),so Got the W-DB");
            //如果是可写DB，也能用，不再开关切换。
        }
    }

    //关数据库
    private void closeDB(){
        //Log.i(TAG,"inside closeDB, before any calls.");
        if(mSQLiteDatabase != null && mSQLiteDatabase.isOpen()){
            try{
                mSQLiteDatabase.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        } // end if
    }


}
