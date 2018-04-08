package com.vkyoungcn.learningtools.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.vkyoungcn.learningtools.R;


/*
* 此类是分组的一项状态集（阶段（以颜色表示）+剩余时间）
* 不是直接存入DB的字段；而是由分组的Log字段和当前时间计算而来。
* */
public class CurrentState implements Parcelable{
    private int colorResId = 0;//当前对应的颜色阶段，颜色的id，配合setBackgroundResource (int resid)，传入0时移除背景。
    //color字段目前主要在LogList类的setCurrentStateForGroup方法中设置。
    //color字段不仅显示颜色；还兼有逻辑判断功能，不只是颜色。
    private short remainingMinutes = 0;//距离下一阶段还剩余多少分钟。
    private short remainingHours = 0;//距离下一阶段还剩余多少小时。
    private short remainingDays = 0;



    public final static class ColorResId {
        public static final int COLOR_NEWLY = R.color.colorGP_Newly ;//新建组，没有初记Log,可能还没学习，建议绿色；
        public static final int COLOR_STILL_NOT = R.color.colorGP_STILL_NOT;//还没到复习时间，建议对应灰色；
        public static final int COLOR_AVAILABLE = R.color.colorGP_AVAILABLE;//可以进行复习，应在xHxM内完成本次复习，建议蓝色；
        public static final int COLOR_MISSED_ONCE = R.color.colorGP_Miss_ONCE;//错过了上一次，应在xHxM内完成本次复习，建议橙色；
        public static final int COLOR_MISSED_TWICE = R.color.colorGP_Miss_TWICE;//连续错过两次，标红。
        public static final int COLOR_FULL =0;//完成12次记录，超4个月的记录,建议同样使用无色。
    }//Enum在Android下似乎没法设置值。

    public CurrentState() {
    }

    public CurrentState(int colorResId, short remainingMinutes, short remainingHours) {
        this.colorResId = colorResId;
        this.remainingMinutes = remainingMinutes;
        this.remainingHours = remainingHours;
    }

    public int getColorResId() {
        return colorResId;
    }

    public void setColorResId(int colorResId) {
        this.colorResId = colorResId;
    }

    public short getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(short remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public short getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(short remainingHours) {
        this.remainingHours = remainingHours;
    }

    public short getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(short remainingDays) {
        this.remainingDays = remainingDays;
    }


    /*
    * 以下是Parcelable要求的内容
    * */
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(colorResId);
        parcel.writeInt((int)remainingMinutes);
        parcel.writeInt((int)remainingHours);
        parcel.writeInt((int)remainingDays);
    }

    public static final Parcelable.Creator<CurrentState> CREATOR = new Parcelable.Creator<CurrentState>(){
        @Override
        public CurrentState createFromParcel(Parcel parcel) {
            return new CurrentState(parcel);
        }

        @Override
        public CurrentState[] newArray(int size) {
            return new CurrentState[size];
        }
    };

    private CurrentState(Parcel in){
        colorResId = in.readInt();
        remainingMinutes = (short) in.readInt();
        remainingHours = (short) in.readInt();
        remainingDays = (short) in.readInt();
    }


}
