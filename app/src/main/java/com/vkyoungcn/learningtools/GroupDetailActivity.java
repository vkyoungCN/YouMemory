package com.vkyoungcn.learningtools;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.adapter.ItemsOfSingleGroupAdapter;
import com.vkyoungcn.learningtools.fragments.ShowLogsOfGroupDiaFragment;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.GroupState;
import com.vkyoungcn.learningtools.models.RvGroup;
import com.vkyoungcn.learningtools.models.SingleItem;
import com.vkyoungcn.learningtools.spiralCore.GroupManager;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.util.List;

public class GroupDetailActivity extends Activity {
//    private static final String TAG = "GroupDetailActivity";
    private RvGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        TextView groupId = (TextView) findViewById(R.id.tv_group_detail_id);
        TextView groupDes = (TextView) findViewById(R.id.tv_group_detail_des);
        TextView groupCsColor = (TextView) findViewById(R.id.tv_group_detail_csColor);
        TextView groupCsStr = (TextView) findViewById(R.id.tv_group_detail_csString);
//        Button groupLogs = (Button) findViewById(R.id.tv_group_detail_logs);//点击方法直接在xml设置
        TextView groupRvTitle = (TextView) findViewById(R.id.tv_group_detail_rv_title_with_amount);
        RecyclerView rv_itemsOfGroup = (RecyclerView)findViewById(R.id.items_in_single_group_rv);

        int groupIdFromIntent = getIntent().getIntExtra("GroupId",0);
        String itemTableNameSuffix = getIntent().getStringExtra("TableSuffix");

        YouMemoryDbHelper memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

        //从DB获取本group，如果在Intent传的话需要序列化，也是较大开销。
        DBRwaGroup dbRwaGroup = memoryDbHelper.getGroupById(groupIdFromIntent);
        GroupState groupState = new GroupState(dbRwaGroup.getGroupLogs());
        group = new com.vkyoungcn.learningtools.models.RvGroup(dbRwaGroup,groupState,itemTableNameSuffix);

        //从DB获取本组所属的items
        List<SingleItem> items = memoryDbHelper.getItemsByGroupSubItemIds(dbRwaGroup.getSubItemIdsStr(),itemTableNameSuffix);

        if (group!=null) {
            //将group的信息填充到UI
            groupId.setText(String.valueOf(group.getId()));
            groupDes.setText(group.getDescription());
            groupCsColor.setBackgroundResource(groupState.getColorResId());
            groupCsStr.setText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(groupState));
            groupRvTitle.setText(String.valueOf(items.size()));
        }

        //Rv配置：LM、适配器
        rv_itemsOfGroup.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter adapter = new ItemsOfSingleGroupAdapter(items,itemTableNameSuffix,this);
        rv_itemsOfGroup.setAdapter(adapter);

    }

    public void showLogs(View view){
        if(group.getStrGroupLogs()==null||group.getStrGroupLogs().isEmpty()){
            Toast.makeText(this, "没有复习日志", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("SHOW_LOGS");

        if(prev != null){
            transaction.remove(prev);
        }
        DialogFragment dialogFragment = ShowLogsOfGroupDiaFragment.newInstance(group.getStrGroupLogs());
        dialogFragment.show(transaction,"SHOW_LOGS");
    }

}
