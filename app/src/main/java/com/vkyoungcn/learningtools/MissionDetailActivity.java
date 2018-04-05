package com.vkyoungcn.learningtools;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.adapter.AllGroupPerMissionRvAdapter;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.models.UIGroup;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.util.ArrayList;
import java.util.List;

/*
 * 单个Mission的详情页；Mission详情及所属任务分组的集合展示（Rv）；
 * 可以新建任务分组；
 * */
public class MissionDetailActivity extends AppCompatActivity implements CreateGroupDiaFragment.OnFragmentInteractionListener {
    private static final String TAG = "MissionDetailActivity";

    List<UIGroup> groups = new ArrayList<>();//页面中Rv的数据源
    private int missionIdFromIntent = 0;//从Intent获取的missionId；
    private Mission mission = null;
    private RecyclerView mRv;
    AllGroupPerMissionRvAdapter adapter = null;
    private TextView missionDetailName;
    private TextView missionDetailDescription;

    private YouMemoryDbHelper memoryDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_main);
        missionDetailName = (TextView) findViewById(R.id.tv_mission_detail_name);
        missionDetailDescription =(TextView) findViewById(R.id.tv_mission_detail_description);
        mRv=(RecyclerView) findViewById(R.id.groups_in_single_mission_rv);

        mRv.setLayoutManager(new LinearLayoutManager(this));
//        Log.i(TAG, "onCreate: ready for rv adapter");


        missionIdFromIntent = getIntent().getIntExtra("MissionId",0);
        memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

        List<DBRwaGroup> dbRwaGroups = memoryDbHelper.getAllGroupsByMissionId(missionIdFromIntent);
        //需要转换成Rv可用的Group格式
        for (DBRwaGroup d :dbRwaGroups) {
            UIGroup uiGroup = new UIGroup(d);
            groups.add(uiGroup);
        }

        adapter = new AllGroupPerMissionRvAdapter(groups);
        mRv.setAdapter(adapter);

        //从DB获取本Mission对应的具体信息，填充本页面Activity部分内容
        mission = memoryDbHelper.getMissionById(missionIdFromIntent);
        if(mission == null){
            Toast.makeText(this, "DB returning null mission, ID from Intent is:"+missionIdFromIntent, Toast.LENGTH_SHORT).show();
        }else{
            //根据db返回数据填充Mission信息两项
            missionDetailName.setText(mission.getName());
            missionDetailDescription.setText(mission.getDescription());

            //Mission和Group间的对应关系还没有建立起来；
            //需要给两个表建立外键关系；需要给MissionDetail以查看全部Items，新增Groups（及对应的DB操作）
            // ，查看和DB数据处理的逻辑等内容。待。
        }

    }

    public void createGroup(View view){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("CREATE_GROUP");

        if(prev != null){
            Log.i(TAG, "inside showDialog(), inside if prev!=null branch");
            transaction.remove(prev);
        }
        DialogFragment dfg = CreateGroupDiaFragment.newInstance(mission.getTableItem_suffix(),mission.getId());
//        Log.i(TAG, "createGroup: before show.");
        dfg.show(transaction,"CREATE_GROUP");
    }


    @Override
    public void onFragmentInteraction(long lines) {
        Log.i(TAG, "onFragmentInteraction: before");
        if(lines!=-1){
            //新增操作只影响一行
           DBRwaGroup dGroup = memoryDbHelper.getGroupsById((int)lines);
           UIGroup newGroup = new UIGroup(dGroup);
           int oldSize = groups.size();
           groups.add(newGroup);
            Log.i(TAG, "onFragmentInteraction: ready to notify");
           adapter.notifyItemInserted(oldSize);

        }

    }
}
