package com.vkyoungcn.learningtools.sqlite;

import android.provider.BaseColumns;

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

    public static class MissionCrossGroup implements BaseColumns{
        public static final String TABLE_NAME ="mission_cross_group";
        public static final String COLUMN_MISSION_ID = "mission_id";
        public static final String COLUMN_GROUP_ID = "group_id";
    }

    public static class Group implements BaseColumns{
        public static final String TABLE_NAME = "group_table";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_INIT_PICK_TIME = "init_pick_time";
        public static final String COLUMN_LAST_PICK_TIME ="last_pick_time";
        public static final String COLUMN_TIME_PICKED = "time_picked";
        public static final String COLUMN_SPECIAL_MARK = "special_mark";
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
        public static final String COLUMN_EXTENDING_LIST = "extending_list";
        public static final String COLUMN_PICKING_TIME_LIST = "picking_time_list";
    }

}
