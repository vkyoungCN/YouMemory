package com.vkyoungcn.learningtools.spiralCore;

import android.content.Context;
import android.util.Log;

import com.vkyoungcn.learningtools.models.GroupState;
import com.vkyoungcn.learningtools.models.RvGroup;

import java.util.ArrayList;
import java.util.List;


/*
* 职责：①从Adapter（数据源）取到原始的DB-Group数据，转换成适合UI使用的Group数据
* ②分组所属项目的列表（在字串形式和List<Integer>形式间）的转换。
* ③刷新当前任务下所属分组的情况。
* */
@SuppressWarnings("all")
public class GroupManager {
    private static final String TAG = "GroupManager";
    private Context context = null;

    //另，原计划增加枚举类：分组状态，用以专门性的标识分组状态（目前是以colorId替代的）；
    // 后来认为没有必要。
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
        GroupState.stateNumber stateNumber = groupState.getState();
        StringBuilder sbf = new StringBuilder();

        switch (stateNumber){
            case NOT_YET:
                sbf.append("未到复习时间,还有");
                if(groupState.getRemainingDays()!=0){
                    sbf.append(groupState.getRemainingDays());
                    sbf.append("天 ");
                }
                if(groupState.getRemainingHours()!=0){
                    sbf.append(groupState.getRemainingHours());
                    sbf.append("小时 ");
                    if(groupState.getRemainingDays()!=0) break;//剩余1天以上，记完小时后跳出(不记分钟)
                }
                if(groupState.getRemainingMinutes()!=0) {
                    sbf.append(groupState.getRemainingMinutes());
                }
                sbf.append("分");
                break;

            case AVAILABLE:
            case MISSED_ONCE:
                sbf.append("请在 ");
                if(groupState.getRemainingDays()!=0){
                    sbf.append(groupState.getRemainingDays());
                    sbf.append("天 ");
                }
                if(groupState.getRemainingHours()!=0){
                    sbf.append(groupState.getRemainingHours());
                    sbf.append("小时 ");
                    if(groupState.getRemainingDays()!=0) {
                        sbf.append(" 内完成复习");
                        break;//剩余1天以上，记完小时后跳出(不记分钟)
                    }
                }
                if(groupState.getRemainingMinutes()!=0){
                    sbf.append(groupState.getRemainingMinutes());
                }

                sbf.append("分钟 内完成复习");
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

    public static String subItemIdsListIntToString(List<Integer> idsList){

        StringBuilder sbIds = new StringBuilder();
        for (int i :idsList) {
            sbIds.append(i);
            sbIds.append(";");
        }
        return sbIds.toString();
    }

    public static int getItemAmountFromSubItemStr(String subItemIdsStr){
        String[] subItemsStrArray = subItemIdsStr.split(";");
        return subItemsStrArray.length;
    }

    public static List<RvGroup> ascOrderByRemainingTime(List<RvGroup> rvGroups){
        List<RvGroup> resultRvGroups = new ArrayList<>();

        //此排序属何种算法【？我非科班游击队基本功不行啊】
        for (int i = 0; i < rvGroups.size(); ) {//不能i++，但size每次减少1。
            RvGroup minRvGroup = rvGroups.get(i);//即使用new也是指针形式，最后都是重复数据（且提示new无意义）

            for (int j = 1; j < rvGroups.size(); j++) {
                //计算指针项的值
                RemainingTimeAmount remainingTimeAmountMinPointer = LogList.getCurrentRemainingTimeForGroup(minRvGroup.getStrGroupLogs());
                long remainingMinutesMinPoint = RemainingTimeAmount.getRemainingTimeInMinutes(remainingTimeAmountMinPointer);

                RemainingTimeAmount remainingTimeAmountJ = LogList.getCurrentRemainingTimeForGroup(rvGroups.get(j).getStrGroupLogs());
                long remainingMinutesJ = RemainingTimeAmount.getRemainingTimeInMinutes(remainingTimeAmountJ);

                if(remainingMinutesJ<remainingMinutesMinPoint){
                    minRvGroup = rvGroups.get(j);//指针指向较小者
                }
            }
            rvGroups.remove(rvGroups.indexOf(minRvGroup));//将最小的删除
            try {
                RvGroup gp = (RvGroup) minRvGroup.clone();//克隆方式复制。
                resultRvGroups.add(gp);//一遍检索的最小值加入结果list。
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return resultRvGroups;
    }

}
