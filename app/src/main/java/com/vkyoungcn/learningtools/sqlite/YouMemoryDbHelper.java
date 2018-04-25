package com.vkyoungcn.learningtools.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.*;
import com.vkyoungcn.learningtools.spiralCore.LogList;
import com.vkyoungcn.learningtools.spiralCore.SingleLog;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class YouMemoryDbHelper extends SQLiteOpenHelper {
    //如果修改了数据库结构方案，则应当改动（增加）版本号
    private static final String TAG = "YouMemory-DbHelper";
    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "YouMemory.db";
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
                YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE + " BOOLEAN)";//v6新增
    }


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
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

        getWritableDatabaseIfClosedOrNull();
    }

    //DCL模式单例，因为静态内部类模式不支持传参
    public static YouMemoryDbHelper getInstance(Context context){
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
        db.execSQL(SQL_CREATE_MISSION);
        db.execSQL(SQL_CREATE_GROUP);

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
        db.execSQL(getSqlCreateItemWithSuffix(DEFAULT_ITEM_SUFFIX));

        //向Mission表增加默认记录
        Mission defaultMission  = new Mission("EnglishWords13531","螺旋式背单词",DEFAULT_ITEM_SUFFIX);
        createMission(db,defaultMission);//传入db是避免调用getDataBase，后者（会调用onCreate）导致递归调用错误

        //Item_default13531表数据导入
        importToItemDefaultFromCSV("EbbingWords13531.csv",db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // 使用for实现跨版本升级数据库
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 4:
                    // 上次错误升级，版本号可能4、5；改名的临时表删除。新表无数据。
                    // 原计划删除的交叉表继续删除
                    db.execSQL("DROP TABLE IF EXISTS temp_old_group");
                    db.execSQL("DROP TABLE IF EXISTS "+YouMemoryContract.GroupCrossItem.TABLE_NAME + DEFAULT_ITEM_SUFFIX);
                    break;

                case 5:
                    //Item_默认表，新增一列。
                    String alterItem_default ="ALTER TABLE "+YouMemoryContract.ItemBasic.TABLE_NAME
                            +DEFAULT_ITEM_SUFFIX+" ADD COLUMN "
                            +YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" BOOLEAN";
                    db.execSQL(alterItem_default);
                    break;
                case 6:
                    //group表有错误，missionId列没有。故升级一次。
                    db.execSQL(SQL_DROP_GROUP);
                    db.execSQL(SQL_CREATE_GROUP);
                    break;

                case 7:
                    //group删一列，增两列。
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
        String line = "";

        InputStream is = null;
        try {
            is = context.getAssets().open(csvFileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            db.beginTransaction();

            int number = 0;
            while ((line = bufferedReader.readLine()) != null) {
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

            db.setTransactionSuccessful();
            db.endTransaction();

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        for (int i = oldVersion; i > newVersion; i--) {
            switch (i) {
                //没写。。
                default:
                    break;
            }
        }

    }

    /*CRUD部分需要时再写*/

    public long createMission(Mission mission){
        long l;

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
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            mission.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.Mission._ID)));
            mission.setName(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_NAME)));
            mission.setDescription(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_DESCRIPTION)));
            mission.setTableItem_suffix(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_TABLE_ITEM_SUFFIX)));
        }else{
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

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                missionTitles.add(cursor.getString(cursor.getColumnIndex(YouMemoryContract.Mission.COLUMN_NAME)));
            }while (cursor.moveToNext());
        }

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return missionTitles;

    }

    public List<SingleItem> getAllItems(String tableNameSuffix){
        List<SingleItem> items = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix;

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do {
                SingleItem item = new SingleItem();
                item.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID)));
                item.setName(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_NAME)));
                item.setExtending_list_1(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_1)));
                item.setExtending_list_2(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_2)));
                item.setChose(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE))==1);//【待测试。getString .equals(false)可用】
                items.add(item);
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();

        return items;
    }


    public List<SingleItem> getItemsByGroupSubItemIds(String subItemIds, String tableNameSuffix){
        List<SingleItem> items = new ArrayList<>();

        String sqlWhere = getStingSubIdsWithParenthesisForWhereSql(subItemIds);
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                +" WHERE "+YouMemoryContract.ItemBasic._ID+" IN "+sqlWhere;
        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do {
                SingleItem item = new SingleItem();
                item.setId(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID)));
                item.setName(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_NAME)));
                item.setExtending_list_1(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_1)));
                item.setExtending_list_2(cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_EXTENDING_LIST_2)));
                item.setChose(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE))==1);//[?]数据表设计时已经是BOOLEAN，表内数据太多，不适宜改造成INTEGER
                items.add(item);
            }while (cursor.moveToNext());
        }else{
            return null;
        }
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
        getWritableDatabaseIfClosedOrNull();

        mSQLiteDatabase.beginTransaction();
        ContentValues values = new ContentValues();

        values.put(YouMemoryContract.Group.COLUMN_DESCRIPTION, dbRwaGroup.getDescription());
        values.put(YouMemoryContract.Group.COLUMN_GROUP_LOGS, dbRwaGroup.getGroupLogs());
        values.put(YouMemoryContract.Group.COLUMN_IS_FALL_BEHIND, dbRwaGroup.isFallBehind());
        values.put(YouMemoryContract.Group.COLUMN_IS_OBSOLETED, dbRwaGroup.isObsoleted());
        values.put(YouMemoryContract.Group.COLUMN_MISSION_ID, dbRwaGroup.getMission_id());
        values.put(YouMemoryContract.Group.COLUMN_SUB_ITEM_IDS, dbRwaGroup.getSubItemIdsStr());

        l = mSQLiteDatabase.insert(YouMemoryContract.Group.TABLE_NAME, null, values);

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

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

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

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return groups;
    }

    public DBRwaGroup getGroupById(int groupId){
        DBRwaGroup group = new DBRwaGroup();
        String selectQuery = "SELECT * FROM "+ YouMemoryContract.Group.TABLE_NAME+
                " WHERE "+YouMemoryContract.Group._ID+" = "+groupId;

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

        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDB();
        return group;
    }


    /*
    * 更新指定分组的Logs学习记录。
    * */
    public long updateLogOfGroupById(int groupId,String newFullyLogs){
        long lines;
                getWritableDatabaseIfClosedOrNull();
                ContentValues values = new ContentValues();
                values.put(YouMemoryContract.Group.COLUMN_GROUP_LOGS,newFullyLogs);
                lines = mSQLiteDatabase.update(YouMemoryContract.Group.TABLE_NAME,values,
                        YouMemoryContract.Group._ID+"=?",new String[]{String.valueOf(groupId)});
                return lines;

    }

    public void removeGroupById(int groupId,String subItemIdsStr,String itemTableNameSuffix){
        getWritableDatabaseIfClosedOrNull();
        String deleteSingleGroupSql = "DELETE FROM "+YouMemoryContract.Group.TABLE_NAME+" WHERE "+
                YouMemoryContract.Group._ID+" = "+groupId;

        mSQLiteDatabase.beginTransaction();

        mSQLiteDatabase.execSQL(deleteSingleGroupSql);

        setItemsUnChose(itemTableNameSuffix,subItemIdsStr);

        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();
        closeDB();
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
    private void setItemsUnChose(String tableSuffix, String itemIds){
        getWritableDatabaseIfClosedOrNull();

        if(itemIds==null||itemIds.isEmpty()){
            return;
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
                " = 0 WHERE "+YouMemoryContract.ItemBasic._ID+
                " IN "+sbr.toString();

        mSQLiteDatabase.execSQL(itemsGiveBackSql);
    }

    /*
    * 设为已抽取，该方法将位于其他方法开启的事务内，因而不能有关DB操作。
    * */
    private void setItemsChose(String tableSuffix, String itemIds){
        getWritableDatabaseIfClosedOrNull();

        if(itemIds==null||itemIds.isEmpty()){
            return;
        }
        String[] str =  itemIds.split(";");
        StringBuilder sbr = new StringBuilder();
        sbr.append("( ");
        for (String s: str) {
            sbr.append(s);
            sbr.append(", ");
        }
        sbr.deleteCharAt(sbr.length()-2);//【错误记录，这里原来误写成str，错得很隐蔽】
        sbr.append(")");

        String itemsChoseSql = "UPDATE "+ YouMemoryContract.ItemBasic.TABLE_NAME+tableSuffix+
                " SET "+YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+
                " = 1 WHERE "+YouMemoryContract.ItemBasic._ID+
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
                +" WHERE "+YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" =  0  OR "
                +YouMemoryContract.ItemBasic.COLUMN_HAS_BEEN_CHOSE+" IS NULL";
        String selectQueryOuter = "SELECT "+YouMemoryContract.ItemBasic._ID
                +" FROM "+YouMemoryContract.ItemBasic.TABLE_NAME+tableNameSuffix
                +" WHERE "+YouMemoryContract.ItemBasic._ID+" IN ( "+selectQueryInner+" ) LIMIT "+amount;


        getReadableDatabaseIfClosedOrNull();
//        mSQLiteDatabase.beginTransaction();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQueryOuter, null);

        if(cursor.moveToFirst()){
            do{
                int i = cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID));
                ids.add(i);
            }while (cursor.moveToNext());

        }//【即使无结果也不能返回null；返回长为0的list即可】

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

        getReadableDatabaseIfClosedOrNull();

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                ids.add(cursor.getInt(cursor.getColumnIndex(YouMemoryContract.ItemBasic._ID)));
            }while (cursor.moveToNext());

        }

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

        getReadableDatabaseIfClosedOrNull();
        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            itemName = cursor.getString(cursor.getColumnIndex(YouMemoryContract.ItemBasic.COLUMN_NAME));

        }else{
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

    private String getStingSubIdsWithParenthesisForWhereSql(String subItemsIdsStr){
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
        if(mSQLiteDatabase==null || !mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase = this.getWritableDatabase();
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
            //如果是可写DB，也能用，不再开关切换。
        }
    }

    //关数据库
    private void closeDB(){
        if(mSQLiteDatabase != null && mSQLiteDatabase.isOpen()){
            try{
                mSQLiteDatabase.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        } // end if
    }

}
