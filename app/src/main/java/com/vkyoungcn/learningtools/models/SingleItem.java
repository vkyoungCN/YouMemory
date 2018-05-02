package com.vkyoungcn.learningtools.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class SingleItem implements Parcelable {
    private int id =0;
    private String name="";
    private String extending_list_1="";
    private String extending_list_2="";
    private boolean isChose=false;//已被分组抽取过的词汇置true，不会被其他分组再次抽取。

    private boolean isRemindingBlur = false;
    private short failedSpelling_times = 0;//本词汇迄今拼写错误总次数。自动记录，点击翻面查看原词时，当次复习置拼错，总量+1；但一次复习中的多次拼错只记录1次。
    private boolean isSpellingFailed = false;//当次复习是否拼写失败；在最后向DB写入记录时如本项为true则failedSpelling_times +1。
    private short failedReminding_times = 0;//类似上条；但是手动设置。
    private short priority = 2;//初始2，数字越大越重要。当优先级在7以下时，每拼错、记错一次+1；可以手动调节数值。

    public SingleItem() {
    }

    public SingleItem(int id, String name, String extending_list_1, String extending_list_2, boolean isChose, boolean isRemindingBlur, short failedSpelling_times, short failedReminding_times, short priority) {
        this.id = id;
        this.name = name;
        this.extending_list_1 = extending_list_1;
        this.extending_list_2 = extending_list_2;
        this.isChose = isChose;
        this.isRemindingBlur = isRemindingBlur;
        this.failedSpelling_times = failedSpelling_times;
        this.failedReminding_times = failedReminding_times;
        this.priority = priority;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtending_list_1() {
        return extending_list_1;
    }

    public void setExtending_list_1(String extending_list_1) {
        this.extending_list_1 = extending_list_1;
    }

    public String getExtending_list_2() {
        return extending_list_2;
    }

    public void setExtending_list_2(String extending_list_2) {
        this.extending_list_2 = extending_list_2;
    }

    public Boolean isChose() {
        return isChose;
    }

    public void setChose(Boolean chose) {
        isChose = chose;
    }

    public void setChose(boolean chose) {
        isChose = chose;
    }

    public boolean isRemindingBlur() {
        return isRemindingBlur;
    }

    public void setRemindingBlur(boolean remindingBlur) {
        isRemindingBlur = remindingBlur;
    }

    public short getFailedSpelling_times() {
        return failedSpelling_times;
    }

    public void setFailedSpelling_times(short failedSpelling_times) {
        this.failedSpelling_times = failedSpelling_times;
    }

    public boolean isSpellingFailed() {
        return isSpellingFailed;
    }

    public void setSpellingFailed(boolean spellingFailed) {
        isSpellingFailed = spellingFailed;
    }

    public short getFailedReminding_times() {
        return failedReminding_times;
    }

    public void setFailedReminding_times(short failedReminding_times) {
        this.failedReminding_times = failedReminding_times;
    }

    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }

    /*
     * 以下是Parcelable要求的内容
     * */
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(extending_list_1);
        parcel.writeString(extending_list_2);
        parcel.writeByte((byte)( isChose?1:0));

        parcel.writeByte((byte)(isRemindingBlur?1:0));
        parcel.writeInt(failedSpelling_times);
        parcel.writeByte((byte)(isSpellingFailed?1:0));
        parcel.writeInt(failedReminding_times);
        parcel.writeInt(priority);
    }

    public static final Parcelable.Creator<SingleItem> CREATOR = new Parcelable.Creator<SingleItem>(){
        @Override
        public SingleItem createFromParcel(Parcel parcel) {
            return new SingleItem(parcel);
        }

        @Override
        public SingleItem[] newArray(int size) {
            return new SingleItem[size];
        }
    };

    private SingleItem(Parcel in){
        id = in.readInt();
        name = in.readString();
        extending_list_1 = in.readString();
        extending_list_2 = in.readString();
        isChose = in.readByte()==1;

        isRemindingBlur = in.readByte()==1;
        failedSpelling_times = (short) in.readInt();
        isSpellingFailed = in.readByte()==1;
        failedReminding_times = (short)in.readInt();
        priority = (short) in.readInt();
    }


}
