package com.vkyoungcn.learningtools;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vkyoungcn.learningtools.adapter.GroupsOfMissionRvAdapter;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.models.GroupState;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.models.RvGroup;
import com.vkyoungcn.learningtools.spiralCore.GroupManager;
import com.vkyoungcn.learningtools.spiralCore.LogList;
import com.vkyoungcn.learningtools.spiralCore.RemainingTimeAmount;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
 * 单个Mission的详情页；Mission详情及所属任务分组的集合展示（Rv）；
 * 可以新建任务分组；
 * */
public class MissionDetailActivity extends AppCompatActivity implements CreateGroupDiaFragment.OnFragmentInteractionListener,ConfirmReadyLearningDiaFragment.OnConfirmClick {
    private static final String TAG = "MissionDetailActivity";

    private Mission missionFromIntent;//从前一页面获取。后续页面需要mission的id，suffix字段。
    List<DBRwaGroup> dbRwaGroups = new ArrayList<>();//DB原始数据源
    List<RvGroup> rvGroups = new ArrayList<>();//分开设计的目的是避免适配器内部的转换，让转换在外部完成，适配器直接只用直接数据才能降低卡顿。
    private RecyclerView mRv;
    private FrameLayout maskFrameLayout;

    public static final int MESSAGE_PRE_DB_FETCHED =10;
    public static final int MESSAGE_UI_REFERSH = 11;
    private Boolean fetched =false;//是否已执行完成过从DB获取分组数据的任务；如完成，则onResume中可以重启UI-Timer
    private Boolean uiRefreshingNeeded =true;
    private Boolean isRefreshingGroupRv = false;

    private static final String GROUP_ID = "group_id";
    private static final String ITEM_TABLE_SUFFIX = "item_table_suffix";
    private static final String GROUP_SUB_ITEM_ID_STR = "group_sub_item_ids_str";
    public static final int REQUEST_CODE_LEARNING = 1;//学习完成后，要会送然后更新adp状态。
    private int clickPosition;//点击（前往学习页面）发生的位置，需要该数据来更新rv位置


    //    private Handler handler;//如果Rv效率高，就用不到多线程。
    private Activity self;//为了后方Timer配合runOnUiThread.
    private Timer groupsStateTimer;


    private GroupsOfMissionRvAdapter adapter = null;//Rv适配器引用
    private Handler handler = new GroupOfMissionHandler(this);

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
        maskFrameLayout = (FrameLayout)findViewById(R.id.maskOverRv_MissionDetail);
        findViewById(R.id.groups_refresh_missionDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //先停止计时更新（控制变量重设false）；清空旧adp
                //设置刷新rv的控制变量为真（避免重新加载adp）
                // 开启从DB获取数据的线程重获数据
                //计时更新控制变量重设真
                //完成后由其自动启动计时更新线程
                uiRefreshingNeeded = false;
                rvGroups.clear();

                isRefreshingGroupRv = true;
                new Thread(new PrepareForMissionDetailRunnable()).start();         // start thread

                uiRefreshingNeeded = true;
            }
        });

        missionFromIntent = getIntent().getParcelableExtra("Mission");


        if (missionFromIntent == null) {
//            Log.i(TAG, "onCreate: Intent has no Mission.");
        } else {
            //根据Mission数据填充Mission信息两项
            missionDetailName.setText(missionFromIntent.getName());
            missionDetailDescription.setText(missionFromIntent.getDescription());
        }

        memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

        new Thread(new PrepareForMissionDetailRunnable()).start();         // start thread

//        Log.i(TAG, "onCreate: missionId = "+missionFromIntent.getId());


        //创建属于主线程的handler
