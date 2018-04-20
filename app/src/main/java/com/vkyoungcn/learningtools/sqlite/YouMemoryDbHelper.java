package com.vkyoungcn.learningtools.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.*;
import com.vkyoungcn.learningtools.spiralCore.LogList;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class YouMemoryDbHelper extends SQLiteOpenHelper {
    //如果修改了数据库结构方案，则应当改动（增加）版本号
    private static final String TAG = "YouMemory-DbHelper";
    private static final int DATEBASE_VERSION = 8;
    private static final String DATEBASE_NAME = "YouMemory.db";
    private volatile static YouMemoryDbHelper sYouMemoryDbHelper = null;
    private SQLiteDatabase mSQLiteDatabase = null;

    private Context context = null;

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

    /* version 8 */
    public static final String SQL_CREATE_GROUP =
            "CREATE TABLE " + YouMemoryContract.Group.TABLE_NAME + " (" +
                    YouMemoryContract.Group._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    YouMemoryContract.Group.COLUMN_DESCRIPTION + " TEXT, "+
                    YouMemoryContract.Group.COLUMN_IS_FALL_BEHIND + " BOOLEAN, "+
                    YouMemoryContract.Group.COLUMN_IS_OBSOLETED + " BOOLEAN, "+
                    YouMemoryContract.Group.COLUMN_GROUP_LOGS + " TEXT, "+ //version4新增列。
                    YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS + " TEXT, " +
                    YouMemoryContract.Group.COLUMN_MISSION_ID + " INTEGER REFERENCES "+ //version5新增列。
                    YouMemoryContract.Mission.TABLE_NAME+"("+YouMemoryContract.Mission._ID+") " +
                    "ON DELETE CASCADE)";//version4新增列。
                    //外键采用级联删除，以防missionId被重用时分组混淆（虽然重用的可能性很低）

    /*以下两种表需要根据具体的任务id创建，需动态生成建表语句*/
    /* 根据Mission_id创建具体的任务项目表，所需语句*/
    /* version 6 */
    public String getSqlCreateItemWithSuffix(String suffix){
         return "CREATE TABLE " +
                YouMemoryContract.ItemBasic.TABLE_NAME + suffix+" (" +
                YouMemoryContract.ItemBasic._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                YouMemoryContract.ItemBasic.COLUMN_NAME + " TEXT, " +
                YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_1 + " TEXT, " +
                YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_2 + " TEXT, " +
//                YouMemoryContract.ItemBasic.COLUMN_PICKING_TIME_LIST + " TEXT, " + //v9删除（升级逻辑未做更改，只是不使用了而已）
                YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE + " BOOLEAN)";//v6新增
    }

    /* version 5
    public String getSqlCreateItemWithSuffix(String suffix){
        return "CREATE TABLE " +
                YouMemoryContract.ItemBasic.TABLE_NAME + suffix+" (" +
                YouMemoryContract.ItemBasic._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                YouMemoryContract.ItemBasic.COLUMN_NAME + " TEXT, " +
                YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_1 + " TEXT, " +
                YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_2 + " TEXT, " +
                YouMemoryContract.ItemBasic.COLUMN_PICKING_TIME_LIST + " TEXT)";
    }*/

    private static final String SQL_DROP_MISSION =
            "DROP TABLE IF EXISTS " +  YouMemoryContract.Mission.TABLE_NAME;
    private static final String SQL_DROP_GROUP =
            "DROP TABLE IF EXISTS " + YouMemoryContract.Group.TABLE_NAME;
    private static final String SQL_DROP_MISSION_X_GROUP =
            "DROP TABLE IF EXISTS " + YouMemoryContract.MissionCrossGroup.TABLE_NAME;

    /*以下两种表的删除语句动态生成*/
    //原名……WithMissionId()
    public String getSqlDropItemWithSuffix(String suffix){
        return "DROP TABLE IF EXISTS " +  YouMemoryContract.ItemBasic.TABLE_NAME + suffix;
    }

    public YouMemoryDbHelper(Context context) {
        super(context, DATEBASE_NAME, null, DATEBASE_VERSION);
        //Log.i(TAG,"inside YouMemoryDbHelper Constructor, after the super ");
        this.context = context;

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

        //db.execSQL(SQL_CREATE_MISSION_X_GROUP);已在v5删除，由group的一列替代。
        //其余两个（默认）表在以下方法中建立，随后在该方法中导入相应数据。
        dataInitialization(db);
    }

    /*
    * 在本方法中，建立Item_default13531表、GroupXItem_default13531表；
    * 为Mission表增添记录：EnglishWords13531、螺旋重复式背单词，共13531词，初级简单词汇已剔除、
    * _default13531；
    * 为Item_default13531表添加全部记录。（改由版本2处理）
    * */
    private void dataInitialization(SQLiteDatabase db){
//        Log.i(TAG, "dataInitialization: gotW");
        db.execSQL(getSqlCreateItemWithSuffix(DEFAULT_ITEM_SUFFIX));
//        Log.i(TAG,"inside YouMemoryDbHelper,CREATE ITEM_DEFAULT");

        //向Mission表增加默认记录
        Mission defaultMission  = new Mission("EnglishWords13531","螺旋式背单词",DEFAULT_ITEM_SUFFIX);

        Log.i(TAG, "dataInitialization: ready to insert default_mission,mission= "+defaultMission);
        createMission(db,defaultMission);//传入db是避免调用getDataBase，后者（会调用onCreate）导致递归调用错误

//      Item_default13531表数据导入
//        db.execSQL(getSqlCreateItemWithSuffix(DEFAULT_ITEM_SUFFIX));
//        Log.i(TAG, "onUpgrade: case 1");
        importToItemDefaultFromCSV("EbbingWords13531.csv",db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.i(TAG, "onUpgrade: before any");
        // 使用for实现跨版本升级数据库
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 4:
                    // 上次错误升级，版本号可能4、5；改名的临时表删除。新表无数据。
                    // 原计划删除的交叉表继续删除
                    db.execSQL("DROP TABLE IF EXISTS temp_old_group");
                    db.execSQL("DROP TABLE IF EXISTS "+YouMemoryContract.GroupCrossItem.TABLE_NAME + DEFAULT_ITEM_SUFFIX);
                    Log.i(TAG, "onUpgrade: case 4-5");
                    break;

                case 5:
                    //Item_默认表，新增一列。
                    String alterItem_default ="ALTER TABLE "+YouMemoryContract.ItemBasic.TABLE_NAME
                            +DEFAULT_ITEM_SUFFIX+" ADD COLUMN "
                            +YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" BOOLEAN";
                    db.execSQL(alterItem_default);
                    Log.i(TAG, "onUpgrade: case 5-6");
                    break;
                case 6:
                    //group表有错误，missionId列没有。故升级一次。
                    Log.i(TAG, "onUpgrade: v6-7");
                    db.execSQL(SQL_DROP_GROUP);
                    db.execSQL(SQL_CREATE_GROUP);
                    break;

                case 7:
                    //group删一列，增两列。
                    Log.i(TAG, "onUpgrade: v7-8");
                    String sqlAlt = "ALTER TABLE "+YouMemoryContract.Group.TABLE_NAME+" RENAME TO "+
                            YouMemoryContract.Group.TABLE_NAME+"_temp";
                    db.execSQL(sqlAlt);
                    db.execSQL(SQL_CREATE_GROUP);//按新版重建表
                    //读取旧数据（注意列数不同）插入新表，新字段用默认值。
                    Cursor c = db.rawQuery("SELECT * FROM "+YouMemoryContract.Group.TABLE_NAME+"_temp",null);
                    long l = 0;
                    if(c.moveToFirst()){
                        db.beginTransaction();
                        do{
                            ContentValues cv = new ContentValues();
                            cv.put(YouMemoryContract.Group._ID,c.getInt(c.getColumnIndex(YouMemoryContract.Group._ID)));
                            cv.put(YouMemoryContract.Group.COLUMN_DESCRIPTION,c.getString(c.getColumnIndex(YouMemoryContract.Group.COLUMN_DESCRIPTION)));
                            cv.put(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS,c.getString(c.getColumnIndex(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS)));
                            //手动变更此记录，作测试。
                            cv.put(YouMemoryContract.Group.COLUMN_GROUP_LOGS,"1#2018-04-05 08:01:01#false;");
                            cv.put(YouMemoryContract.Group.COLUMN_MISSION_ID,c.getInt(c.getColumnIndex(YouMemoryContract.Group.COLUMN_MISSION_ID)));
                            cv.put(YouMemoryContract.Group.COLUMN_IS_FALL_BEHIND,0);
                            cv.put(YouMemoryContract.Group.COLUMN_IS_OBSOLETED,0);

                             l+= db.insert(YouMemoryContract.Group.TABLE_NAME,null,cv);

                        }while (c.moveToNext());
                        db.setTransactionSuccessful();
                        db.endTransaction();
                    }
                    Log.i(TAG, "onUpgrade: v7-8, l= "+l);
                    db.execSQL("DROP TABLE IF EXISTS "+YouMemoryContract.Group.TABLE_NAME+"_temp");


                default:
                    break;
            }
        }
    }

    /*
    * 从csv文件向默认Item表导入数据；
    * 要求csv文件位于Assets目录、且为UTF-8编码。
    * csv文件不需要首行是列名。
    * */
    private void importToItemDefaultFromCSV(String csvFileName,SQLiteDatabase db){
        Log.i(TAG, "importItemsFromCSV: before any.");
        String line = "";

        InputStream is = null;
        try {
            is = context.getAssets().open(csvFileName);
//            Log.i(TAG, "importItemsFromCSV: csv opened from Assets.");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//            Log.i(TAG, "importItemsFromCSV: br got.");

            db.beginTransaction();

            Log.i(TAG, "importItemsFromCSV: transaction begun, ready to make while.");
            int number = 0;
            while ((line = bufferedReader.readLine()) != null) {
                Log.i(TAG, "importToItemDefaultFromCSV: in While number start from 0, now is:"+ number++);
                String[] str = line.split(",");

                ContentValues values = new ContentValues();
                //在csv文件中，第1列是id，舍去不要；2列是单词、3列音标、4列释义字串。
                values.put(YouMemoryContract.ItemBasic.COLUMN_NAME, str[1]);
                values.put(YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_1, str[2]);//音标
                values.put(YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_2, str[3]);//释义字串
                values.put(YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE, false);//v9,补【经提取测试，log.i中输出为0,并且是数字0，如果再提取，需要匹配为 = 0】

                db.insert(YouMemoryContract.ItemBasic.TABLE_NAME + DEFAULT_ITEM_SUFFIX, null, values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "importItemsFromCSV: after while");

            db.setTransactionSuccessful();
            db.endTransaction();

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

    public Mission getMissionById(long mission_id){
        Mission mission = new Mission();
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.Mission.TABLE_NAME+
                " WHERE "+YouMemoryContract.Mission._ID+" = "+mission_id;
//        Log.i(TAG, "getMissionById: before any.");
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            mission.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Mission._ID)));
            mission.setName(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_NAME)));
            mission.setDescription(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_DESCRIPTION)));
            mission.setTableItem_suffix(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_TABLE_ITEM_SUFFIX)));
        }else{
            Log.i(TAG, "getMissionById: wrong, selected nothing");
            return null;
        }

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return mission;
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
                mission.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Mission._ID)));
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


    public List<SingleItem> getItemsByGroupSubItemIds(String subItemIds, String tableNameSuffix){
        Log.i(TAG, "getItemsByGroupSubItemIds: b");
        List<SingleItem> items = new ArrayList<>();

        String sqlWhere = getStingSubIdsWithParenthesisForWhereSql(subItemIds);
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                +" WHERE "+YouMemoryContract.ItemBasic._ID+" IN "+sqlWhere;
//        Log.i(TAG, "getItemsByGroupSubItemIds: where = "+sqlWhere);
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do {
                SingleItem item = new SingleItem();
                item.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID)));
                item.setName(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_NAME)));
                item.setExtending_list_1(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_1)));
                item.setExtending_list_2(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_2)));
                item.setChose(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE)).equals("true"));//数据表设计时已经是BOOLEAN，表内数据太多，不适宜改造成INTEGER
                items.add(item);
            }while (cursor.moveToNext());
        }else{
//            Log.i(TAG, "getItemsByGroupSubItemIds: got nothing");
            return null;
        }
