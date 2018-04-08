package com.vkyoungcn.learningtools.models;

import java.util.Date;
import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class Item {
    private int id =0;
    private String name="";
    private String extending_list_1="";
    private String extending_list_2="";
    private Boolean isChose=false;


    public Item() {
    }

    public Item(int id, String name, String extending_list_1, String extending_list_2, Boolean isChose ) {
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

}