//        handler=new Handler();
    }


    //需要采用在pause-resume中重启线程的方案
    // 否则（即使排除了其他BUG，依然）存在“新变更log的条目”不会随线程更新的BUG。
    @Override
    protected void onResume() {
        super.onResume();
        if(fetched){
            uiRefreshingNeeded =true;
            //【实践验证，只设true，线程不会自动重启。】
            new Thread(new GroupsStateReCalculateRunnable()).start();         // start thread
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiRefreshingNeeded =false;

        //        groupsStateTimer.cancel();//退出首屏时终止（Terminates）本Timer，onResume中再启动。
    }



    final static class GroupOfMissionHandler extends Handler{
        private final WeakReference<MissionDetailActivity> activityWeakReference;

        public GroupOfMissionHandler(MissionDetailActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
//            Log.i(TAG, "handleMessage: b");
            MissionDetailActivity missionDetailActivity = activityWeakReference.get();
            if(missionDetailActivity!=null){
                missionDetailActivity.handleMessage(msg);
            }

        }
    }

    /*
    * 获取各分组原始数据，并进行排序，将排好序的数据数组返回给UI。
    * ①新建组按rowId位于前部；②available及missOnce按剩余时间排序；③未到时按剩余时间排序；
    * ④然后是已完成分组；⑤最后是missTwice分组。
    */
    public class PrepareForMissionDetailRunnable implements Runnable{
        @Override
        public void run() {
//            Log.i(TAG, "run: runnable b");
            //获取各分组原始数据
            dbRwaGroups = memoryDbHelper.getAllGroupsByMissionId(missionFromIntent.getId());
            //将各分组原始数据转换为UI所需数据，比较耗时。相关数据直接设置给Activity的成员。

            List<RvGroup> rvGroupsGreen = new ArrayList<>();//存放新建分组的临时list
            List<RvGroup> rvGroupsBlueAndOrange = new ArrayList<>();//存放可复习分组的临时list
            List<RvGroup> rvGroupsGrey = new ArrayList<>();//存放未到时分组的临时list
            List<RvGroup> rvGroupsZero = new ArrayList<>();//存放已完成分组的临时list
            List<RvGroup> rvGroupsRed = new ArrayList<>();//存放全失分组的临时list

            for (DBRwaGroup d : dbRwaGroups) {
                GroupState groupState = new GroupState(d.getGroupLogs());
                RvGroup rvGroup = new RvGroup(d, groupState, missionFromIntent.getTableItem_suffix());//其中的时间字串信息是生成时获取的。

                //依据新生成的rvGroup的颜色信息，加入不同的临时组
                switch (rvGroup.getStateColorResId()){
                    case R.color.colorGP_Newly:
                        rvGroupsGreen.add(rvGroup);
                        break;
                    case R.color.colorGP_AVAILABLE:
                    case R.color.colorGP_Miss_ONCE:
//                        Log.i(TAG, "run: blue id: "+rvGroup.getId());
                        rvGroupsBlueAndOrange.add(rvGroup);
                        break;
                    case R.color.colorGP_STILL_NOT:
                        rvGroupsGrey.add(rvGroup);
                        break;
                    case 0:
                        rvGroupsZero.add(rvGroup);
                        break;
                    case R.color.colorGP_Miss_TWICE:
                        rvGroupsRed.add(rvGroup);
                        break;
                }
            }

            rvGroups.addAll(rvGroupsGreen);
            rvGroups.addAll(GroupManager.ascOrderByRemainingTime(rvGroupsBlueAndOrange));
            rvGroups.addAll(GroupManager.ascOrderByRemainingTime(rvGroupsGrey));
            rvGroups.addAll(rvGroupsZero);
            rvGroups.addAll(rvGroupsRed);
            //至此已完成排序。

            Message message =new Message();
            message.what = MESSAGE_PRE_DB_FETCHED;

            handler.sendMessage(message);
        }
    }

    /*
    * 【经验证，线程在onPause后（回桌面）不停止计时】
    * 【从后续学习activity返回后，旧数据集仍能计时，但新学习的条目不计时】
    * */
    public class GroupsStateReCalculateRunnable implements Runnable{
        @Override
        public void run() {
//            Log.i(TAG, "run: b. rvGroup.size = "+rvGroups.size());

            int n=0;
            while (uiRefreshingNeeded){
                n++;//每秒加一【如果改到最后++，这样线程第一轮即可更新一次，适用于onResume；】
                List<Integer> idListOfGroupsNeedRefreshing = new ArrayList<>();
                try {
                    Thread.sleep(1000);//休息1秒钟
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //对所有项目，判断需更新的频率【所以负荷还是不小，好在都是非UI线程的任务，
                // 只有符合更新条件的才会将其索引号通知UI更新】
                for (RvGroup singleRvGroup :rvGroups) {
                    String strLogs = singleRvGroup.getStrGroupLogs();
                    if(strLogs==null||strLogs.isEmpty()){
                        //空则为新建--不更新
                        continue;
                    }
                    RemainingTimeAmount remainingTimeAmount = LogList.getCurrentRemainingTimeForGroup(strLogs);
                    if(remainingTimeAmount.getRemainingDays()!=0){
                        //【按设计逻辑，剩余时间大于1天的，不显示分钟数，每小时更新一次即可】
                        if(n%3600==0){
                            idListOfGroupsNeedRefreshing.add(rvGroups.indexOf(singleRvGroup));
                            singleRvGroup.setStateText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(new GroupState(singleRvGroup.getStrGroupLogs())));
                        }
                    }else if(remainingTimeAmount.getRemainingHours()!=0||remainingTimeAmount.getRemainingMinutes()>15){
                        //不足1天的都会显示分钟，（其中大于15分钟的），需要每分钟更新一次
                        if(n%60==0) {
                            //每分钟，将索引加入到待更新列表一次；且更新其时间状态字串。
                            idListOfGroupsNeedRefreshing.add(rvGroups.indexOf(singleRvGroup));
                            singleRvGroup.setStateText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(new GroupState(singleRvGroup.getStrGroupLogs())));
                            singleRvGroup.setStateColorResId(new GroupState(strLogs).getColorResId());
                            //【这种引用更新实践证实有用。】
                        }
                    }else if (remainingTimeAmount.getRemainingMinutes()>10){
                        //小于15分钟大于10分钟，为了更高的精度，每30秒更新一次
                        if(n%30==0) {
                            //每分钟，将索引加入到待更新列表一次；且更新其时间状态字串。
                            idListOfGroupsNeedRefreshing.add(rvGroups.indexOf(singleRvGroup));
                            singleRvGroup.setStateText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(new GroupState(singleRvGroup.getStrGroupLogs())));
                            singleRvGroup.setStateColorResId(new GroupState(strLogs).getColorResId());
                        }
                    }else {
                        //不足10分钟的，为了更高的精度，每15秒更新一次
                        if(n%15==0){
                            //每分钟，将索引加入到待更新列表一次；且更新其时间状态字串。
                            idListOfGroupsNeedRefreshing.add(rvGroups.indexOf(singleRvGroup));
                            singleRvGroup.setStateText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(new GroupState(singleRvGroup.getStrGroupLogs())));
                            singleRvGroup.setStateColorResId(new GroupState(strLogs).getColorResId());
                        }
                    }
                }
                Message message = new Message();
                message.what = MESSAGE_UI_REFERSH;
                message.obj = idListOfGroupsNeedRefreshing;

                handler.sendMessage(message);
            }
        }
    }

    void handleMessage(Message message){

        switch (message.what){
            case MESSAGE_PRE_DB_FETCHED://此时是从DB获取各分组数据并转换成合适的数据源完成
                fetched = true;//用于onResume中的判断。
//                Log.i(TAG, "handleMessage: case 1 b");
                //取消上方遮罩
                maskFrameLayout.setVisibility(View.GONE);

                if(!isRefreshingGroupRv) {//在首次启动时，不是刷新Rv，需要下列工作
                    //初始化Rv构造器，令UI加载Rv控件……
                    adapter = new GroupsOfMissionRvAdapter(rvGroups, this, missionFromIntent.getTableItem_suffix());
                    mRv = findViewById(R.id.groups_in_single_mission_rv);
                    mRv.setLayoutManager(new LinearLayoutManager(this));
                    mRv.setAdapter(adapter);
                }else {
                    //如果是刷新Rv数据源的操作，只需
                    adapter.notifyDataSetChanged();

                }

                //Rv加载后，启动更新计时器【此方式不能实现UI的更新，因为UI所需的数据是提前写好了的；
                // 要更新显示，必须提供新的数据；这一任务符合较大，改由新线程执行】
                new Thread(new GroupsStateReCalculateRunnable()).start();         // start thread

                //Timer负责每隔一分钟令Rv的adapter更新CurrentState显示
                /*groupsStateTimer = new Timer();
                groupsStateTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                *//*后来认为，分组超时未复习不需直接删除，应由用户选择是删还是直接重新开始，
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
                Log.i(TAG, "run: on"+android.os.Process.getThreadPriority(android.os.Process.myTid()));*//*
                        self.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });

                    }
                },60*1000,60*1000);*/
            case MESSAGE_UI_REFERSH:
                List<Integer> positionsNeedUpdate = (ArrayList)(message.obj);

                //所有的批量notify方法都只能用于连续项目，所以只能…
                if(positionsNeedUpdate==null||positionsNeedUpdate.size()==0) return;//判空
                for (int i :positionsNeedUpdate) {
                    adapter.notifyItemChanged(i);
                }

        }
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
//        Log.i(TAG, "onFragmentInteraction: before");

        //如果新增操作成功，通知adp变更。
        if (lines != -1) {
            //新增操作只影响一行
            DBRwaGroup dGroup = memoryDbHelper.getGroupById((int) lines);
            GroupState groupState = new GroupState(dGroup.getGroupLogs());
            RvGroup newGroup = new RvGroup(dGroup, groupState, missionFromIntent.getTableItem_suffix());

//            int oldSize = dbRwaGroups.size();
//            Log.i(TAG, "onFragmentInteraction: old size= "+oldSize);
            rvGroups.add(0,newGroup);//新增分组放在最前【逻辑便于处理】
//            Log.i(TAG, "onFragmentInteraction: ready to notify.new group size=" +rvGroups.size());
            adapter.notifyItemInserted(0);//（仍是0起算，但是加到最后时似乎比较奇怪）
//            adapter.notifyItemInserted(rvGroups.size());//【这个方法的意思是在添加后的数据集的第X项上（从1起算，不是0）是新插入的数据】
            mRv.scrollToPosition(0);//设置增加后滚动到新增位置。【这个则是从0起算】
        }

    }


    @Override
    public void onConfirmClick(int position) {

        this.clickPosition = position;
        Intent intent = new Intent(this,ItemLearningActivity.class);
        intent.putExtra("group_id",rvGroups.get(position).getId());
//        Log.i(TAG, "onConfirmClick: rvgroup.get-position-getId = "+rvGroups.get(position).getId());
        intent.putExtra(ITEM_TABLE_SUFFIX,missionFromIntent.getTableItem_suffix());
        intent.putExtra(GROUP_SUB_ITEM_ID_STR,rvGroups.get(position).getStrSubItemsIds());
        intent.putExtra("learning_type",rvGroups.get(position).getStateColorResId());
        this.startActivityForResult(intent,REQUEST_CODE_LEARNING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_LEARNING:
                if(data == null) return;
                String newLogsStr = data.getStringExtra("newLogsStr");
                Log.i(TAG, "onActivityResult: new Lg str = "+newLogsStr);
                if(newLogsStr.isEmpty()) return;//根据当前设计，60分钟内的复习不计入Log且返回空串，
                // 所以最终接受到空串后，应直接返回，UI仍按原方式记录即可。

                //通知adp变更显示
                RvGroup groupWithNewLog = rvGroups.get(clickPosition);
                GroupState newGS = new GroupState(newLogsStr);
                groupWithNewLog.setStateText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(newGS));
                groupWithNewLog.setStateColorResId(newGS.getColorResId());
                groupWithNewLog.setStrGroupLogs(newLogsStr);//UI的定时更新线程需要该字段做计算。否则本条无法按时更新。

                rvGroups.set(clickPosition,groupWithNewLog);
                adapter.notifyItemChanged(clickPosition);
        }
    }
}

/*旧片段范例
* groupsStateTimer = new Timer(); 25 28 30
            groupsStateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    self.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
            },60*1000,60*1000);
* */