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
import java.util.List;

import com.vkyoungcn.learningtools.models.*;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class YouMemoryDbHelper extends SQLiteOpenHelper {
    //如果修改了数据库结构方案，则应当改动（增加）版本号
    private static final String TAG = "YouMemory-DbHelper";
    private static final int DATEBASE_VERSION = 7;
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

    /* version 5 */
    public static final String SQL_CREATE_GROUP =
            "CREATE TABLE " + YouMemoryContract.Group.TABLE_NAME + " (" +
                    YouMemoryContract.Group._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    YouMemoryContract.Group.COLUMN_DESCRIPTION + " TEXT, "+
                    YouMemoryContract.Group.COLUMN_SPECIAL_MARK + " INTEGER, "+
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
                YouMemoryContract.ItemBasic.COLUMN_PICKING_TIME_LIST + " TEXT, " +
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
//        Log.i(TAG,"inside YouMemoryDbHelper,CREAT ITEM_DEFAULT");

        //向Mission表增加默认记录
        Mission defaultMission  = new Mission("EnglishWords13531","螺旋式背单词",DEFAULT_ITEM_SUFFIX);

        Log.i(TAG, "dataInitialization: ready to insert default_mission,mission= "+defaultMission);
        createMission(db,defaultMission);//传入db是避免调用getDataBase，后者（会调用onCreate）导致递归调用错误

//      Item_default13531表数据导入
        db.execSQL(getSqlCreateItemWithSuffix(DEFAULT_ITEM_SUFFIX));
        Log.i(TAG, "onUpgrade: case 1");
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

    public long createGroup(DBRwaGroup dbRwaGroup){
        long l;
        Log.i(TAG, "createGroup: before");
        getWritableDatabaseIfClosedOrNull();

        ContentValues values = new ContentValues();

        values.put(YouMemoryContract.Group.COLUMN_DESCRIPTION, dbRwaGroup.getDescription());
        values.put(YouMemoryContract.Group.COLUMN_SPECIAL_MARK, dbRwaGroup.getSpecial_mark());
        values.put(YouMemoryContract.Group.COLUMN_MISSION_ID, dbRwaGroup.getMission_id());
        values.put(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS, dbRwaGroup.getSubItems_ids());

        l = mSQLiteDatabase.insert(YouMemoryContract.Group.TABLE_NAME, null, values);

        Log.i(TAG, "createGroup: lines:"+l);
        closeDB();

        return l;
    }

    public List<DBRwaGroup> getAllGroupsByMissionId(int missionsId){
        List<DBRwaGroup> groups = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.Group.TABLE_NAME+
                " WHERE "+YouMemoryContract.Group.COLUMN_MISSION_ID+" = "+missionsId;

        Log.i(TAG, "getAllGroupsByMissionId: before");
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                DBRwaGroup group = new DBRwaGroup();
                group.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group._ID)));
                group.setDescription(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_DESCRIPTION)));
                group.setSubItems_ids(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS)));
                group.setGroupLogs(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_GROUP_LOGS)));
                group.setMission_id(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_MISSION_ID)));
                group.setSpecial_mark(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_SPECIAL_MARK)));

                groups.add(group);
            }while (cursor.moveToNext());
        }
        Log.i(TAG, "getAllGroupsByMissionId: after. done");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return groups;
    }

    public DBRwaGroup getGroupsById(int groupId){
        DBRwaGroup group = new DBRwaGroup();
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.Group.TABLE_NAME+
                " WHERE "+YouMemoryContract.Group._ID+" = "+groupId;

        Log.i(TAG, "getGroupById: before");
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
                group.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group._ID)));
                group.setDescription(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_DESCRIPTION)));
                group.setSubItems_ids(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS)));
                group.setGroupLogs(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_GROUP_LOGS)));
                group.setMission_id(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_MISSION_ID)));
                group.setSpecial_mark(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Group.COLUMN_SPECIAL_MARK)));
        }
        Log.i(TAG, "getGroupsById: after. done");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return group;
    }


    /*
    * 按顺序选取Item中前n项记录（的id），提供给任务组的生成。
    * 相应记录要置已抽取
    * 未能选到任何结果时，返回null；
    * */
    public List<Integer> getCertainAmountItemIdsOrderly(int amount,String tableNameSuffix){
        List<Integer> ids = new ArrayList<>();
        String selectQuery = "SELECT TOP "+ amount +" "+YouMemoryContract.ItemBasic._ID
                +" FROM "+YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix;
        Log.i(TAG, "getCertainAmountItemIdsOrderly: ready to select certain amount Items");

        getReadableDatabaseIfClosedOrNull();

        StringBuilder sbIdsWithPth = new StringBuilder();//StringBuilder用于同步构建接下来Update各Item的chose状态时的WHERE子句。
        sbIdsWithPth.append("( ");
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                int i = cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID));
                sbIdsWithPth.append(i);
                sbIdsWithPth.append(", ");
                ids.add(i);
            }while (cursor.moveToNext());
            sbIdsWithPth.deleteCharAt(sbIdsWithPth.length()-2);//倒数第二个字符是附加的多余“，”删除
            sbIdsWithPth.append(")");

            String updateItemChoseAtSpecifiedPosition ="UPDATE "+YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                    +" SET "+YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" = 'true' WHERE "
                    +YouMemoryContract.ItemBasic._ID+" IN "+sbIdsWithPth.toString();

            mSQLiteDatabase.execSQL(updateItemChoseAtSpecifiedPosition);
