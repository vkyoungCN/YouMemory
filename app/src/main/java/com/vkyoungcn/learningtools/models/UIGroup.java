package com.vkyoungcn.learningtools.models;

import android.util.Log;

import com.vkyoungcn.learningtools.spiralCore.LogList;

import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 * 与RecyclerView配合使用（适用与UI、Rv的）group，与DB配合的是DBRawGroup。
 */

public class UIGroup {
    private static final String TAG = "UIGroup";

    private int id = 0;//DB列。从DB取回时本列有意义。存入时由于Helper不写id列，值无所谓。
    private String description;//DB列。默认填入该组“起始-末尾”词汇
    private int special_mark;//DB列。是不是掉队重组词汇
    private int mission_id;//v5新增

    private List<LogModel> groupLogs;//DB列本组记忆与复习日志；

    private int subItemsTotalNumber;//非DB列
    private CurrentState groupCurrentState = null;//非DB列。本组当前状态；

    /* 备用字段
    private short additionalRePickingTimes_24 = 0;//额外加班补充的次数（24小时内）
    private short additionalRePickTimes_24_72 = 0;//额外加班补充的次数（24~72小时间）*/

    public UIGroup() {
    }

    public UIGroup(DBRwaGroup dbRwaGroup) {
        Log.i(TAG, "UIGroup: constructor from raw.");
        this.id = dbRwaGroup.getId();
        this.description = dbRwaGroup.getDescription();
        this.special_mark = dbRwaGroup.getSpecial_mark();
        this.groupLogs = LogList.textListLogToListLog(dbRwaGroup.getGroupLogs());
        this.mission_id = dbRwaGroup.getMission_id();
        this.subItemsTotalNumber =dbRwaGroup.getItemAmount();
        this.groupCurrentState= new CurrentState();
        Log.i(TAG, "UIGroup: ready for current state.");
        LogList.setCurrentStateForGroup(this.groupCurrentState,this.groupLogs);
    }


    public UIGroup(int id, String description, int special_mark, int mission_id, List<LogModel> groupLogs, int subItemsTotalNumber, CurrentState groupCurrentState) {
        this.id = id;
        this.description = description;
        this.special_mark = special_mark;
        this.mission_id = mission_id;
        this.groupLogs = groupLogs;
        this.subItemsTotalNumber = subItemsTotalNumber;
        this.groupCurrentState = groupCurrentState;
    }

    /*
    * 第五项可以传入List<Long>，降低耦合性
    * */
    public UIGroup(int id, String description, int special_mark, List<LogModel> groupLogs, List<Long> subItems, CurrentState groupCurrentState) {
        this.id = id;
        this.description = description;
        this.special_mark = special_mark;
        this.groupLogs = groupLogs;
        this.subItemsTotalNumber = subItems.size();
        this.groupCurrentState = groupCurrentState;
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

    public List<LogModel> getGroupLogs() {
        return groupLogs;
    }

    public void setGroupLogs(List<LogModel> groupLogs) {
        this.groupLogs = groupLogs;
    }

    public CurrentState getGroupCurrentState() {
        return groupCurrentState;
    }

    public void setGroupCurrentState(CurrentState groupCurrentState) {
        this.groupCurrentState = groupCurrentState;
    }

    public int getSubItemsTotalNumber() {
        return subItemsTotalNumber;
    }

    public void setSubItemsTotalNumber(int subItemsTotalNumber) {
        this.subItemsTotalNumber = subItemsTotalNumber;
    }

    public int getMission_id() {
        return mission_id;
    }

    public void setMission_id(int mission_id) {
        this.mission_id = mission_id;
    }
}
