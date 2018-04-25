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
    private boolean isFallBehind =false;//DB列。是不是掉队重组词汇
    private boolean isObsoleted = false;//v7新增列。是否因超时废止，相应ITEMs应改回未抽取；color应为红色。
    private String groupLogs="";//DB列本组记忆与复习日志；
    private String subItemIdsStr ="";//DB列。本组所属Items的id集合（替代了额外1：N的那个交叉表）。实际应是List<Integer>类型。
    //格式上，要求不同id记录间以英文分号分隔。
    private int mission_id=0;//v5新增。

    private boolean extra_1hAccomplished = false;//30~60的额外复习是否完成。初始false
    private short extra_24hAccomplishTimes = 0;//24小时内的额外复习次数。


    /* 备用字段
    private short additionalRePickingTimes_24 = 0;//额外加班补充的次数（24小时内）
    private short additionalRePickTimes_24_72 = 0;//额外加班补充的次数（24~72小时间）*/

    public DBRwaGroup() {
    }

    /*以下的转换式构造器无法使用，UiGroup缺少最后一列，只有苏bItems的数量，没有id详细列表。
    public DBRwaGroup(RvGroup group) {
        this.id = group.getId();
        this.description = group.getDescription();
        this.isFallBehind = group.getFallBehind();

        //"N#yyyy-MM-dd HH:mm:ss#false;"
        List<SingleLog> logs = group.getGroupLogs();
        StringBuilder sbForStringLogs = new StringBuilder();

        for (SingleLog l : logs) {
            sbForStringLogs.append(l.getN());
            sbForStringLogs.append("#");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sbForStringLogs.append(sdf.format(new Date(l.getTimeInMilli())));

            sbForStringLogs.append("#");
            sbForStringLogs.append(l.isMiss());
            sbForStringLogs.append(";");
        }
        this.groupLogs = sbForStringLogs.toString();
        this.subItemIdsStr = null;
    }*/


    public DBRwaGroup(int id, String description, boolean isFallBehind, String groupLogs, String subItemIdsStr, int mission_id) {
        this.id = id;
        this.description = description;
        this.isFallBehind = isFallBehind;
        this.groupLogs = groupLogs;
        this.subItemIdsStr = subItemIdsStr;
        this.mission_id = mission_id;
    }

    public DBRwaGroup(int id, String description, boolean isFallBehind, boolean isObsoleted, String groupLogs, String subItemIdsStr, int mission_id) {
        this.id = id;
        this.description = description;
        this.isFallBehind = isFallBehind;
        this.isObsoleted = isObsoleted;
        this.groupLogs = groupLogs;
        this.subItemIdsStr = subItemIdsStr;
        this.mission_id = mission_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isFallBehind() {
        return isFallBehind;
    }

    public void setFallBehind(boolean fallBehind) {
        isFallBehind = fallBehind;
    }

    public boolean isObsoleted() {
        return isObsoleted;
    }

    public void setObsoleted(boolean obsoleted) {
        isObsoleted = obsoleted;
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

    public boolean isExtra_1hAccomplished() {
        return extra_1hAccomplished;
    }

    public void setExtra_1hAccomplished(boolean extra_1hAccomplished) {
        this.extra_1hAccomplished = extra_1hAccomplished;
    }

    public short getExtra_24hAccomplishTimes() {
        return extra_24hAccomplishTimes;
    }

    public void setExtra_24hAccomplishTimes(short extra_24hAccomplishTimes) {
        this.extra_24hAccomplishTimes = extra_24hAccomplishTimes;
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
