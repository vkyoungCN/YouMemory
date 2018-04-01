package com.vkyoungcn.learningtools.models;

import java.util.Date;
import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class Group {
    private int id;
    private int times_picked;
    private int special_mark;
    private String description;
    private Date init_pick_time;
    private Date last_pick_time;
    private List<Integer> subItems_ids;

    /*
    * 构造器1，指定两个最基础的参数：
    * 本组建立时间(可作为唯一区分)；
    * */
    public Group(Date init_pick_time, List<Integer> subItems_ids) {
        this.init_pick_time = init_pick_time;
        this.subItems_ids = subItems_ids;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimes_picked() {
        return times_picked;
    }

    public void setTimes_picked(int times_picked) {
        this.times_picked = times_picked;
    }

    public int getSpecial_mark() {
        return special_mark;
    }

    public void setSpecial_mark(int special_mark) {
        this.special_mark = special_mark;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getInit_pick_time() {
        return init_pick_time;
    }

    public void setInit_pick_time(Date init_pick_time) {
        this.init_pick_time = init_pick_time;
    }

    public Date getLast_pick_time() {
        return last_pick_time;
    }

    public void setLast_pick_time(Date last_pick_time) {
        this.last_pick_time = last_pick_time;
    }

    public List<Integer> getSubItems_ids() {
        return subItems_ids;
    }

    public void setSubItems_ids(List<Integer> subItems_ids) {
        this.subItems_ids = subItems_ids;
    }
}