//        Log.i(TAG, "getItemsByGroupSubItemIds: items.size :"+items.size());
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return items;
    }

    /*
    * 建立新分组的时候，必须将所属items置为已抽取；所以需要tableSuffix
    * */
    public long createGroup(DBRwaGroup dbRwaGroup,String tableSuffix){
        long l;
//        Log.i(TAG, "createGroup: before");
        getWritableDatabaseIfClosedOrNull();

        mSQLiteDatabase.beginTransaction();
        ContentValues values = new ContentValues();

        values.put(YouMemoryContract.Group.COLUMN_DESCRIPTION, dbRwaGroup.getDescription());
        values.put(YouMemoryContract.Group.COLUMN_IS_FALL_BEHIND, dbRwaGroup.isFallBehind());
        values.put(YouMemoryContract.Group.COLUMN_IS_OBSOLETED, dbRwaGroup.isObsoleted());
        values.put(YouMemoryContract.Group.COLUMN_MISSION_ID, dbRwaGroup.getMission_id());
        values.put(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS, dbRwaGroup.getSubItemIdsStr());

        l = mSQLiteDatabase.insert(YouMemoryContract.Group.TABLE_NAME, null, values);

//        Log.i(TAG, "createGroup: lines:"+l);
        setItemsChose(tableSuffix,dbRwaGroup.getSubItemIdsStr());
        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();

        closeDB();

        return l;
    }

    public List<DBRwaGroup> getAllGroupsByMissionId(int missionsId){
        List<DBRwaGroup> groups = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.Group.TABLE_NAME+
                " WHERE "+YouMemoryContract.Group.COLUMN_MISSION_ID+" = "+missionsId;

//        Log.i(TAG, "getAllGroupsByMissionId: before. sql= "+selectQuery);
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);
//        Log.i(TAG, "getAllGroupsByMissionId: cursor.size "+cursor.getCount());

        if(cursor.moveToFirst()){
            do{
                DBRwaGroup group = new DBRwaGroup();
                group.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group._ID)));
                group.setDescription(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_DESCRIPTION)));
                group.setSubItemIdsStr(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS)));
                group.setGroupLogs(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_GROUP_LOGS)));
                group.setMission_id(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_MISSION_ID)));
                group.setFallBehind(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_IS_FALL_BEHIND))==1);
                group.setObsoleted(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_IS_OBSOLETED))==1);

                groups.add(group);
            }while (cursor.moveToNext());
        }
