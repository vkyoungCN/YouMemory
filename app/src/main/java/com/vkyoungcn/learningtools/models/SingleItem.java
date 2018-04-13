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
    private Boolean isChose=false;


    public SingleItem() {
    }

    public SingleItem(int id, String name, String extending_list_1, String extending_list_2, Boolean isChose ) {
        this.id = id;
        this.name = name;
        this.extending_list_1 = extending_list_1;
        this.extending_list_2 = extending_list_2;
        this.isChose = isChose;
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

    }


}
