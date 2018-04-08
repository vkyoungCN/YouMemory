package com.vkyoungcn.learningtools;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.adapter.GroupsOfMissionRvAdapter;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.models.UIGroup;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
 * 单个Mission的详情页；Mission详情及所属任务分组的集合展示（Rv）；
 * 可以新建任务分组；
 * */
public class MissionDetailActivity extends AppCompatActivity implements CreateGroupDiaFragment.OnFragmentInteractionListener {
    private static final String TAG = "MissionDetailActivity";

    List<UIGroup> groups = new ArrayList<>();//页面中Rv的数据源
    List<DBRwaGroup> dbRwaGroups = new ArrayList<>();
    private int missionIdFromIntent = 0;//从Intent获取的missionId；
    private Mission mission = new Mission();
    private RecyclerView mRv;
    private Activity self;//为了后方Timer配合runOnUiThread.
    GroupsOfMissionRvAdapter adapter = null;
    private TextView missionDetailName;
    private TextView missionDetailDescription;

    private YouMemoryDbHelper memoryDbHelper;
    private Timer groupsStateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.self = this;
        setContentView(R.layout.activity_mission_main);
        missionDetailName = (TextView) findViewById(R.id.tv_mission_detail_name);
        missionDetailDescription =(TextView) findViewById(R.id.tv_mission_detail_description);
        mRv=(RecyclerView) findViewById(R.id.groups_in_single_mission_rv);

        mRv.setLayoutManager(new LinearLayoutManager(this));
//        Log.i(TAG, "onCreate: ready for rv adapter");


        missionIdFromIntent = getIntent().getIntExtra("MissionId",0);
        memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

        //从DB获取本Mission对应的具体信息，填充本页面Activity部分内容
        mission = memoryDbHelper.getMissionById(missionIdFromIntent);

        if(mission == null){
            Toast.makeText(this, "DB returning null mission, ID from Intent is:"+missionIdFromIntent, Toast.LENGTH_SHORT).show();
        }else{
            //根据db返回数据填充Mission信息两项
            missionDetailName.setText(mission.getName());
            missionDetailDescription.setText(mission.getDescription());
        }

        //获取本Mission下的groups，准备填给RV.Adapter
        dbRwaGroups = memoryDbHelper.getAllGroupsByMissionId(missionIdFromIntent);
//        Log.i(TAG, "onCreate: dbRwaG size = "+dbRwaGroups.size());
        if(dbRwaGroups.size()>0) {
            //需要转换成Rv可用的Group格式
            for (DBRwaGroup d : dbRwaGroups) {
                UIGroup uiGroup = new UIGroup(d);
                groups.add(uiGroup);
            }
        }

        adapter = new GroupsOfMissionRvAdapter(groups,this,mission.getTableItem_suffix());
        mRv.setAdapter(adapter);




        //Timer负责每隔一分钟令Rv的adapter更新CurrentState显示
        groupsStateTimer = new Timer();
        groupsStateTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                /*后来认为，分组超时未复习不需直接删除，应由用户选择是删还是直接重新开始，
                且提示应该更详细。如果删，再执行下列任务。
                for (DBRwaGroup d :dbRwaGroups) {
                    //遍历检查，是否有需要设置为废弃的组。
                    CurrentState cs = new CurrentState();
                    LogList.setCurrentStateForGroup(cs,LogList.textListLogToListLog(d.getGroupLogs()));
                    if(!d.isObsoleted() && cs.getColorResId()==R.color.colorGP_Miss_TWICE){
                        //还未标为已废止，但是时间已经超了，所以要：标成废止、所属items标回未抽取。
                        // 并将activity持有的数据源中的这一条group标为已废弃，以防下次继续计算。
                        d.setObsoleted(true);
                        memoryDbHelper.setGroupObsoleted(d.getId());
                        memoryDbHelper.setItemsUnChose(mission.getTableItem_suffix(),d.getSubItems_ids());

                    }//其他的虽然不用处理数据库，但是需要改变时间字串
                    //但是改变字串不需要专门处理，只需令rv重新加载数据源，自动计算新值
                }
                Log.i(TAG, "run: on"+android.os.Process.getThreadPriority(android.os.Process.myTid()));*/
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        },60*1000,60*1000);

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
           DBRwaGroup dGroup = memoryDbHelper.getGroupById((int)lines);
           UIGroup newGroup = new UIGroup(dGroup);
           int oldSize = groups.size();
            Log.i(TAG, "onFragmentInteraction: old size= "+oldSize);
           groups.add(newGroup);
            Log.i(TAG, "onFragmentInteraction: ready to notify.new groups size=" +groups.size());
           adapter.notifyItemInserted(oldSize);

        }

    }
}
