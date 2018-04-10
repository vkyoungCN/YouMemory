package com.vkyoungcn.learningtools.spiralCore;

import android.content.Context;
import android.util.Log;

import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.GroupState;
import com.vkyoungcn.learningtools.models.UIGroup;
import com.vkyoungcn.learningtools.models.LogModel;

import java.util.ArrayList;
import java.util.List;


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
    public static String getCurrentStateTimeAmountStringFromUIGroup(GroupState groupState){
        Log.i(TAG, "getCurrentStateTimeAmountString: be");
        GroupState.stateNumber stateNumber = groupState.getState();

        StringBuilder sbf = new StringBuilder();

        switch (stateNumber){
            case NOT_YES:
//                Log.i(TAG, "getCurrentStateTimeAmountString: color not");
                sbf.append("未到复习时间 -");
                if(groupState.getRemainingDays()!=0){
                    sbf.append(groupState.getRemainingDays());
                    sbf.append("天 ");
                }
                if(groupState.getRemainingHours()!=0){
                    sbf.append(groupState.getRemainingHours());
                    sbf.append("小时 ");
                }
                if(groupState.getRemainingMinutes()!=0) {
                    sbf.append(groupState.getRemainingMinutes());
                }
                sbf.append("分");
                break;

            case AVAILABLE:
            case MISSED_ONCE:
                sbf.append("请在 -");
                if(groupState.getRemainingDays()!=0){
                    sbf.append(groupState.getRemainingDays());
                    sbf.append("天 ");
                }
                if(groupState.getRemainingHours()!=0){
                    sbf.append(groupState.getRemainingHours());
                    sbf.append("小时 ");
                }
                if(groupState.getRemainingMinutes()!=0){
                    sbf.append(groupState.getRemainingMinutes());
                }

                sbf.append("分 内完成复习");
                break;
            case MISSED_TWICE:
                sbf.append("复习间隔过久，请重新开始");
                break;
            case ACCOMPLISHED:
                sbf.append("成功上岸");
                break;
            case NEWLY_CREATED:
                sbf.append("新任务，请尽快学习。");
                break;

        }
        return sbf.toString();
    }

}
