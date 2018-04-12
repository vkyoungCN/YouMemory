package com.vkyoungcn.learningtools.adapter;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import com.vkyoungcn.learningtools.models.GroupState;
import com.vkyoungcn.learningtools.models.RvGroup;
import com.vkyoungcn.learningtools.spiralCore.GroupManager;
import com.vkyoungcn.learningtools.spiralCore.LogList;

import java.util.List;

public class GroupsOfMissionRvAdapter extends RecyclerView.Adapter<GroupsOfMissionRvAdapter.ViewHolder> {
    private static final String TAG = "AllMissionRvAdapter";

    private List<RvGroup> groups;
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
            int position  =getAdapterPosition();
            if (position == RecyclerView.NO_POSITION){return;}// Check if an item was deleted, but the user clicked it before the UI removed it


            switch (view.getId()){
                case R.id.rv_group_detail:
                    int groupId = groups.get(position).getId();

                    //启动GroupDetailActivity，根据missionId进行获取填充。
                        Intent intent = new Intent(context, GroupDetailActivity.class);
                        intent.putExtra("GroupId",groupId);
                        intent.putExtra("TableSuffix",itemTableSuffix);
                        Log.i(TAG, "onClick: ready to GroupDetailActivity.");
                        context.startActivity(intent);

                    break;
                case R.id.current_state://点击了状态TextView大色块，进行学习/复习
                    //根据currentState的情况有不同处理逻辑
                    //①绿：弹出确认框：要开始学习吗？否返回，是进入学习页（新学习逻辑）
                    // ②蓝、橙：直接进入复习（不确认，反正可退出），是复习逻辑；
                    //   在完成时检查log情况，生成一条或两条。
                    //③红色：提示框，由用户选择是重置为新组，还是删除。
                    //④灰、无色：不执行动作。

                    int groupStateColorRes = groups.get(position).getStateColorResId();
                    switch (groupStateColorRes){
                        case R.color.colorGP_Newly:
                            //绿色，开始学习。弹出确认框。
                            FragmentTransaction transaction = System.getFragmentManager().beginTransaction();
                            Fragment prev = getFragmentManager().findFragmentByTag("CREATE_GROUP");

                            if (prev != null) {
                                Log.i(TAG, "inside showDialog(), inside if prev!=null branch");
                                transaction.remove(prev);
                            }
                            DialogFragment dfg = CreateGroupDiaFragment.newInstance(missionFromIntent.getTableItem_suffix(), missionFromIntent.getId());
//        Log.i(TAG, "createGroup: before show.");
                            dfg.show(transaction, "CREATE_GROUP");

                            break;
                        case R.color.colorGP_STILL_NOT:

                            break;
                        case 0:

                            break;
                        case R.color.colorGP_AVAILABLE:

                            break;
                        case R.color.colorGP_Miss_ONCE:

                            break;
                        case R.color.colorGP_Miss_TWICE:

                            break;



                    }


                    break;

            }
        }
    }

    /*
    * 构造器，初始化此适配器的数据源
    * */
    public GroupsOfMissionRvAdapter(List<com.vkyoungcn.learningtools.models.RvGroup> groups, Context context, String itemTableSuffix ) {
//        Log.i(TAG, "GroupsOfMissionRvAdapter: constructor");

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
        RvGroup group = groups.get(position);
//        Log.i(TAG, "onBindViewHolder: got group,id:"+group.getId());
        holder.getGroup_id().setText(String.valueOf(group.getId()));
        holder.getGroup_description().setText(group.getDescription());
        holder.getItem_amount().setText(String.valueOf(group.getTotalSubItemsNumber()));

        holder.getCurrent_state_time().setText(group.getStateText());
        holder.getCurrent_state_time().setBackgroundResource(group.getStateColorResId());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }



}
