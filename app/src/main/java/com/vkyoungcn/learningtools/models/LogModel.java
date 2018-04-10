package com.vkyoungcn.learningtools.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
* 对应各组任务的一次学习和复习的记录
* 可以整理成简单的字串形式"N#YYYY-MM-DD hh:mm:ss#false;"
* 记录内使用#分隔，每条记录以英文分号结尾。
*
* 注意，即时某次复习缺失，其记录次数也不应删除，否则影响计算。
* */
public class LogModel implements Parcelable {
    private short n=0;//本次记忆对应的次数；其中初次记忆=0，第n次复习= n。
    private long timeInMilli =0;//本次记忆对应的完成时间
    private boolean isMiss = false;//本次记忆是否未在规定时限内完成（未完成=miss=true）。

    public LogModel() {
    }

    public LogModel(String strLog) {
        if(strLog==null||strLog.isEmpty()){
            return;
        }
        String[] logSection = strLog.split("#");
        this.n = Short.valueOf(logSection[0]);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");

        try {
            Date date  = simpleDateFormat.parse(logSection[1]);
            this.timeInMilli = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.isMiss = logSection[2].equals("true");

    }

    public LogModel(short n, long timeInMilli, boolean isMiss) {
        this.n = n;
        this.timeInMilli = timeInMilli;
        this.isMiss = isMiss;
    }

    public short getN() {
        return n;
    }

    public void setN(short n) {
        this.n = n;
    }

    public long getTimeInMilli() {
        return timeInMilli;
    }

    public void setTimeInMilli(long timeInMilli) {
        this.timeInMilli = timeInMilli;
    }

    public boolean isMiss() {
        return isMiss;
    }

    public void setMiss(boolean miss) {
        this.isMiss = miss;
    }


    /*
     * 以下是Parcelable要求的内容
     * */
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(n);
        parcel.writeLong(timeInMilli);
        parcel.writeByte((byte)( isMiss?1:0));
    }

    public static final Parcelable.Creator<LogModel> CREATOR = new Parcelable.Creator<LogModel>(){
        @Override
        public LogModel createFromParcel(Parcel parcel) {
            return new LogModel(parcel);
        }

        @Override
        public LogModel[] newArray(int size) {
            return new LogModel[size];
        }
    };

    private LogModel(Parcel in){
        n = (short) in.readInt();
        timeInMilli = in.readLong();
        isMiss = in.readByte()==1;
    }

}
