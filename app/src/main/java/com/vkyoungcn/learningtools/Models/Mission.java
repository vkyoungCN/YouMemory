package com.vkyoungcn.learningtools.models;

import java.util.List;

/**
 * Created by VkYoung16 on 2018/3/26 0026.
 */

public class Mission {
    private int db_id;
    private String name;
    private String description;
    private String tableItem_suffix;



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

    public int getDb_id() {
        return db_id;
    }

    public void setDb_id(int db_id) {
        this.db_id = db_id;
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
}
