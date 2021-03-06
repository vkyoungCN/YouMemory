package com.vkyoungcn.learningtools.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.R;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.spiralCore.GroupManager;
import com.vkyoungcn.learningtools.spiralCore.SingleLog;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class CreateGroupDiaFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "CreateGroupDiaFragment";
    private String suffix = " ";//Item表的后缀，每个Mission不同，创建分组时需要从对应的Item表中拉取Items数据。
    private int missionId = 0;

    private OnFragmentInteractionListener mListener;

    private RadioGroup radioGroup_manner;
    private RadioGroup radioGroup_size;
    private RadioButton rb_manner_order;
    private RadioButton rb_manner_random;

    private TextView tv_explanationArea;
    private EditText editText_groupDesc;

    public CreateGroupDiaFragment() {
        // Required empty public constructor
    }


    public static CreateGroupDiaFragment newInstance(String tableSuffix, int missionId) {
        CreateGroupDiaFragment fragment = new CreateGroupDiaFragment();
        Bundle args = new Bundle();
        args.putString("SUFFIX",tableSuffix);
        args.putInt("M_ID",missionId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            suffix = getArguments().getString("SUFFIX");//表的后缀
            missionId = getArguments().getInt("M_ID");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.create_group_dialog_fragment, container, false);
        //所有需要用到的8个控件，获取引用
        //【虽然IDE说redundant，但是不转型后面setText时亲测出错崩溃】
        radioGroup_manner = (RadioGroup) rootView.findViewById(R.id.rg_manner_groupCreateDfg);
        radioGroup_size = (RadioGroup) rootView.findViewById(R.id.rg_size_groupCreateDfg);
        rb_manner_order = (RadioButton) rootView.findViewById(R.id.rb_order_groupCreateDfg);
        rb_manner_random = (RadioButton) rootView.findViewById(R.id.rb_random_groupCreateDfg);
        tv_explanationArea = (TextView) rootView.findViewById(R.id.tv_createGroupDfg_explaining);
        editText_groupDesc = (EditText) rootView.findViewById(R.id.group_desc_in_create_dfg);
        TextView cancel = (TextView) rootView.findViewById(R.id.btn_cancel_createGroupDfg);
        TextView confirm = (TextView) rootView.findViewById(R.id.btn_ok_createGroupDfg);

        //部分需要添加事件监听
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);

        radioGroup_manner.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==rb_manner_order.getId()){
                    tv_explanationArea.setText("按顺序选取，随机模式下已被抽走的项目不会添加。");
                }else if(i==rb_manner_random.getId()){
                    tv_explanationArea.setText("随机选取");
                }
            }
        });
        Log.i(TAG, "onCreateView: done");
        return rootView;
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
        void onFragmentInteraction(long lines);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok_createGroupDfg://创建新分组
                //从控件获取数据；从Item-xx表按所选方式获取项目，项目置有“已抽取”；
                // 生成items条目id列表，添加到group；将group的描述、subItems列表、missionId添加到DB；
                // id由DB负责，Log暂未开始复习，mark新建列正常无。
                // 完成后通知act的Rv更新数据（或交由act负责），dismiss()。
                DBRwaGroup dbRwaGroup =new DBRwaGroup();

                int groupSize = 0;
                List<Integer> itemIds = new ArrayList<>();

                //为获取Items，需要以下字段的数据
                //控件选择了那种“item数量”选项
                switch(radioGroup_size.getCheckedRadioButtonId()){
                    case R.id.rb_36_group_create_dfg:
                        groupSize = 36;
                        break;
                    case R.id.rb_12_group_create_dfg:
                        groupSize = 12;
                        break;
                    case R.id.rb_72_group_create_dfg:
                        groupSize = 72;
                        break;
                    case R.id.rb_108_group_create_dfg:
                        groupSize = 108;
                        break;
                }

                String s = editText_groupDesc.getText().toString();
                String descString = null;
                StringBuilder descriptionSB = new StringBuilder();

                YouMemoryDbHelper memoryDbHelper = YouMemoryDbHelper.getInstance(getActivity().getApplicationContext());

                //Item抽取方式，随机或顺序
                switch (radioGroup_manner.getCheckedRadioButtonId()){
                    case R.id.rb_order_groupCreateDfg:
                        //按顺序，获取指定数量的Items
                        itemIds = memoryDbHelper.getCertainAmountItemIdsOrderly(groupSize,suffix);

                        if(itemIds.size()==0){
                            Toast.makeText(getContext(), "抽取items数量0，错误号#601（顺序抽取失败）", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(itemIds.size()<12){
                            Toast.makeText(getContext(), "抽取items数量不足12，凑不够一组了", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //为描述字段获取首词name
                        String firstItemName = memoryDbHelper.getSingleItemNameById((long)itemIds.get(0),suffix);

                        //如果描述字段留空，构建默认描述字段
                        if(s.isEmpty()){
                            descriptionSB.append("顺序-");
                            descriptionSB.append(firstItemName);
                            descriptionSB.append("开始");

                            descString = descriptionSB.toString();
                        }else{
                            descString = editText_groupDesc.getText().toString();
                        }
                        break;
                    case R.id.rb_random_groupCreateDfg:
                        //随机获取指定数量的Items
                        itemIds = memoryDbHelper.getCertainAmountItemIdsRandomly(groupSize,suffix);

                        if(itemIds.size()==0){
                            Toast.makeText(getContext(), "抽取items数量0，错误号#602（随机抽取失败）", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(itemIds.size()<12){
                            Toast.makeText(getContext(), "抽取items数量不足12，凑不够一组了", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //如果描述字段留空，构建默认描述字段“随机分组-时间”
                        if(s.isEmpty()){
                            descriptionSB.append("随机分组-");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            descriptionSB.append(sdf.format(System.currentTimeMillis()));
                            descString = descriptionSB.toString();
                        }else{
                            descString = editText_groupDesc.getText().toString();
                        }
                        break;
                }

                String logOfCreating = SingleLog.getStrSingleLogFromMillis(0,System.currentTimeMillis(),false);//建组Log记录
                //装填数据
                dbRwaGroup.setDescription(descString);
                dbRwaGroup.setMission_id(missionId);
                dbRwaGroup.setGroupLogs(logOfCreating);
                dbRwaGroup.setSubItemIdsStr(GroupManager.subItemIdsListIntToString(itemIds));
                dbRwaGroup.setFallBehind(false);

                //插入DB
                /*根据文档，lines为刚插入的记录的row ID，出错时返回-1*/
                long lines = memoryDbHelper.createGroup(dbRwaGroup,suffix);

                //根据返回情况，通知act成功或失败；
                mListener.onFragmentInteraction(lines);//【问，这里ACT不在前端能执行吗？】
                //计划由activity根据lines数（不为0时）对rv进行更新。

                //最后调用dismiss()
                dismiss();

                break;
            case R.id.btn_cancel_createGroupDfg:

                this.dismiss();
                break;

        }

    }
}
