package com.vkyoungcn.learningtools;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.adapter.ItemsOfSingleGroupAdapter;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.Item;
import com.vkyoungcn.learningtools.models.UIGroup;
import com.vkyoungcn.learningtools.spiralCore.GroupManager;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailActivity extends Activity {
    private static final String TAG = "GroupDetailActivity";
    private int groupIdFromIntent = 0;
    private String itemTableNameSuffix = "";
    private UIGroup group;
    private List<Item> items = new ArrayList<>();
    private YouMemoryDbHelper memoryDbHelper;

    private TextView groupId;
    private TextView groupDes;
    private TextView groupCsColor;
    private TextView groupCsStr;
    private Button groupLogs;
    private TextView groupRvTitle;

    private RecyclerView rv_itemsOfGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        groupId = (TextView) findViewById(R.id.tv_group_detail_id);
        groupDes = (TextView) findViewById(R.id.tv_group_detail_des);
        groupCsColor = (TextView) findViewById(R.id.tv_group_detail_csColor);
        groupCsStr = (TextView) findViewById(R.id.tv_group_detail_csString);
        groupLogs = (Button) findViewById(R.id.tv_group_detail_logs);
        groupRvTitle = (TextView) findViewById(R.id.tv_group_detail_rv_title_with_amount);

        rv_itemsOfGroup = (RecyclerView)findViewById(R.id.items_in_single_group_rv);

        groupIdFromIntent = getIntent().getIntExtra("GroupId",0);
        itemTableNameSuffix = getIntent().getStringExtra("TableSuffix");

        memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

        //从DB获取本group，如果在Intent传的话需要序列化，也是较大开销。
        DBRwaGroup dbRwaGroup = memoryDbHelper.getGroupById(groupIdFromIntent);
        group = new UIGroup(dbRwaGroup);

        //从DB获取本组所属的items
        items = memoryDbHelper.getItemsByGroupSubItemIds(dbRwaGroup.getSubItems_ids(),itemTableNameSuffix);
        Log.i(TAG, "onCreate: items.size: "+items.size());

        if (group!=null) {
            //将group的信息填充到UI
            groupId.setText(String.valueOf(group.getId()));
            groupDes.setText(group.getDescription());
            groupCsColor.setBackgroundResource(group.getGroupCurrentState().getColorResId());
            groupCsStr.setText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(group));
            groupRvTitle.setText(String.valueOf(items.size()));
        }

        //Rv配置：LM、适配器
        rv_itemsOfGroup.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.Adapter adapter = new ItemsOfSingleGroupAdapter(items,itemTableNameSuffix,this);
        rv_itemsOfGroup.setAdapter(adapter);

    }

    public void showLogs(View view){
        if(group.getGroupLogs().size()==0){
            Toast.makeText(this, "没有复习日志", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("SHOW_LOGS");

        if(prev != null){
            Log.i(TAG, "showLogs: ");
            transaction.remove(prev);
        }
        DialogFragment dialogFragment = ShowLogsOfGroupDiaFragment.newInstance(group.getGroupLogs());
//        Log.i(TAG, "createGroup: before show.");
        dialogFragment.show(transaction,"SHOW_LOGS");
    }


}
