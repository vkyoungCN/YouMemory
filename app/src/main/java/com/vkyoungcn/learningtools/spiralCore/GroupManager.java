package com.vkyoungcn.learningtools.spiralCore;

import android.content.Context;

import com.vkyoungcn.learningtools.models.CurrentState;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.UIGroup;
import com.vkyoungcn.learningtools.models.LogModel;

import java.util.ArrayList;
import java.util.List;

/*
* 负责从Adapter（数据源）取到原始的DB-Group数据，转换成适合UI使用的Group数据
* */
public class GroupManager {
    private Context context = null;

    public GroupManager(Context context) {
        this.context = context;
    }

    /*将从DB读取到的List<DBRawGroup>记录，转换成UI适用的List<UIGroup>*/
    //此方法没有更新，缺少字段
    public List<UIGroup> dBRawToNormalGroup(List<DBRwaGroup> dbRwaGroups){
        List<UIGroup> normalGroups = new ArrayList<>();

        for (DBRwaGroup g:dbRwaGroups) {
            UIGroup normalGroup= new UIGroup();

            normalGroup.setId(g.getId());
            normalGroup.setDescription(g.getDescription());
            normalGroup.setSpecial_mark(g.getSpecial_mark());


            //由于SQLite实际将DATE数据存做TEXT（或REAL/INTEGER），所以直接以String数据存入DB，需转换。
            List<LogModel> lm = LogList.textListLogToListLog(g.getGroupLogs());
            normalGroup.setGroupLogs(lm);

            CurrentState cs = new CurrentState();
            LogList.setCurrentStateForGroup(cs,lm);//根据当前的log记录以及当前时间，计算当前的状态
            normalGroup.setGroupCurrentState(cs);

            List<Integer> l = groupSubItemIdsStringToListInt(g.getSubItems_ids());
            normalGroup.setSubItemsTotalNumber(l.size());

            normalGroups.add(normalGroup);
        }

        return normalGroups;
    }

    //DB记录中的ids是String形式的，处理成List形式。
    //原始记录中是以英文分号分隔的
    private List<Integer> groupSubItemIdsStringToListInt(String stringIds){
        List<Integer> list = new ArrayList<>();

        String[] idsInString = stringIds.split(";");
        for (String s:idsInString) {
            list.add(Integer.parseInt(s));
        }
        return list;
    }

}
