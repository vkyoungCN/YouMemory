package com.vkyoungcn.learningtools.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.MissionDetailActivity;
import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.Mission;

import java.util.List;

/*
* 在主页以列表（Rv）展示所有任务的集合信息（暂只展示名称，其余信息跳转到详情页展示）
* 条目有点击事件，点击后跳转到相应任务的详情页MissionDetailActivity。
* */
public class AllMissionRvAdapter extends RecyclerView.Adapter<AllMissionRvAdapter.ViewHolder>{
//    private static final String TAG = "AllMissionRvAdapter";
    private List<Mission> missions;//本页暂时只显示titles，但后续页面需要suffix，点击事件需要相应id
    private Context context;//用于点击事件的启动新Activity

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView title;

        private ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            itemView.setOnClickListener(this);
        }

        public TextView getTitle() {
            return title;
        }

        @Override
        public void onClick(View view) {
            int position  =getAdapterPosition();
            if (position != RecyclerView.NO_POSITION){ // Check if an item was deleted, but the user clicked it before the UI removed it

                //跳转到任务详情页Activity，直接传递Mission（原方案传递M-id再行获取）
                Intent intent = new Intent(context, MissionDetailActivity.class);
                intent.putExtra("Mission",missions.get(position));
                context.startActivity(intent);
            }
        }
    }

    public AllMissionRvAdapter(List<Mission> missions, Context context) {
        this.missions = missions;
        this.context = context;
    }

    @Override
    public AllMissionRvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_row_all_missions, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Mission mission = missions.get(position);
        holder.getTitle().setText(mission.getName());
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }
}
