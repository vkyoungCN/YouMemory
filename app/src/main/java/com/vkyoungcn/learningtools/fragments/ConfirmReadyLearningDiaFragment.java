package com.vkyoungcn.learningtools.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.R;

/**
 * 开始学习前弹出的对话框，确认键后进入学习Activity
 *
 */
public class ConfirmReadyLearningDiaFragment extends DialogFragment implements View.OnClickListener{
    private static final String TAG = "ConfirmReadyLearningDia";
    private int position;// 回传时，更新指定条目的状态。
    private OnConfirmClick mListener;

    public ConfirmReadyLearningDiaFragment() {
        // Required empty public constructor
    }

    public static ConfirmReadyLearningDiaFragment newInstance(long groupId, String tableNameSuffix,String groupSubItemIdsStr,int stateColors,int position) {
        ConfirmReadyLearningDiaFragment fragment = new ConfirmReadyLearningDiaFragment();
        Bundle args = new Bundle();
        args.putInt("learning_type",stateColors);
        args.putInt("position",position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //只有position参数是需要回送给Activity（用于打开新Activity时结合数据源加载正确的group项）
        if (getArguments() != null) {
            position = getArguments().getInt("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.confirm_ready_learning_dialog_fragment, container, false);
        rootView.findViewById(R.id.ready_learningDfg_confirm).setOnClickListener(this);
        rootView.findViewById(R.id.ready_learningDfg_cancel).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ready_learningDfg_confirm:
                //开启新Activity的任务交由调用本dfg的activity进行。
                mListener.onConfirmClick(position);
                this.dismiss();
                break;
            case R.id.ready_learningDfg_cancel:
                this.dismiss();

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConfirmClick) {
            mListener = (OnConfirmClick) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConfirmClick");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnConfirmClick {
        void onConfirmClick(int position);
    }
}
