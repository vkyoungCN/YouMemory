package com.vkyoungcn.learningtools;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.vkyoungcn.learningtools.adapter.LogsOfSingleGroupAdapter;
import com.vkyoungcn.learningtools.models.LogModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Use the {@link ShowLogsOfGroupDiaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowLogsOfGroupDiaFragment extends DialogFragment {
    private static final String TAG = "ShowLogsOfGroupDiaFragm";
    private static final String ARG_PARAM_Logs = "LOGS";
    private String strGroupLogs;
    private RecyclerView logRv;


    public ShowLogsOfGroupDiaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ShowLogsOfGroupDiaFragment.
     */
    public static ShowLogsOfGroupDiaFragment newInstance(String strGroupLogs) {
        ShowLogsOfGroupDiaFragment fragment = new ShowLogsOfGroupDiaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_Logs, strGroupLogs);
//        Log.i(TAG, "newInstance: put");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            strGroupLogs = getArguments().getString(ARG_PARAM_Logs);
//            Log.i(TAG, "onCreate: get");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.show_logs_of_group_dfg, container, false);

        //通过调整外层VG的大小将dialogFg的宽度设置为90%，高度设为屏幕可用部分的85%。
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
