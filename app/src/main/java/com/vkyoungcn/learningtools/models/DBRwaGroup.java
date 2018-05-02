package com.vkyoungcn.learningtools.models;

import java.util.List;

/*
* 主要用于向数据库存记录（的最后与DB交互）时使用，以及从数据库取数据（最初从DB取出数据）时使用
* 若要将数据应用到UI，需转化为Group类。
* */
public class DBRwaGroup {
    private static final String TAG = "Group";
    private int id = 0;//DB列
    private String description="";//DB列。默认填入该组“起始-末尾”词汇
    private String subItemIdsStr ="";//DB列。本组所属Items的id集合（替代了额外1：N的那个交叉表）。实际应是List<Integer>类型。
    private int mission_id=0;//v5新增。
    private long initLearningLong;//初学时间，需据此记录计算已过时间和当前所处时间段的颜色。

    /*本版新增区域*/
    private int rePickingTimes_30m = 0;//前30分钟内的复习次数。
    private int earlyTimeRePickingTimes = 0;//包括30m内的次数在内。
    private boolean doubleKill = false;//是否有连续的两次复习；复习逻辑在复习完成时对本次开始和上次结束时间进行比较，（如果本字段为否且）时差6分钟内视作连续，对字段记真。
    private boolean tripleKill = false;//是否有连续的三次复习；



//    private boolean isFallBehind =false;//DB列。是不是掉队重组词汇
//    private boolean isObsoleted = false;//v7新增列。是否因超时废止，相应ITEMs应改回未抽取；color应为红色。
//    private String groupLogs="";//DB列本组记忆与复习日志；
    //格式上，要求不同id记录间以英文分号分隔。

//    private boolean extra_1hAccomplished = false;//30~60的额外复习是否完成。初始false
//    private short extra_24hAccomplishTimes = 0;//24小时内的额外复习次数。


    public DBRwaGroup() {
    }


    public DBRwaGroup(int id, String description, String subItemIdsStr, int mission_id, long initLearningLong, int rePickingTimes_30m, int earlyTimeRePickingTimes, boolean doubleKill, boolean tripleKill) {
        this.id = id;
        this.description = description;
        this.subItemIdsStr = subItemIdsStr;
        this.mission_id = mission_id;
        this.initLearningLong = initLearningLong;
        this.rePickingTimes_30m = rePickingTimes_30m;
        this.earlyTimeRePickingTimes = earlyTimeRePickingTimes;
        this.doubleKill = doubleKill;
        this.tripleKill = tripleKill;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubItemIdsStr() {
        return subItemIdsStr;
    }

    public void setSubItemIdsStr(String subItemIdsStr) {
        this.subItemIdsStr = subItemIdsStr;
    }

    public int getMission_id() {
        return mission_id;
    }

    public void setMission_id(int mission_id) {
        this.mission_id = mission_id;
    }

    public long getInitLearningLong() {
        return initLearningLong;
    }

    public void setInitLearningLong(long initLearningLong) {
        this.initLearningLong = initLearningLong;
    }

    public int getRePickingTimes_30m() {
        return rePickingTimes_30m;
    }

    public void setRePickingTimes_30m(int rePickingTimes_30m) {
        this.rePickingTimes_30m = rePickingTimes_30m;
    }

    public int getEarlyTimeRePickingTimes() {
        return earlyTimeRePickingTimes;
    }

    public void setEarlyTimeRePickingTimes(int earlyTimeRePickingTimes) {
        this.earlyTimeRePickingTimes = earlyTimeRePickingTimes;
    }

    public boolean isDoubleKill() {
        return doubleKill;
    }

    public void setDoubleKill(boolean doubleKill) {
        this.doubleKill = doubleKill;
    }

    public boolean isTripleKill() {
        return tripleKill;
    }

    public void setTripleKill(boolean tripleKill) {
        this.tripleKill = tripleKill;
    }

    public static String subItemIdsListIntToString(List<Integer> idsList){

        StringBuilder sbIds = new StringBuilder();
        for (int i :idsList) {
            sbIds.append(i);
            sbIds.append(";");
        }
        return sbIds.toString();
    }

    public String toStingIdsWithParenthesisForWhereSql(){
        if(subItemIdsStr ==null|| subItemIdsStr.isEmpty()){
            return "()";
        }
        String[] str =  this.subItemIdsStr.split(";");
        StringBuilder sbr = new StringBuilder();
        sbr.append("( ");
        for (String s: str) {
            sbr.append(s);
            sbr.append(", ");
        }
        sbr.deleteCharAt(str.length-2);
        sbr.append(")");
        return sbr.toString();

    }

}
