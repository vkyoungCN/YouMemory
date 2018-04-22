package com.vkyoungcn.learningtools;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class LearningTimeUpDiaFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "LearningTimeUpDiaFragme";

    private OnUserChoiceMadeListener mListener;

    public LearningTimeUpDiaFragment() {
        // Required empty public constructor
    }

    public static LearningTimeUpDiaFragment newInstance() {
        LearningTimeUpDiaFragment fragment = new LearningTimeUpDiaFragment();
        /*Bundle args = new Bundle();
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_learning_ending_up_confirm_dia, container, false);
        rootView.findViewById(R.id.confirm_timeEndingDfg).setOnClickListener(this);
        rootView.findViewById(R.id.cancel_timeEndingDfg).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.confirm_timeEndingDfg:
                mListener.onUserMadeChoice(true);//点击了确认则将消息传递给调用方
                dismiss();
                break;
            case R.id.cancel_timeEndingDfg:
                mListener.onUserMadeChoice(false);//点击了取消（不延时，将直接进入未完成的完结程序）
                dismiss();
                break;
        }
    }


    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserChoiceMadeListener) {
            mListener = (OnUserChoiceMadeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserChoiceMadeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
    * 用户点击了确认续时的按钮后，通知Activity。
    * 不必传递布尔值。
    * */
    public interface OnUserChoiceMadeListener {
        void onUserMadeChoice(boolean isConfirmed);
    }
}