//        Log.i(TAG, "getAllGroupsByMissionId: after. done");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
//        Log.i(TAG, "getAllGroupsByMissionId: db get groups size: "+groups.size());
        return groups;
    }

    public DBRwaGroup getGroupById(int groupId){
        DBRwaGroup group = new DBRwaGroup();
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.Group.TABLE_NAME+
                " WHERE "+YouMemoryContract.Group._ID+" = "+groupId;

//        Log.i(TAG, "getGroupById: before");
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
                group.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group._ID)));
                group.setDescription(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_DESCRIPTION)));
                group.setSubItemIdsStr(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS)));
                group.setGroupLogs(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_GROUP_LOGS)));
                group.setMission_id(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_MISSION_ID)));
                group.setFallBehind(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_IS_FALL_BEHIND))==1);
                group.setObsoleted(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_IS_OBSOLETED))==1);
        }
//        Log.i(TAG, "getGroupById: after. done");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return group;
    }


    /*
    * 根据传入的完成时间、分组id、学习的类型（初学？无miss学习？已miss一次？），更新目标分组的log记录
    * 返回生成的log记录
    *
    * */
    public String updateLogOfGroup(int groupId,long finishTime,int learningTypeColor){
//        Log.i(TAG, "updateLogOfGroup: b, groupId = "+groupId);
        long lines;
        switch (learningTypeColor){
            case R.color.colorGP_Newly:
                //初学，直接将记录存入DB即可。
                //生成单条Log记录
                String singleLogStr = LogModel.getStrSingleLogModelFromLong(0,finishTime,false);
//                Log.i(TAG, "updateLogOfGroup: sql-log : "+singleLogStr);
                getWritableDatabaseIfClosedOrNull();
                ContentValues values = new ContentValues();
                values.put(YouMemoryContract.Group.COLUMN_GROUP_LOGS,singleLogStr);
                lines = mSQLiteDatabase.update(YouMemoryContract.Group.TABLE_NAME,values,
                        YouMemoryContract.Group._ID+"=?",new String[]{String.valueOf(groupId)});
//                Log.i(TAG, "updateLogOfGroup: lines = "+lines);
                return singleLogStr;
            case R.color.colorGP_STILL_NOT:
                return "";//未到时间的额外复习，直接返回空串（空串本身就是一个供接收方判断用的标志）

            case R.color.colorGP_AVAILABLE:
            case R.color.colorGP_Miss_ONCE:
                String oldLogsStr = getGroupById(groupId).getGroupLogs();
                String fullyNewLogListStr = LogList.updateStrLogList(oldLogsStr,finishTime,learningTypeColor);
                if(fullyNewLogListStr.isEmpty()) return "";//如果是60分钟内的复习，按设计不计入log，所调用的方法会
                // 直接返回isEmpty的字串，不再向DB写，直接向调用方返回空串。【目前的逻辑是暂定的，后期应在调用DB前
                // 进行其他判断，如果在60分钟内，则根本不应调用到方法内。】

//                Log.i(TAG, "updateLogOfGroup: missed once : "+fullyNewLogListStr);
                ContentValues values2 = new ContentValues();
                values2.put(YouMemoryContract.Group.COLUMN_GROUP_LOGS,fullyNewLogListStr);
                lines = mSQLiteDatabase.update(YouMemoryContract.Group.TABLE_NAME,values2,
                        YouMemoryContract.Group._ID+"=?",new String[]{String.valueOf(groupId)});
                return fullyNewLogListStr;

            default:
                //说明出现错误
                return null;
        }


    }


    /*
    * Timer中每隔1分钟检查一次当前mission的各group，符合条件的会调用此函数设为废弃。
    * */
    public void setGroupObsoleted(int groupId){
        getWritableDatabaseIfClosedOrNull();
        String setGroupObsoletedSql = "UPDATE "+YouMemoryContract.Group.TABLE_NAME+
                " SET "+YouMemoryContract.Group.COLUMN_IS_OBSOLETED+
                " = 'true' WHERE "+YouMemoryContract.Group._ID+" = "+groupId;
        mSQLiteDatabase.execSQL(setGroupObsoletedSql);
        closeDB();
    }

    /*该方法将位于其他方法开启的事务内，因而不能有关DB操作*/
    public void setItemsUnChose(String tableSuffix, String itemIds){
        getWritableDatabaseIfClosedOrNull();

        if(itemIds==null||itemIds.isEmpty()){
//            Log.i(TAG, "setItemsUnChose: enmpty ids.");;
        }
        String[] str =  itemIds.split(";");
        StringBuilder sbr = new StringBuilder();
        sbr.append("( ");
        for (String s: str) {
            sbr.append(s);
            sbr.append(", ");
        }
        sbr.deleteCharAt(sbr.length()-2);
        sbr.append(")");

        String itemsGiveBackSql = "UPDATE "+ YouMemoryContract.ItemBasic.TABLE_NAME+tableSuffix+
                " SET "+YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+
                " = 'false' WHERE "+YouMemoryContract.ItemBasic._ID+
                " IN "+sbr.toString();

        mSQLiteDatabase.execSQL(itemsGiveBackSql);
    }

    /*
    * 设为已抽取，该方法将位于其他方法开启的事务内，因而不能有关DB操作。
    * */
    public void setItemsChose(String tableSuffix, String itemIds){
        getWritableDatabaseIfClosedOrNull();

        if(itemIds==null||itemIds.isEmpty()){
//            Log.i(TAG, "setItemsUnChose: empty ids.");;
        }
        String[] str =  itemIds.split(";");
        StringBuilder sbr = new StringBuilder();
//        Log.i(TAG, "setItemsChose: ids raw"+itemIds);
        sbr.append("( ");
        for (String s: str) {
            sbr.append(s);
            sbr.append(", ");
        }
        sbr.deleteCharAt(sbr.length()-2);//【错误记录，这里原来误写成str，错得很隐蔽】
        sbr.append(")");
//        Log.i(TAG, "setItemsChose: sql"+sbr.toString());

        String itemsChoseSql = "UPDATE "+ YouMemoryContract.ItemBasic.TABLE_NAME+tableSuffix+
                " SET "+YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+
                " = 'true' WHERE "+YouMemoryContract.ItemBasic._ID+
                " IN "+sbr.toString();

        mSQLiteDatabase.execSQL(itemsChoseSql);
    }

    /*
    * 按顺序选取Item中前n项记录（的id），提供给任务组的生成。
    * 要求是未抽取的
    * 相应记录要置已抽取（标记为抽取的操作改由创建Group时进行，作为一个整体事务，以免取回itemId后创建失败。）
    * 未能选到任何结果时，返回null；
    * */
    public List<Integer> getCertainAmountItemIdsOrderly(int amount,String tableNameSuffix){
        List<Integer> ids = new ArrayList<>();
        String selectQueryInner = "SELECT "+YouMemoryContract.ItemBasic._ID
                +" FROM "+YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                +" WHERE "+YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" = 0 OR "
                +YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" IS NULL";
        String selectQueryOuter = "SELECT "+YouMemoryContract.ItemBasic._ID
                +" FROM "+YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                +" WHERE "+YouMemoryContract.ItemBasic._ID+" IN ( "+selectQueryInner+" ) LIMIT "+amount;

//        Log.i(TAG, "getCertainAmountItemIdsOrderly: ready to select certain amount Items");
//        Log.i(TAG, "getCertainAmountItemIdsOrderly: sql: "+selectQueryOuter);

        getReadableDatabaseIfClosedOrNull();
//        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQueryOuter, null);

//        Log.i(TAG, "getCertainAmountItemIdsOrderly: cursor's size:"+cursor.getCount());
        if(cursor.moveToFirst()){
            do{
                int i = cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID));
                ids.add(i);
            }while (cursor.moveToNext());

        }//【即使无结果也不能返回null；返回长为0的list即可】
