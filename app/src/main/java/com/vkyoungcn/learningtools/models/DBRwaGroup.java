package com.vkyoungcn.learningtools.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*
* 主要用于向数据库存记录（的最后与DB交互）时使用，以及从数据库取数据（最初从DB取出数据）时使用
* 若要将数据应用到UI，需转化为Group类。
* */
public class DBRwaGroup implements GroupSpecialMarks {
    private static final String TAG = "Group";
    private int id;//DB列
    private String description;//DB列。默认填入该组“起始-末尾”词汇
    private int special_mark;//DB列。是不是掉队重组词汇
    private String groupLogs;//DB列本组记忆与复习日志；
    private String subItems_ids;//DB列。本组所属Items的id集合（替代了额外1：N的那个交叉表）。实际应是List<Integer>类型。
    //格式上，要求不同id记录间以英文分号分隔。
    private int mission_id;//v5新增。


    /* 备用字段
    private short additionalRePickingTimes_24 = 0;//额外加班补充的次数（24小时内）
    private short additionalRePickTimes_24_72 = 0;//额外加班补充的次数（24~72小时间）*/

    public DBRwaGroup() {
    }

    /*以下的转换式构造器无法使用，UiGroup缺少最后一列，只有苏bItems的数量，没有id详细列表。
    public DBRwaGroup(UIGroup group) {
        this.id = group.getId();
        this.description = group.getDescription();
        this.special_mark = group.getSpecial_mark();

        //"N#YYYY-MM-DD hh:mm:ss#false;"
        List<LogModel> logs = group.getGroupLogs();
        StringBuilder sbForStringLogs = new StringBuilder();

        for (LogModel l : logs) {
            sbForStringLogs.append(l.getN());
            sbForStringLogs.append("#");

            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");
            sbForStringLogs.append(sdf.format(new Date(l.getTimeInMilli())));

            sbForStringLogs.append("#");
            sbForStringLogs.append(l.isMISS());
            sbForStringLogs.append(";");
        }
        this.groupLogs = sbForStringLogs.toString();
        this.subItems_ids = null;
    }*/


    public DBRwaGroup(int id, String description, int special_mark, String groupLogs, String subItems_ids, int mission_id) {
        this.id = id;
        this.description = description;
        this.special_mark = special_mark;
        this.groupLogs = groupLogs;
        this.subItems_ids = subItems_ids;
        this.mission_id = mission_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSpecial_mark() {
        return special_mark;
    }

    public void setSpecial_mark(int special_mark) {
        this.special_mark = special_mark;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupLogs() {
        return groupLogs;
    }

    public void setGroupLogs(String groupLogs) {
        this.groupLogs = groupLogs;
    }

    public String getSubItems_ids() {
        return subItems_ids;
    }

    public void setSubItems_ids(String subItems_ids) {
        this.subItems_ids = subItems_ids;
    }

    public int getMission_id() {
        return mission_id;
    }

    public void setMission_id(int mission_id) {
        this.mission_id = mission_id;
    }

    public int getItemAmount(){
        String[] subItemsStr = subItems_ids.split(";");
        return subItemsStr.length;
    }

    public static String subItemIdsListIntToString(List<Integer> idsList){

        StringBuilder sbIds = new StringBuilder();
        for (int i :idsList) {
            sbIds.append(i);
            sbIds.append(";");
        }
        return sbIds.toString();
    }


}
