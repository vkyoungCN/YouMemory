package com.vkyoungcn.learningtools.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vkyoungcn.learningtools.MissionDetailActivity;
import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.Mission;

import java.util.List;

/*
* 在主页以列表（Rv）展示所有任务的集合信息（暂只展示名称，其余信息跳转到详情页展示）
* 条目有点击事件，点击后跳转到相应任务的详情页MissionDetailActivity。
* */
public class AllMissionRvAdapter extends RecyclerView.Adapter<AllMissionRvAdapter.ViewHolder> implements View.OnClickListener {
    private static final String TAG = "AllMissionRvAdapter";
    private List<Mission> missions;//虽然显示只要titles，但点击事件需要相应id，所以必须是List<Mission>。
    private RecyclerView mRv;
    private Context context;//用于点击事件的启动新Activity

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
//            Log.i(TAG, "ViewHolder: constructor");
            title = itemView.findViewById(R.id.title);

        }

        public TextView getTitle() {
            return title;
        }


    }

    /*
    * 构造器，初始化此适配器的数据源
    * */

    public AllMissionRvAdapter(List<Mission> missions, Context context) {
        this.missions = missions;
        this.context = context;
    }

    @Override
    public AllMissionRvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.i(TAG, "onCreateViewHolder: before any.");

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_row_all_missions, parent, false);
//        Log.i(TAG, "onCreateViewHolder: after inflate");

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Log.i(TAG, "onBindViewHolder: before any.");
        Mission mission = missions.get(position);
        holder.getTitle().setText(mission.getName());
        holder.getTitle().setOnClickListener(this);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRv = recyclerView;//获取Rv引用，用于获取点击事件的位置，比较绕；
        // 因为VH中不能访问非静态变量（数据源）；这可能是唯一能将监听器设计在adp内的方式。
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    @Override
    public void onClick(View view) {
//        Log.i(TAG, "onClick: before any");
        int viewPosition =  mRv.getChildAdapterPosition((FrameLayout)view.getParent());
//        Log.i(TAG, "onClick: viewPosition: "+viewPosition);
        int missionId = missions.get(viewPosition).getId();

        //启动MissionMainActivity，根据missionId进行获取填充。
        Intent intent = new Intent(context, MissionDetailActivity.class);
        intent.putExtra("MissionId",missionId);
        Log.i(TAG, "onClick: ready to new activity.");
        context.startActivity(intent);
    }
}