//            Log.i(TAG, "getCertainAmountItemIdsOrderly: Item chose state set.");
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }else {
            return null;
        }

        Log.i(TAG, "getCertainAmountItemIdsOrderly: got certain amount items,and set chose");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return ids;
    }


    /*
     * 随机选取Item中前n项记录（的id），提供给任务组的生成。
     * 未能选到任何结果时，返回null；
     * 【待】我怎么记得涉及到SQLite的ID的项目都需用long啊？！
     * */
    public List<Integer> getCertainAmountItemIdsRandomly(int amount,String tableNameSuffix){
        List<Integer> ids = new ArrayList<>();
        String selectQuery = "SELECT "+YouMemoryContract.ItemBasic._ID
                + " FROM "+YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                + " ORDER BY RANDOM() LIMIT "+amount;
        Log.i(TAG, "getCertainAmountItemIdsRandomly: ready to go");

        getReadableDatabaseIfClosedOrNull();

        StringBuilder sbIdsWithPth = new StringBuilder();//StringBuilder用于同步构建接下来Update各Item的chose状态时的WHERE子句。
        sbIdsWithPth.append("( ");
        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                int i = cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID));
                sbIdsWithPth.append(i);
                sbIdsWithPth.append(", ");
                ids.add(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID)));
            }while (cursor.moveToNext());

            sbIdsWithPth.deleteCharAt(sbIdsWithPth.length()-2);//倒数第二个字符是附加的多余“，”删除
            sbIdsWithPth.append(")");

            String updateItemChoseAtSpecifiedPosition ="UPDATE "+YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                    +" SET "+YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" = 'true' WHERE "
                    +YouMemoryContract.ItemBasic._ID+" IN "+sbIdsWithPth.toString();

            mSQLiteDatabase.execSQL(updateItemChoseAtSpecifiedPosition);

            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }else {
            return null;
        }
        Log.i(TAG, "getCertainAmountItemIdsRandomly: got certain amount items");

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return ids;
    }


    public String getSingleItemNameById(long itemId,String suffix){
        String Itemname = null;
        String selectQuery = "SELECT "+YouMemoryContract.ItemBasic.COLUMN_NAME+
                " FROM "+ YouMemoryContract.ItemBasic.TABLE_NAME+suffix+
                " WHERE "+YouMemoryContract.ItemBasic._ID+" = "+itemId;
        Log.i(TAG, "getSingleItemNameById: before any");

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            Itemname = cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_NAME));

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
        return Itemname;

    }


    private void getWritableDatabaseIfClosedOrNull(){
//        Log.i(TAG, "getWritableDatabaseIfClosedOrNull: before any");
        if(mSQLiteDatabase==null || !mSQLiteDatabase.isOpen()) {
//            Log.i(TAG, "getWritableDatabaseIfClosedOrNull: inside but not got W");
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
            Log.i(TAG,"inside GetRDbIfClosedOrNull(),so Got the R-DB");
            //如果是可写DB，也能用，不再开关切换。
        }
    }

    //关数据库
    private void closeDB(){
        Log.i(TAG,"inside closeDB, before any calls.");
        if(mSQLiteDatabase != null && mSQLiteDatabase.isOpen()){
            try{
                mSQLiteDatabase.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        } // end if
    }


}
