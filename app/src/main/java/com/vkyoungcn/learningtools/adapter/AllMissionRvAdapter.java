package com.vkyoungcn.learningtools.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.R;

import java.util.List;

public class AllMissionRvAdapter extends RecyclerView.Adapter<AllMissionRvAdapter.ViewHolder> {
    private static final String TAG = "AllMissionRvAdapter";
    private List<String> missionTitles;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView titel;

        public ViewHolder(View itemView) {
            super(itemView);

            Log.i(TAG, "ViewHolder: constructor");
            titel = itemView.findViewById(R.id.title);
        }

        public TextView getTitel() {
            return titel;
        }
    }

    /*
    * 构造器，初始化此适配器的数据源
    * */

    public AllMissionRvAdapter(List<String> missionTitles) {
        this.missionTitles = missionTitles;
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
        String title = missionTitles.get(position);
        holder.getTitel().setText(title);
    }

    @Override
    public int getItemCount() {
        return missionTitles.size();
    }
}
