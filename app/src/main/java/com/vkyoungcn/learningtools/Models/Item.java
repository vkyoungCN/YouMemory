package com.vkyoungcn.learningtools.models;

import java.util.Date;
import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class Item {
    private int id;
    private int name;
    private String extending_list_1;
    private String extending_list_2;
    private List<Date> picking_log; //对应item-pickingTime交叉表；


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
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

    public List<Date> getPicking_log() {
        return picking_log;
    }

    public void setPicking_log(List<Date> picking_log) {
        this.picking_log = picking_log;
    }
}
