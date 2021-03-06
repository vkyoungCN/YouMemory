package com.vkyoungcn.learningtools.sqlite;

import android.provider.BaseColumns;

@SuppressWarnings("all")
/**
 * Created by VkYoung16 on 2018/3/26 0026.
 * 是数据库的设计方案类；描述了其中的表和字段结构。
 */

public final class YouMemoryContract {
    private static YouMemoryContract instance;

    static {
        instance = new YouMemoryContract();
    }

//    防止类意外实例化，令构造器为private。
    private YouMemoryContract(){}

    public static YouMemoryContract getInstance(){
        return instance;
    }

     /*以下各内部类，用于定义表的内容；本DB共有5 张业务表;
     * 三种资源Mission、Group、Item表；M-G、G-I交叉表；
     * 注意，另有各任务的资源表，如EnglishWords13531。
     * */

//    id列交由DB自动负责。
    public static class Mission implements BaseColumns{
        public static final String TABLE_NAME = "missions";
        public static final String COLUMN_NAME ="mission_name";
        public static final String COLUMN_DESCRIPTION = "mission_description";
        public static final String COLUMN_TABLE_ITEM_SUFFIX = "table_item_suffix";
    }

    /*public static class MissionCrossGroup implements BaseColumns{
        public static final String TABLE_NAME ="mission_cross_group";
        public static final String COLUMN_MISSION_ID = "mission_id";
        public static final String COLUMN_GROUP_ID = "group_id";
    }*/

/* version 9 */
    public static class Group implements BaseColumns{
        public static final String TABLE_NAME = "group_table";
        public static final String COLUMN_DESCRIPTION = "description";
//        public static final String COLUMN_IS_FALL_BEHIND= "is_fall_behind";//v8
//        public static final String COLUMN_IS_OBSOLETED= "is_obsoleted";//v8
        public static final String COLUMN_MISSION_ID = "mission_id";//v5新增，替代n:1任务-分组表
//        public static final String COLUMN_GROUP_LOGS = "group_logs";//v4新增；替代1:n日志交叉表
        public static final String COLUMN_SUB_ITEM_IDS = "sub_item_ids";//v4新增；替代1:n交叉表
//        public static final String COLUMN_EXTRA_1H = "extra_1h";//1小时内的复习是否执行（即30~60分的那次，不计入log，以本字段（布尔型）记录）
//        public static final String COLUMN_EXTRA_24H = "extra_24h";//1h以上，24小时以内的额外学习次数，是INTEGER型。
        public static final String COLUMN_INIT_LEARNING_LONG = "init_learning_time";//long型（DB整型）；初始学习时间。
        public static final String COLUMN_RE_PICKING_TIMES_30 = "re_picking_times_30";//整型；30min内的复习总次数。
        public static final String COLUMN_RE_PICKING_TIMES_EARLY = "re_picking_times_early";//整型；6小时内的复习总次数（含30min加总计算）。
        public static final String COLUMN_DOUBLE_KILL = "has_double_kill";//布尔；是否有连续地两次复习。
        public static final String COLUMN_TRIPLE_KILL = "has_triple_kill";//布尔；是否有连续地三次复习。



}

    public static class GroupCrossItem implements BaseColumns{
        public static final String TABLE_NAME = "group_cross_item";
        public static final String COLUMN_GROUP_ID = "group_id";
        public static final String COLUMN_ITEM_ID = "item_id";
    }

    /*
    * 各项任务的Item表内容不同，结构可以如下强行一致；但是各表的表名应在新建Mission时，以
    * 字符串连接的形式加上Mission_id做为尾缀，以互相区分。
    * Item表中以扩展内容（List<String>形式）记录如音标、释义等内容；
    * Item表中以List<Date>形式记录所有螺旋式记忆的记忆时间，不再单独设置Log表。
    * 由外界导入的任务资源，经过适当整理后，整体导入到各任务的Item_ID表中。
    * */
    public static class ItemBasic implements BaseColumns{
        public static final String TABLE_NAME = "item_";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_EXTENDING_LIST_1 = "extending_list_1";
        public static final String COLUMN_EXTENDING_LIST_2 = "extending_list_2";
        public static final String COLUMN_PICKING_TIME_LIST = "picking_time_list";//v9已取消
        public static final String COLUMN_HAS_BEEN_CHOSE = "been_chose";//v6新增
    }

}
