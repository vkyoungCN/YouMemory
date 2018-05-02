package com.vkyoungcn.learningtools.models;

import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.spiralCore.GroupManager;

/**
 * Updated by VkYoung16 on 2018/5/1 21:24.
 * 用于RecyclerView的数据模型，只提供直接数据；
 * 运算部分提前完成不能带到适配器内部，否则运行卡顿严重。
 */
public class RvGroup implements Cloneable{
    private static final String TAG = "RvGroup";

    private int id = 0;//DB数据，RvUI使用。
    private String description="";//DB数据。RvUI使用。

    private String timePast = "";//根据“初学时间”字段计算
    private int stageColorRes = 0;

    private String missionItemTableSuffix ="";//RV-row点击进入GroupDetail后,展示所属Item时使用。由调用方Activity传递。
    private String strSubItemsIds = "";//DB数据。RV-row点击进入GroupDetail后所属Items详情列表使用。
    private int totalSubItemsNumber = 0;//计算数据，RvUI使用。

    /*本版新增区域*/
    private int rePickingTimes_30m;//前30分钟内的复习次数。
    private int earlyTimeRePickingTimes;//包括30m内的次数在内。
    private boolean doubleKill = false;//是否有连续的两次复习；复习逻辑在复习完成时对本次开始和上次结束时间进行比较，（如果本字段为否且）时差6分钟内视作连续，对字段记真。
    private boolean tripleKill = false;//是否有连续的三次复习；


    public RvGroup() {
    }


    public RvGroup(DBRwaGroup dbRwaGroup,int id, String timePast, int stageColorRes, String missionItemTableSuffix, int rePickingTimes_30m, int earlyTimeRePickingTimes, boolean doubleKill, boolean tripleKill) {
        this.id = dbRwaGroup.getId();
        this.description = dbRwaGroup.getDescription();
        this.strSubItemsIds = dbRwaGroup.getSubItemIdsStr();
        this.totalSubItemsNumber = (GroupManager.getItemAmountFromSubItemStr(dbRwaGroup.getSubItemIdsStr()));this.id = id;
        this.rePickingTimes_30m = dbRwaGroup.getRePickingTimes_30m();
        this.earlyTimeRePickingTimes = dbRwaGroup.getEarlyTimeRePickingTimes();
        this.doubleKill = dbRwaGroup.isDoubleKill();
        this.tripleKill = dbRwaGroup.isTripleKill();

        this.timePast = toTimePastStr(dbRwaGroup.getInitLearningLong());
        this.stageColorRes = calculateStageColor(dbRwaGroup.getInitLearningLong());

        this.missionItemTableSuffix = missionItemTableSuffix;


    }

    private static String toTimePastStr(long initLearningLong){
        long currentLong = System.currentTimeMillis();
        long timeBetweenMinutes = (currentLong - initLearningLong)/1000*60;
        if(timeBetweenMinutes==0){
            //不足一分钟，输出“刚刚”
            return "刚刚";
        }else if(timeBetweenMinutes<60){
            //不足1小时，超出1分钟，启用M字段
            return "+"+timeBetweenMinutes+"分钟";
        }else if(timeBetweenMinutes<60*24){
            //不足一天，超出1小时，启用H/m字段，不启用D字段。
            byte hourSection = (byte)(timeBetweenMinutes/60);
            byte minuteSection = (byte)(timeBetweenMinutes%60);
            return "+"+hourSection+"H"+minuteSection+"min";
        }else {
            //超出一天，启用D字段
            short daySection = (short)((timeBetweenMinutes/60)/24);
//            byte hourSection = (byte)((timeBetweenMinutes%(60*24))/60);
            return "+"+daySection+"天";
        }
    }

    private static int calculateStageColor(long initLearningLong){
        long currentLong = System.currentTimeMillis();
        long timeBetweenMinutes = (currentLong - initLearningLong)/1000*60;
        if(timeBetweenMinutes<=30){
            return R.color.color_GroupStage_30m;
        }else if(timeBetweenMinutes<=360){
            return R.color.color_GroupStage_6h;
        }else if(timeBetweenMinutes<720){
            return R.color.color_GroupStage_12h;
        }else if(timeBetweenMinutes<60*24){
            return R.color.color_GroupStage_24h;
        }else if(timeBetweenMinutes<60*24*2){
            return R.color.color_GroupStage_2d;
        }else if(timeBetweenMinutes<60*24*4){
            return R.color.color_GroupStage_4d;
        }else if(timeBetweenMinutes<60*24*8){
            return R.color.color_GroupStage_8d;
        }else if(timeBetweenMinutes<60*24*15){
            return R.color.color_GroupStage_15d;
        }else {
            return R.color.color_GroupStage_15d_U;
        }
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimePast() {
        return timePast;
    }

    public void setTimePast(String timePast) {
        this.timePast = timePast;
    }

    public int getStageColorRes() {
        return stageColorRes;
    }

    public void setStageColorRes(int stageColorRes) {
        this.stageColorRes = stageColorRes;
    }

    public String getStrSubItemsIds() {
        return strSubItemsIds;
    }

    public void setStrSubItemsIds(String strSubItemsIds) {
        this.strSubItemsIds = strSubItemsIds;
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
     * Parcelable接口所要求覆写的一些内容【后期增加的1h 24h两字段暂未加入Parcelable】
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
