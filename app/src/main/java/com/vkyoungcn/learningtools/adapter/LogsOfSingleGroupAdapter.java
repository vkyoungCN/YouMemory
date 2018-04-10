package com.vkyoungcn.learningtools.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.LogModel;

import java.text.SimpleDateFormat;
import java.util.List;

public class LogsOfSingleGroupAdapter extends RecyclerView.Adapter<LogsOfSingleGroupAdapter.ViewHolder> {
    private static final String TAG = "LogsOfSingleGroupAdapter";

    private String[] strGroupLogs;//【这里初始化无意义的，如果传入的是null一样挂。已出错记录】

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView logs_num;
        private final TextView logs_time;
        private final TextView log_isMiss;

        public ViewHolder(View itemView) {
            super(itemView);
//            Log.i(TAG, "ViewHolder: be");
            logs_num = itemView.findViewById(R.id.rv_logs_num);
            logs_time = itemView.findViewById(R.id.rv_logs_time);
            log_isMiss = itemView.findViewById(R.id.rv_logs_miss);

        }

        public TextView getLogs_num() {
            return logs_num;
        }

        public TextView getLogs_time() {
            return logs_time;
        }

        public TextView getLog_isMiss() {
            return log_isMiss;
        }
    }

    public LogsOfSingleGroupAdapter(String strGroupLogsInOne) {
        this.strGroupLogs = strGroupLogsInOne.split(";");

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Log.i(TAG, "onBindViewHolder: be");
        String strSingleLog = strGroupLogs[position];
        String[] strLogSection = strSingleLog.split("#");

        holder.getLogs_num().setText(strLogSection[0]);
        holder.getLogs_time().setText(strLogSection[1]);
        holder.getLog_isMiss().setText(strLogSection[2]);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_logs_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return strGroupLogs.length;
    }
}
