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

import com.vkyoungcn.learningtools.adapter.GroupsOfMissionRvAdapter;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.GroupState;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.models.RvGroup;
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

    private Mission missionFromIntent;//从前一页面获取。后续页面需要mission的id，suffix字段。
    List<DBRwaGroup> dbRwaGroups = new ArrayList<>();//DB原始数据源
    List<RvGroup> rvGroups = new ArrayList<>();//分开设计的目的是避免适配器内部的转换，让转换在外部完成，适配器直接只用直接数据才能降低卡顿。
    private RecyclerView mRv;

    //    private Handler handler;//如果Rv效率高，就用不到多线程。
    private Activity self;//为了后方Timer配合runOnUiThread.
    private Timer groupsStateTimer;


    GroupsOfMissionRvAdapter adapter = null;//Rv适配器引用

    //上方Mission详情区控件
    private TextView missionDetailName;
    private TextView missionDetailDescription;

    private TextView tvShowGroups;
    private TextView tvHideGroups;

    private YouMemoryDbHelper memoryDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.self = this;
        setContentView(R.layout.activity_mission_main);
        missionDetailName = (TextView) findViewById(R.id.tv_mission_detail_name);
        missionDetailDescription = (TextView) findViewById(R.id.tv_mission_detail_description);

        missionFromIntent = getIntent().getParcelableExtra("Mission");


        if (missionFromIntent == null) {
            Log.i(TAG, "onCreate: Intent has no Mission.");
        } else {
            //根据Mission数据填充Mission信息两项
            missionDetailName.setText(missionFromIntent.getName());
            missionDetailDescription.setText(missionFromIntent.getDescription());
        }

        memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

//        Log.i(TAG, "onCreate: missionId = "+missionFromIntent.getId());
        dbRwaGroups = memoryDbHelper.getAllGroupsByMissionId(missionFromIntent.getId());

        for (DBRwaGroup d : dbRwaGroups) {
            GroupState groupState = new GroupState(d.getGroupLogs());
            RvGroup rvGroup = new RvGroup(d, groupState, missionFromIntent.getTableItem_suffix());//其中的时间字串信息是生成时获取的。
            rvGroups.add(rvGroup);
        }
//        Log.i(TAG, "onCreate: group size:"+dbRwaGroups.size());

        adapter = new GroupsOfMissionRvAdapter(rvGroups, getApplicationContext(), missionFromIntent.getTableItem_suffix());
        mRv = findViewById(R.id.groups_in_single_mission_rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(adapter);

        //创建属于主线程的handler
//        handler=new Handler();


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
                        memoryDbHelper.setItemsUnChose(mission.getTableItem_suffix(),d.getSubItemIdsStr());

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

    public void createGroup(View view) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("CREATE_GROUP");

        if (prev != null) {
            Log.i(TAG, "inside showDialog(), inside if prev!=null branch");
            transaction.remove(prev);
        }
        DialogFragment dfg = CreateGroupDiaFragment.newInstance(missionFromIntent.getTableItem_suffix(), missionFromIntent.getId());
//        Log.i(TAG, "createGroup: before show.");
        dfg.show(transaction, "CREATE_GROUP");
    }

    @Override
    public void onFragmentInteraction(long lines) {
        Log.i(TAG, "onFragmentInteraction: before");

        //如果新增操作成功，通知adp变更。
        if (lines != -1) {
            //新增操作只影响一行
            DBRwaGroup dGroup = memoryDbHelper.getGroupById((int) lines);
            GroupState groupState = new GroupState(dGroup.getGroupLogs());
            RvGroup newGroup = new RvGroup(dGroup, groupState, missionFromIntent.getTableItem_suffix());

            int oldSize = dbRwaGroups.size();
//            Log.i(TAG, "onFragmentInteraction: old size= "+oldSize);
            rvGroups.add(newGroup);
//            Log.i(TAG, "onFragmentInteraction: ready to notify.new dbRwaGroups size=" +dbRwaGroups.size());
            adapter.notifyItemInserted(oldSize);
        }

    }


//
// Log.i(TAG, "onCreate: ready for rv adapter");

//        Log.i(TAG, "onCreate: dbRwaG size = "+dbRwaGroups.size());
        /*if(dbRwaGroups.size()>0) {
            //需要转换成Rv可用的Group格式
            for (DBRwaGroup d : dbRwaGroups) {
                RvGroup uiGroup = new RvGroup(d);
                dbRwaGroups.add(uiGroup);
            }
        }*/





   /* // 构建Runnable对象。在runnable中更新界面
    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            //UI更新
            mRv=(RecyclerView) findViewById(R.id.groups_in_single_mission_rv);

            mRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            mRv.setAdapter(adapter);
        }
    };*/

}
