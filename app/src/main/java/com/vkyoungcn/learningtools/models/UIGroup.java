package com.vkyoungcn.learningtools.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.vkyoungcn.learningtools.spiralCore.LogList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 * 与RecyclerView配合使用（适用与UI、Rv的）group，与DB配合的是DBRawGroup。
 */

public class UIGroup {
    private static final String TAG = "UIGroup";

    private int id = 0;//DB列。从DB取回时本列有意义。存入时由于Helper不写id列，值无所谓。
    private String description="";//DB列。默认填入该组“起始-末尾”词汇
    private boolean isFallBehind =false;//DB列。是不是掉队重组词汇。本组废弃后也应置废弃。
    private boolean isObsoleted=false;//v7新增列，是否因未及时复习而废止分组。对应Items应改回未抽取。
    private int mission_id = 0;//v5新增

    private List<LogModel> groupLogs = new ArrayList<>();//DB列本组记忆与复习日志；
    //防止调用时的空指针，需要在此实例化。

    private int subItemsTotalNumber = 0;//非DB列
    private CurrentState groupCurrentState = new CurrentState();//非DB列。本组当前状态；

    /* 备用字段
    private short additionalRePickingTimes_24 = 0;//额外加班补充的次数（24小时内）
    private short additionalRePickTimes_24_72 = 0;//额外加班补充的次数（24~72小时间）*/

    public UIGroup() {
    }

    public UIGroup(DBRwaGroup dbRwaGroup) {
        Log.i(TAG, "UIGroup: constructor from raw.");
        this.id = dbRwaGroup.getId();
        this.description = dbRwaGroup.getDescription();
        this.isFallBehind = dbRwaGroup.isFallBehind();
        this.isObsoleted = dbRwaGroup.isObsoleted();
        this.groupLogs = LogList.textListLogToListLog(dbRwaGroup.getGroupLogs());
        this.mission_id = dbRwaGroup.getMission_id();
        this.subItemsTotalNumber =dbRwaGroup.getItemAmount();
        this.groupCurrentState= new CurrentState();
        Log.i(TAG, "UIGroup: ready for current state.");
        LogList.setCurrentStateForGroup(this.groupCurrentState,this.groupLogs);
    }

    public UIGroup(int id, String description, boolean isFallBehind, boolean isObsoleted, int mission_id, List<LogModel> groupLogs, int subItemsTotalNumber, CurrentState groupCurrentState) {
        this.id = id;
        this.description = description;
        this.isFallBehind = isFallBehind;
        this.isObsoleted = isObsoleted;
        this.mission_id = mission_id;
        this.groupLogs = groupLogs;
        this.subItemsTotalNumber = subItemsTotalNumber;
        this.groupCurrentState = groupCurrentState;
    }

    public UIGroup(int id, String description, boolean isFallBehind, int mission_id, List<LogModel> groupLogs, int subItemsTotalNumber, CurrentState groupCurrentState) {
        this.id = id;
        this.description = description;
        this.isFallBehind = isFallBehind;
        this.mission_id = mission_id;
        this.groupLogs = groupLogs;
        this.subItemsTotalNumber = subItemsTotalNumber;
        this.groupCurrentState = groupCurrentState;
    }

    /*
    * 第五项可以传入List<Long>，降低耦合性
    * */
    public UIGroup(int id, String description, boolean isFallBehind, List<LogModel> groupLogs, List<Long> subItems, CurrentState groupCurrentState) {
        this.id = id;
        this.description = description;
        this.isFallBehind = isFallBehind;
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


    /*
     * Parcelable接口所要求覆写的一些内容
     * */
    /*public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(description);
        parcel.writeByte((byte)(isObsoleted? 1:0));
        parcel.writeByte((byte)(isFallBehind? 1:0));
        parcel.writeInt(mission_id);
        parcel.writeList(groupLogs);
        parcel.writeInt(subItemsTotalNumber);
        parcel.writeParcelable(groupCurrentState,0);

    }

    public static final Parcel.Creator<UIGroup> CREATOR = new Parcelable.Creator<UIGroup>(){
        @Override
        public UIGroup createFromParcel(Parcel parcel) {
            return new UIGroup(parcel);
        }

        @Override
        public UIGroup[] newArray(int size) {
            return new UIGroup[size];
        }
    };

    private UIGroup(Parcel in){
        id = in.readInt();
        description = in.readString();
        isObsoleted = in.readByte()==1;
        isFallBehind = in.readByte()==1;
        mission_id = in.readInt();
        groupLogs = in.readList();
    }*/

}
