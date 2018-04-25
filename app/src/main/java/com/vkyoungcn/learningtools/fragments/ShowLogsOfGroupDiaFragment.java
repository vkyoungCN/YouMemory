package com.vkyoungcn.learningtools.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.adapter.LogsOfSingleGroupAdapter;

import java.util.ArrayList;


public class ShowLogsOfGroupDiaFragment extends DialogFragment {
    private static final String TAG = "ShowLogsOfGroupDiaFragm";
    private static final String STRING_GROUP_LOGS = "LOGS";
    private String strGroupLogs;
    private RecyclerView logRv;


    public ShowLogsOfGroupDiaFragment() {
        // Required empty public constructor
    }


    public static ShowLogsOfGroupDiaFragment newInstance(String strGroupLogs) {
        ShowLogsOfGroupDiaFragment fragment = new ShowLogsOfGroupDiaFragment();
        Bundle args = new Bundle();
        args.putString(STRING_GROUP_LOGS, strGroupLogs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            strGroupLogs = getArguments().getString(STRING_GROUP_LOGS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.show_logs_of_group_dfg, container, false);

        //通过调整外层VG的大小将dialogFg的宽度设置为75%，高度设为屏幕可用部分的70%。
        LinearLayout llt = (LinearLayout) rootView.findViewById(R.id.frame_resize_logs_dfg);
        WindowManager appWm = (WindowManager) getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        try {
            appWm.getDefaultDisplay().getSize(point);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LinearLayout.LayoutParams gLp = new LinearLayout.LayoutParams((int)(point.x*0.75),(int)(point.y*0.7));
        llt.setLayoutParams(gLp);


        logRv =  (RecyclerView) rootView.findViewById(R.id.rv_logs_list);
        logRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        logRv.setAdapter(new LogsOfSingleGroupAdapter(strGroupLogs));
        logRv.setHasFixedSize(true);

        return rootView;

    }



}
