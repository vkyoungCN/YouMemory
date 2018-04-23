package com.vkyoungcn.learningtools.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.R;

/**
 * 一个纯粹的提示确认对话框
 */
public class ConfirmRemoveRedsDiaFragment extends DialogFragment implements View.OnClickListener{
    private static final String TAG = "ConfirmReadyLearningDia";

    private OnRemoveRedsConfirmClick mListener;

    public ConfirmRemoveRedsDiaFragment() {
        // Required empty public constructor
    }

    public static ConfirmRemoveRedsDiaFragment newInstance() {
        ConfirmRemoveRedsDiaFragment fragment = new ConfirmRemoveRedsDiaFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(TAG, "onCreate: b");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.i(TAG, "onCreateView: b");
        View rootView = inflater.inflate(R.layout.confirm_remove_reds_dialog_fragment, container, false);
        rootView.findViewById(R.id.removeRedsDfg_onfirm).setOnClickListener(this);
        rootView.findViewById(R.id.removeRedsDfg_cancel).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.removeRedsDfg_onfirm:
                mListener.onConfirmRemoveRedsClick();
                this.dismiss();
                break;
            case R.id.removeRedsDfg_cancel:
                this.dismiss();

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRemoveRedsConfirmClick) {
            mListener = (OnRemoveRedsConfirmClick) context;
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

    public interface OnRemoveRedsConfirmClick {
        void onConfirmRemoveRedsClick();//
    }
}
