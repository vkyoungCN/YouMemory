package com.vkyoungcn.learningtools.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.R;

/**
 * 刪除group的确认对话框
 */
public class ConfirmDeletingDiaFragment extends DialogFragment implements View.OnClickListener{
    private int position;// 用于回传。
    private OnDeletingGroupDfgClickListener mListener;

    public ConfirmDeletingDiaFragment() {
        // Required empty public constructor
    }

    public static ConfirmDeletingDiaFragment newInstance(int position) {
        ConfirmDeletingDiaFragment fragment = new ConfirmDeletingDiaFragment();
        Bundle args = new Bundle();
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
        View rootView = inflater.inflate(R.layout.confirm_deleting_group_dialog_fragment, container, false);
        rootView.findViewById(R.id.deletingGroupDfg_confirm).setOnClickListener(this);
        rootView.findViewById(R.id.deletingGroupDfg_cancel).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.deletingGroupDfg_confirm:
                //任务交由调用本dfg的activity进行。
                mListener.onDeletingConfirmClick(position);
                this.dismiss();
                break;
            case R.id.deletingGroupDfg_cancel:
                this.dismiss();

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeletingGroupDfgClickListener) {
            mListener = (OnDeletingGroupDfgClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDeletingGroupDfgClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnDeletingGroupDfgClickListener {
        void onDeletingConfirmClick(int position);
    }
}
