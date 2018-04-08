package com.vkyoungcn.learningtools.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.vkyoungcn.learningtools.GroupDetailActivity;
import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.UIGroup;
import com.vkyoungcn.learningtools.spiralCore.GroupManager;

import java.util.List;

public class GroupsOfMissionRvAdapter extends RecyclerView.Adapter<GroupsOfMissionRvAdapter.ViewHolder> {
    private static final String TAG = "AllMissionRvAdapter";
    private List<UIGroup> groups;
    private Context context;//用于启动新activity。
    private String itemTableSuffix;//用于点击条目查看分组所属items时

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView group_id;
        private final TextView group_description;//对应DB中的Description列，“起始-末尾”词汇。
        private final TextView item_amount;//所属item的数量
        private final TextView current_state_time;//字面显示（剩余）时间.底色显示状态。
        private final Button btn_group_detail;//绑定点击事件，进入group详情页。
        // ...测验分综合获得，由专门package类提供

        public ViewHolder(View itemView) {
            super(itemView);
//            Log.i(TAG, "ViewHolder: constructor");

            group_id = itemView.findViewById(R.id.group_id);
            group_description = itemView.findViewById(R.id.group_description);
            item_amount = itemView.findViewById(R.id.item_amount);
            current_state_time = itemView.findViewById(R.id.current_state);
            current_state_time.setOnClickListener(this);//
            btn_group_detail = itemView.findViewById(R.id.rv_group_detail);
            btn_group_detail.setOnClickListener(this);//VH 监听器方案下直接在这里绑定监听器。
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

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.rv_group_detail:
                    int position  =getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                        int groupId = groups.get(position).getId();

                        //启动GroupDetailActivity，根据missionId进行获取填充。
                        Intent intent = new Intent(context, GroupDetailActivity.class);
                        intent.putExtra("GroupId",groupId);
                        intent.putExtra("TableSuffix",itemTableSuffix);
                        Log.i(TAG, "onClick: ready to GroupDetailActivity.");
                        context.startActivity(intent);
                    }

                    break;
                case R.id.current_state:
                    //根据currentState的情况有不同处理逻辑
                    //①红、绿：弹出确认框：要开始学习吗？否返回，是进入学习页（新学习逻辑）
                    // ②蓝、橙：直接进入复习（不确认，反正可退出），是复习逻辑；
                    //   在橙色进入时检查log情况，如果上次未记录（且标miss）则记录之（在副线程进行）。
                    // ③灰、无色：不执行动作。


                    break;

            }
        }
    }

    /*
    * 构造器，初始化此适配器的数据源
    * */
    public GroupsOfMissionRvAdapter(List<UIGroup> groups, Context context, String itemTableSuffix ) {
        this.groups = groups;
        this.context = context;
        this.itemTableSuffix = itemTableSuffix;
    }

    @Override
    public GroupsOfMissionRvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.i(TAG, "onCreateViewHolder: before any.");

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_row_groups_in_single_mission, parent, false);
//        Log.i(TAG, "onCreateViewHolder: after inflate");

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Log.i(TAG, "onBindViewHolder: before any. group size = "+groups.size());
        UIGroup group = groups.get(position);
//        Log.i(TAG, "onBindViewHolder: got group,id:"+group.getId());
        holder.getGroup_id().setText(String.valueOf("#"+group.getId()));
        holder.getGroup_description().setText(group.getDescription());
        holder.getItem_amount().setText(String.valueOf("("+group.getSubItemsTotalNumber()+")"));

        holder.getCurrent_state_time().setText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(group));
        holder.getCurrent_state_time().setBackgroundResource(group.getGroupCurrentState().getColorResId());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }



}
