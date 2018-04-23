package com.vkyoungcn.learningtools.models;

import com.vkyoungcn.learningtools.spiralCore.GroupManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 * 用于RecyclerView的数据模型，只提供直接数据；
 * 运算部分提前完成不能带到适配器内部，否则运行卡顿严重。
 */
@SuppressWarnings("all")
public class RvGroup implements Cloneable{
    private static final String TAG = "RvGroup";

    private int id = 0;//DB数据，RvUI使用。
    private String description="";//DB数据。RvUI使用。
    private boolean isFallBehind =false;//DB数据，RvUI使用。是不是掉队重组词汇。本组废弃后也应置废弃。
    private boolean isObsoleted=false;//DB数据，RvUI使用。是否因未及时复习而废止分组。对应Items应改回未抽取。

    private int totalSubItemsNumber = 0;//计算数据，RvUI使用。
//    private GroupState groupCurrentState = new GroupState();//计算数据。【在直接设计下方两条的情况下，本计算数据可以不再持有】
    private String stateText = "";//计算数据，RvUI使用。
    private int stateColorResId = 0;//0 是移除底色。//计算数据，RvUI使用。


    private String missionItemTableSuffix ="";//RV-row点击进入GroupDetail后,展示所属Item时使用。
//    private List<String> groupLogs = new ArrayList<>();//本组记忆与复习日志；RV-row点击进入GroupDetail后点击日志按键时使用。
//    private List<Integer> strSubItemsIds = new ArrayList<>();//RV-row点击进入GroupDetail后所属Items详情列表使用。
    private String strSubItemsIds = "";//DB数据。RV-row点击进入GroupDetail后所属Items详情列表使用。
    private String strGroupLogs ="";//DB数据。RV-row点击进入GroupDetail后点击日志按键时使用。可在使用前转换为List<String>。

    /* 备用字段
    private short additionalRePickingTimes_24 = 0;//额外加班补充的次数（24小时内）
    private short additionalRePickTimes_24_72 = 0;//额外加班补充的次数（24~72小时间）*/

    public RvGroup() {
    }



    //用于从DBRawGroup到RvGroup的转换，但是tableSuffix字段前者并不持有，需要额外传入。
    //GroupState需计算后传入，用于设置stateText和stateColor;
    public RvGroup(DBRwaGroup dbRwaGroup, GroupState groupState,String tableSuffix) {
        this.id = dbRwaGroup.getId();
        this.description = dbRwaGroup.getDescription();
        this.isFallBehind = dbRwaGroup.isFallBehind();
        this.isObsoleted = dbRwaGroup.isObsoleted();
        this.strSubItemsIds = dbRwaGroup.getSubItemIdsStr();
        this.strGroupLogs = dbRwaGroup.getGroupLogs();

        this.totalSubItemsNumber = (GroupManager.getItemAmountFromSubItemStr(dbRwaGroup.getSubItemIdsStr()));
        this.missionItemTableSuffix = tableSuffix;

        this.stateText = GroupManager.getCurrentStateTimeAmountStringFromUIGroup(groupState);
        this.stateColorResId = groupState.getColorResId();

    }


    public int getTotalSubItemsNumber() {
        return totalSubItemsNumber;
    }

    public void setTotalSubItemsNumber(int totalSubItemsNumber) {
        this.totalSubItemsNumber = totalSubItemsNumber;
    }

    public String getMissionItemTableSuffix() {
        return missionItemTableSuffix;
    }

    public void setMissionItemTableSuffix(String missionItemTableSuffix) {
        this.missionItemTableSuffix = missionItemTableSuffix;
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

    public String getStrSubItemsIds() {
        return strSubItemsIds;
    }

    public void setStrSubItemsIds(String strSubItemsIds) {
        this.strSubItemsIds = strSubItemsIds;
    }

    public String getStrGroupLogs() {
        return strGroupLogs;
    }

    public void setStrGroupLogs(String strGroupLogs) {
        this.strGroupLogs = strGroupLogs;
    }

    public String getStateText() {
        return stateText;
    }

    public void setStateText(String stateText) {
        this.stateText = stateText;
    }

    public int getStateColorResId() {
        return stateColorResId;
    }

    public void setStateColorResId(int stateColorResId) {
        this.stateColorResId = stateColorResId;
    }

    public void calculateAndSetTotalSubItemsNumber() {
        String[] subItemsStr = strSubItemsIds.split(";");
        this.totalSubItemsNumber = subItemsStr.length;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //如果不包含GroupState则全是初级类型，浅复制即可
        RvGroup rvGroup = null;
        try {
            rvGroup = (RvGroup)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return rvGroup;

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

    public static final Parcel.Creator<RvGroup> CREATOR = new Parcelable.Creator<RvGroup>(){
        @Override
        public RvGroup createFromParcel(Parcel parcel) {
            return new RvGroup(parcel);
        }

        @Override
        public RvGroup[] newArray(int size) {
            return new RvGroup[size];
        }
    };

    private RvGroup(Parcel in){
        id = in.readInt();
        description = in.readString();
        isObsoleted = in.readByte()==1;
        isFallBehind = in.readByte()==1;
        mission_id = in.readInt();
        groupLogs = in.readList();
    }*/

}
