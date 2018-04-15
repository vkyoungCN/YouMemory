package com.vkyoungcn.learningtools;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
    /*private static final String GROUP_ID = "group_id";
    private static final String ITEM_TABLE_SUFFIX = "item_table_suffix";
    private static final String GROUP_SUB_ITEM_ID_STR = "group_sub_item_ids_str";*/
//    public static final int REQUEST_CODE_LEARNING = 1;//学习完成后，要会送然后更新adp状态。

    private long groupId;//学习系列页面的最后需要展示本组信息；
    private int position;// 回传时，更新指定条目的状态。
    private String tableNameSuffix;//学习中页面需要获取本组的items信息
    private String groupSubItemIdsStr;//传到学习第一页后减少一次DB查询
    private int stateColor;//最后生成学习记录时需要基于学习时的状态产生不同记录。

    private OnConfirmClick mListener;

    public ConfirmReadyLearningDiaFragment() {
        // Required empty public constructor
    }

    public static ConfirmReadyLearningDiaFragment newInstance(long groupId, String tableNameSuffix,String groupSubItemIdsStr,int stateColors,int position) {
        ConfirmReadyLearningDiaFragment fragment = new ConfirmReadyLearningDiaFragment();
        Bundle args = new Bundle();
        /*args.putLong(GROUP_ID, groupId);
        args.putString(ITEM_TABLE_SUFFIX, tableNameSuffix);
        args.putString(GROUP_SUB_ITEM_ID_STR,groupSubItemIdsStr);*/
        args.putInt("learning_type",stateColors);
        args.putInt("position",position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: b");
        if (getArguments() != null) {
          /*  groupId = getArguments().getLong(GROUP_ID);
            tableNameSuffix = getArguments().getString(ITEM_TABLE_SUFFIX);
            groupSubItemIdsStr = getArguments().getString(GROUP_SUB_ITEM_ID_STR);*/
            stateColor =getArguments().getInt("learning_type");
            position = getArguments().getInt("position");
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


    /*public void confirmCLick(View view){
        Log.i(TAG, "confirmCLick: b");
        Intent intent = new Intent(getActivity(),ItemLearningActivity.class);
        intent.putExtra(GROUP_ID,groupId);
        intent.putExtra(ITEM_TABLE_SUFFIX,tableNameSuffix);
        intent.putExtra(GROUP_SUB_ITEM_ID_STR,groupSubItemIdsStr);
        getActivity().startActivity(intent);
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ready_learningDfg_confirm:
                /*Log.i(TAG, "onClick: confirm b");
                mListener.onConfirmClick();
                Intent intent = new Intent(getActivity(),ItemLearningActivity.class);
                intent.putExtra(GROUP_ID,groupId);
                intent.putExtra(ITEM_TABLE_SUFFIX,tableNameSuffix);
                intent.putExtra(GROUP_SUB_ITEM_ID_STR,groupSubItemIdsStr);
                intent.putExtra("learning_type",stateColor);
                getActivity().startActivityForResult(intent,REQUEST_CODE_LEARNING);
                调用新Activity的任务改由原Activity承担，便于其直接接收返回（由dfg中转返回的方案不成功）
                */
//                this.dismiss();//【待，此方法调用时机应该在最后吗？】
                mListener.onConfirmClick(position);
                this.dismiss();
                break;
            case R.id.ready_learningDfg_cancel:
                this.dismiss();

        }
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_LEARNING:
                //其实还应判断resultCode
                String newLogsStr = data.getStringExtra("newLogsStr");

                this.dismiss();
                //通知调用方Activity。
//                mListener.onConfirmClick(position,newLogsStr);
        }
    }*/

    /*    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
*/
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
        void onConfirmClick(int position);//
    }
}
