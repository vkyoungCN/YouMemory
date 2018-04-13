package com.vkyoungcn.learningtools;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 开始学习前弹出的对话框，确认键后进入学习Activity
 *
 */
public class ConfirmReadyLearningDiaFragment extends DialogFragment implements View.OnClickListener{
    private static final String TAG = "ConfirmReadyLearningDia";
    private static final String GROUP_ID = "group_id";
    private static final String ITEM_TABLE_SUFFIX = "item_table_suffix";
    private static final String GROUP_SUB_ITEM_ID_STR = "group_sub_item_ids_str";

    private long groupId;//学习系列页面的最后需要展示本组信息
    private String tableNameSuffix;//学习中页面需要获取本组的items信息
    private String groupSubItemIdsStr;//传到学习第一页后减少一次DB查询

//    private OnFragmentInteractionListener mListener;

    public ConfirmReadyLearningDiaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConfirmReadyLearningDiaFragment.
     */
    public static ConfirmReadyLearningDiaFragment newInstance(long groupId, String tableNameSuffix,String groupSubItemIdsStr) {
        ConfirmReadyLearningDiaFragment fragment = new ConfirmReadyLearningDiaFragment();
        Bundle args = new Bundle();
        args.putLong(GROUP_ID, groupId);
        args.putString(ITEM_TABLE_SUFFIX, tableNameSuffix);
        args.putString(GROUP_SUB_ITEM_ID_STR,groupSubItemIdsStr);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: b");
        if (getArguments() != null) {
            groupId = getArguments().getLong(GROUP_ID);
            tableNameSuffix = getArguments().getString(ITEM_TABLE_SUFFIX);
            groupSubItemIdsStr = getArguments().getString(GROUP_SUB_ITEM_ID_STR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: b");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.confirm_ready_learning_dialog_fragment, container, false);
        rootView.findViewById(R.id.ready_learningDfg_confirm).setOnClickListener(this);
        rootView.findViewById(R.id.ready_learningDfg_cancel).setOnClickListener(this);
        return rootView;
    }


    public void confirmCLick(View view){
        Log.i(TAG, "confirmCLick: b");
        Intent intent = new Intent(getActivity(),ItemLearningActivity.class);
        intent.putExtra(GROUP_ID,groupId);
        intent.putExtra(ITEM_TABLE_SUFFIX,tableNameSuffix);
        intent.putExtra(GROUP_SUB_ITEM_ID_STR,groupSubItemIdsStr);
        getActivity().startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ready_learningDfg_confirm:
                Log.i(TAG, "onClick: confirm b");
                Intent intent = new Intent(getActivity(),ItemLearningActivity.class);
                intent.putExtra(GROUP_ID,groupId);
                intent.putExtra(ITEM_TABLE_SUFFIX,tableNameSuffix);
                intent.putExtra(GROUP_SUB_ITEM_ID_STR,groupSubItemIdsStr);
                getActivity().startActivity(intent);
                this.dismiss();//【待，此方法调用时机应该在最后吗？】
                break;
            case R.id.ready_learningDfg_cancel:
                this.dismiss();

        }
    }

    /*    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
