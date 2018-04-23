package com.vkyoungcn.learningtools.fragments;

import android.content.Context;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vkyoungcn.learningtools.R;


public class LearningTimeUpDiaFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "LearningTimeUpDiaFragme";

    private OnSimpleDFgButtonClickListener mListener;

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
        //不论点击的是确认还是取消或其他按键，直接调用Activity中实现的监听方法，
        // 将view的id传给调用方处理。
        mListener.onDfgButtonClick(v.getId());
        dismiss();
    }


    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSimpleDFgButtonClickListener) {
            mListener = (OnSimpleDFgButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSimpleDFgButtonClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
