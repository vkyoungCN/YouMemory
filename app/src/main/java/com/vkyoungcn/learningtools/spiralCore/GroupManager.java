package com.vkyoungcn.learningtools.spiralCore;

import android.content.Context;
import android.util.Log;

import com.vkyoungcn.learningtools.models.CurrentState;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.UIGroup;
import com.vkyoungcn.learningtools.models.LogModel;

import java.util.ArrayList;
import java.util.List;

import static com.vkyoungcn.learningtools.models.CurrentState.ColorResId.*;

/*
* 职责：①从Adapter（数据源）取到原始的DB-Group数据，转换成适合UI使用的Group数据
* ②分组所属项目的列表（在字串形式和List<Integer>形式间）的转换。
* ③刷新当前任务下所属分组的情况。
* */
public class GroupManager {
    private static final String TAG = "GroupManager";
    private Context context = null;

    public static final class GroupSpecialMarks {
        public static final int NORMAL_GROUP = 0;
        public static final int SPLIT = 1;
        public static final int FALL_BEHIND_SPLIT = 2;
        public static final int OBSOLETED =3;
    }

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
            normalGroup.setFallBehind(g.isFallBehind());


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


    /*
    * 基于UIGroup的CS提供相应的显示字串
    * */
    public static String getCurrentStateTimeAmountStringFromUIGroup(UIGroup group){
        Log.i(TAG, "getCurrentStateTimeAmountString: be");
        CurrentState gcs = group.getGroupCurrentState();
        StringBuilder sbf = new StringBuilder();

        switch (gcs.getColorResId()){
            case COLOR_STILL_NOT:
//                Log.i(TAG, "getCurrentStateTimeAmountString: color not");
                sbf.append("未到复习时间 -");
                if(gcs.getRemainingDays()!=0){
                    sbf.append(gcs.getRemainingDays());
                    sbf.append("天 ");
                }
                if(gcs.getRemainingHours()!=0){
                    sbf.append(gcs.getRemainingHours());
                    sbf.append("小时 ");
                }
                if(gcs.getRemainingMinutes()!=0) {
                    sbf.append(gcs.getRemainingMinutes());
                }
                sbf.append("分");
                break;

            case COLOR_AVAILABLE:
            case COLOR_MISSED_ONCE:
                sbf.append("请在 -");
                if(gcs.getRemainingDays()!=0){
                    sbf.append(gcs.getRemainingDays());
                    sbf.append("天 ");
                }
                if(gcs.getRemainingHours()!=0){
                    sbf.append(gcs.getRemainingHours());
                    sbf.append("小时 ");
                }
                if(gcs.getRemainingMinutes()!=0){
                    sbf.append(gcs.getRemainingMinutes());
                }

                sbf.append("分 内完成复习");
                break;
            case COLOR_MISSED_TWICE:
                sbf.append("复习间隔过久，请重新开始");
                break;
            case COLOR_FULL:
                sbf.append("成功上岸");
                break;
            case COLOR_NEWLY:
                sbf.append("新任务，请尽快学习。");
                break;

        }
        return sbf.toString();
    }

}
