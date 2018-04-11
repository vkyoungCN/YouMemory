package com.vkyoungcn.learningtools.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class Mission implements Parcelable {
    private int id=0;
    private String name="";
    private String description="";
    private String tableItem_suffix="";



    private List<Integer> subGroups_ids;

    //空构造器
    public Mission() {
    }

    public Mission(String name) {
        this.name = name;
    }

    //三字串构造器
    public Mission(String name, String description, String tableItem_suffix) {
        this.name = name;
        this.description = description;
        this.tableItem_suffix = tableItem_suffix;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getSubGroups_ids() {
        return subGroups_ids;
    }

    public void setSubGroups_ids(List<Integer> subGroups_ids) {
        this.subGroups_ids = subGroups_ids;
    }

    public String getTableItem_suffix() {
        return tableItem_suffix;
    }

    public void setTableItem_suffix(String tableItem_suffix) {
        this.tableItem_suffix = tableItem_suffix;
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
        parcel.writeString(description);
        parcel.writeString(tableItem_suffix);
    }

    public static final Parcelable.Creator<Mission> CREATOR = new Parcelable.Creator<Mission>(){
        @Override
        public Mission createFromParcel(Parcel parcel) {
            return new Mission(parcel);
        }

        @Override
        public Mission[] newArray(int size) {
            return new Mission[size];
        }
    };

    private Mission(Parcel in){
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        tableItem_suffix = in.readString();
    }

}