//        Log.i(TAG, "getCertainAmountItemIdsOrderly: ids"+ids.toString());

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return ids;
    }


    /*
     * 随机选取Item中前n项记录（的id），提供给任务组的生成。item置为已抽取的操作在建组时进行。
     * 未能选到任何结果时，返回null；
     * 【待】我怎么记得涉及到SQLite的ID的项目都需用long啊？！
     * */
    public List<Integer> getCertainAmountItemIdsRandomly(int amount,String tableNameSuffix){
        List<Integer> ids = new ArrayList<>();
        String selectQuery = "SELECT "+YouMemoryContract.ItemBasic._ID
                + " FROM "+YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                +" WHERE "+YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" = 0 OR "
                +YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" IS NULL "
                + " ORDER BY RANDOM() LIMIT "+amount;
//        Log.i(TAG, "getCertainAmountItemIdsRandomly: sql: "+selectQuery);

        getReadableDatabaseIfClosedOrNull();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                ids.add(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID)));
            }while (cursor.moveToNext());

        }
//        Log.i(TAG, "getCertainAmountItemIdsRandomly: got certain amount items");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return ids;
    }


    public String getSingleItemNameById(long itemId,String suffix){
        String itemName;
        String selectQuery = "SELECT "+YouMemoryContract.ItemBasic.COLUMN_NAME+
                " FROM "+ YouMemoryContract.ItemBasic.TABLE_NAME+suffix+
                " WHERE "+YouMemoryContract.ItemBasic._ID+" = "+itemId;
//        Log.i(TAG, "getSingleItemNameById: before any");

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            itemName = cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_NAME));

        }else{
            Log.i(TAG, "getMissionById: wrong, selected nothing");
            return null;
        }

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return itemName;

    }

    public String getStingSubIdsWithParenthesisForWhereSql(String subItemsIdsStr){
        if(subItemsIdsStr==null||subItemsIdsStr.isEmpty()){
            return "()";
        }
        String[] strings =  subItemsIdsStr.split(";");
        StringBuilder builder = new StringBuilder();
        builder.append("( ");
        for (String s: strings) {
            builder.append(s);
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length()-2);
        builder.append(")");
        return builder.toString();

    }

    private void getWritableDatabaseIfClosedOrNull(){
//        Log.i(TAG, "getWritableDatabaseIfClosedOrNull: before any");
        if(mSQLiteDatabase==null || !mSQLiteDatabase.isOpen()) {
//            Log.i(TAG, "getWritableDatabaseIfClosedOrNull: inside but not got W");
            mSQLiteDatabase = this.getWritableDatabase();
//            Log.i(TAG,"inside GetWDbIfClosedOrNull(),so Got the W-DB");
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
//            Log.i(TAG,"inside GetRDbIfClosedOrNull(),so Got the R-DB");
            //如果是可写DB，也能用，不再开关切换。
        }
    }

    //关数据库
    private void closeDB(){
//        Log.i(TAG,"inside closeDB, before any calls.");
        if(mSQLiteDatabase != null && mSQLiteDatabase.isOpen()){
            try{
                mSQLiteDatabase.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        } // end if
    }


}
