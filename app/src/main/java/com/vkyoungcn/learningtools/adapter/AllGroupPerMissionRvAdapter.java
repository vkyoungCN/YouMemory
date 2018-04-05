package com.vkyoungcn.learningtools.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.CurrentState;
import com.vkyoungcn.learningtools.models.UIGroup;

import java.util.List;

public class AllGroupPerMissionRvAdapter extends RecyclerView.Adapter<AllGroupPerMissionRvAdapter.ViewHolder> {
    private static final String TAG = "AllMissionRvAdapter";
    private List<UIGroup> groups;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView group_id;
        private final TextView group_description;//对应DB中的Description列，“起始-末尾”词汇。
        private final TextView item_amount;//所属item的数量
        private final TextView current_state_time;//字面显示（剩余）时间.底色显示状态。
        // ...测验分综合获得，由专门package类提供

        public ViewHolder(View itemView) {
            super(itemView);
//            Log.i(TAG, "ViewHolder: constructor");

            group_id = itemView.findViewById(R.id.group_id);
            group_description = itemView.findViewById(R.id.group_description);
            item_amount = itemView.findViewById(R.id.item_amount);
            current_state_time = itemView.findViewById(R.id.current_state);
//            times_past = itemView.findViewById(R.id.times_past);改成Button。

        }

        public TextView getGroup_id() {
            return group_id;
        }

        public TextView getGroup_description() {
            return group_description;
        }

        public TextView getItem_amount() {
            return item_amount;
        }

        public TextView getCurrent_state_time() {
            return current_state_time;
        }
    }

    /*
    * 构造器，初始化此适配器的数据源
    * */
    public AllGroupPerMissionRvAdapter(List<UIGroup> groups) {
        this.groups = groups;
    }

    @Override
    public AllGroupPerMissionRvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: before any.");

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_row_groups_in_single_mission, parent, false);
//        Log.i(TAG, "onCreateViewHolder: after inflate");

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: before any.");
        UIGroup group = groups.get(position);
        Log.i(TAG, "onBindViewHolder: got group,id:"+group.getId());
        holder.getGroup_id().setText(String.valueOf(group.getId()));
        holder.getGroup_description().setText(group.getDescription());
        holder.getItem_amount().setText(String.valueOf(group.getSubItemsTotalNumber()));

        holder.getCurrent_state_time().setText(getCurrentStateTimeAmountString(group));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }


    private String getCurrentStateTimeAmountString(UIGroup group){
        Log.i(TAG, "getCurrentStateTimeAmountString: be");
        CurrentState gcs = group.getGroupCurrentState();
        StringBuilder sbf = new StringBuilder();

        switch (gcs.getColor()){
            case COLOR_STILL_NOT:
                Log.i(TAG, "getCurrentStateTimeAmountString: color not");
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
                sbf.append("请重新开始");
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
