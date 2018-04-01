package com.vkyoungcn.learningtools.models;

import java.util.Date;
import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class Item {
    private int id;
    private int name;
    private List<String> extending_list;
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

    public List<String> getExtending_list() {
        return extending_list;
    }

    public void setExtending_list(List<String> extending_list) {
        this.extending_list = extending_list;
    }

    public List<Date> getPicking_log() {
        return picking_log;
    }

    public void setPicking_log(List<Date> picking_log) {
        this.picking_log = picking_log;
    }
}
