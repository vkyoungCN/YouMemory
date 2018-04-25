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
import android.widget.Toast;

import com.vkyoungcn.learningtools.adapter.GroupsOfMissionRvAdapter;
import com.vkyoungcn.learningtools.fragments.ConfirmDeletingDiaFragment;
import com.vkyoungcn.learningtools.fragments.ConfirmReadyLearningDiaFragment;
import com.vkyoungcn.learningtools.fragments.ConfirmRemoveRedsDiaFragment;
import com.vkyoungcn.learningtools.fragments.CreateGroupDiaFragment;
import com.vkyoungcn.learningtools.fragments.OnSimpleDFgButtonClickListener;
import com.vkyoungcn.learningtools.models.DBRwaGroup;
import com.vkyoungcn.learningtools.spiralCore.GroupState;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.models.RvGroup;
import com.vkyoungcn.learningtools.spiralCore.GroupManager;
import com.vkyoungcn.learningtools.spiralCore.LogList;
import com.vkyoungcn.learningtools.spiralCore.RemainingTimeAmount;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.vkyoungcn.learningtools.ItemLearningActivity.RESULT_EXTRA_LEARNING_SUCCEEDED;

/*
 * 单个Mission的详情页；
 * 页面上部是Mission详情；
 * 页面下部是所属分组的集合展示（Rv）；默认以排序方式（新组最前，蓝、橙按时间次之，灰、红在后。）
 * 本页面中：可以新建分组；可以对超时分组（红色）进行删除，所属词汇回归为未选中的Item。
 * 点击Rv中的条项（绿、蓝、橙）可以进入学习/复习页面；会有确认框弹出提示。
 * 学习/复习完成或因超时而未能完成的，都会回到本页面；完成则更新RV列表的显示，
 * 失败则产生一条消息【待实现】。
 * */
public class MissionDetailActivity extends AppCompatActivity implements OnSimpleDFgButtonClickListener,
        CreateGroupDiaFragment.OnFragmentInteractionListener,ConfirmReadyLearningDiaFragment.OnConfirmClick,
        ConfirmRemoveRedsDiaFragment.OnRemoveRedsConfirmClick, ConfirmDeletingDiaFragment.OnDeletingGroupDfgClickListener {
    private static final String TAG = "MissionDetailActivity";

    public static final int MESSAGE_PRE_DB_FETCHED =5011;
    public static final int MESSAGE_UI_RE_FRESH = 5012;

    private static final String ITEM_TABLE_SUFFIX = "item_table_suffix";
    private static final String GROUP_SUB_ITEM_ID_STR = "group_sub_item_ids_str";
    public static final int REQUEST_CODE_LEARNING = 2011;//学习完成后，要回送然后更新Rv数据源和显示。

    private Mission missionFromIntent;//从前一页面获取。后续页面需要mission的id，suffix字段。
    List<DBRwaGroup> dbRwaGroups = new ArrayList<>();//DB原始数据源
    List<RvGroup> rvGroups = new ArrayList<>();//分开设计的目的是避免适配器内部的转换，让转换在外部完成，适配器直接只用直接数据才能降低卡顿。
    private YouMemoryDbHelper memoryDbHelper;
    private String tableItemSuffix;//由于各任务所属的Item表不同，后面所有涉及Item的操作都需要通过后缀才能构建出完整表名。
    private RecyclerView mRv;
    private GroupsOfMissionRvAdapter adapter = null;//Rv适配器引用
    private int clickPosition;//点击（前往学习页面）发生的位置，需要该数据来更新rv位置

    private FrameLayout maskFrameLayout;
    //另外，页面上部的Mission详情区“任务名称、描述”两个控件不声明为全局变量。在onCreate内以局部变量声明。

    private Activity self;//为了后方Timer配合runOnUiThread.
    private Handler handler = new GroupOfMissionHandler(this);//涉及弱引用【待】，通过其发送消息。
    private Boolean fetched =false;//是否已执行完成过从DB获取分组数据的任务；如完成，则onResume中可以重启UI-Timer
    private Boolean uiRefreshingNeeded =true;//退出到Pause状态时，停止定时更新线程【是否会自动停止？】；并在刷新Rv列表时暂停更新。
    private Boolean isRefreshingGroupRv = false;//点击刷新列表的按键后，会重新执行加载数据的线程，为与首次的自动运行相区分，此标志变量会设true。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.self = this;
        setContentView(R.layout.activity_mission_main);

        TextView missionDetailName = (TextView) findViewById(R.id.tv_mission_detail_name);
        TextView missionDetailDescription = (TextView) findViewById(R.id.tv_mission_detail_description);
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

        findViewById(R.id.groups_removeRed_missionDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rvGroups == null || rvGroups.size()==0)return;
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("REMOVE_REDS");

                if (prev != null) {
                    transaction.remove(prev);
                }
                DialogFragment dfg = ConfirmRemoveRedsDiaFragment.newInstance();
                dfg.show(transaction, "REMOVE_REDS");
            }
        });

        missionFromIntent = getIntent().getParcelableExtra("Mission");


        if (missionFromIntent == null) {
            Toast.makeText(self, "任务信息传递失败", Toast.LENGTH_SHORT).show();
        } else {
            //根据Mission数据填充Mission信息两项
            missionDetailName.setText(missionFromIntent.getName());
            missionDetailDescription.setText(missionFromIntent.getDescription());
            tableItemSuffix = missionFromIntent.getTableItem_suffix();
        }

        memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

        new Thread(new PrepareForMissionDetailRunnable()).start();         // start thread
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
    }



    final static class GroupOfMissionHandler extends Handler{
        private final WeakReference<MissionDetailActivity> activityWeakReference;

        private GroupOfMissionHandler(MissionDetailActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
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
            //获取各分组原始数据
            dbRwaGroups = memoryDbHelper.getAllGroupsByMissionId(missionFromIntent.getId());
            //将各分组原始数据转换为UI所需数据，比较耗时。相关数据直接设置给Activity的成员。

            //5个临时分组
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

            //将不同颜色的分组按设计的先后加入数据源中；其中部分分组需要再执行一次按时间排序的操作后再加入。
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
                    if(strLogs.split(";").length == 1){
                        //新建，不更新
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
                message.what = MESSAGE_UI_RE_FRESH;
                message.obj = idListOfGroupsNeedRefreshing;

                handler.sendMessage(message);
            }
        }
    }

    void handleMessage(Message message){
        switch (message.what){
            case MESSAGE_PRE_DB_FETCHED://此时是从DB获取各分组数据并转换成合适的数据源完成
                fetched = true;//用于onResume中的判断。
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

                //后来认为，分组超时未复习不需直接删除，应由用户选择是删还是直接重新开始，
            case MESSAGE_UI_RE_FRESH:
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
            Toast.makeText(self, "Old DialogFg still there, removing first...", Toast.LENGTH_SHORT).show();
            transaction.remove(prev);
        }
        DialogFragment dfg = CreateGroupDiaFragment.newInstance(missionFromIntent.getTableItem_suffix(), missionFromIntent.getId());
        dfg.show(transaction, "CREATE_GROUP");
    }

    /*
    * 相应某按钮点击事件，跳转到新页面显示本任务所属全部items
    * */
    public void listingItems(View view){
        Log.i(TAG, "listingItems: ");
        Intent intent = new Intent(this,ItemsOfMissionActivity.class);
        intent.putExtra("MissionTableSuffix",tableItemSuffix);
        intent.putExtra("Mission",missionFromIntent);
        startActivity(intent);

    }


    @Override
    public void onDfgButtonClick(int viewId) {
        switch (viewId) {
            case R.id.ready_learningDfg_confirm:
                break;
        }
    }

    @Override
    public void onFragmentInteraction(long lines) {
        //如果新增操作成功，通知adp变更。
        if (lines != -1) {
            //新增操作只影响一行
            DBRwaGroup dGroup = memoryDbHelper.getGroupById((int) lines);
            GroupState groupState = new GroupState(dGroup.getGroupLogs());
            RvGroup newGroup = new RvGroup(dGroup, groupState, missionFromIntent.getTableItem_suffix());

            rvGroups.add(0,newGroup);//新增分组放在最前【逻辑便于处理】
            adapter.notifyItemInserted(0);//（仍是0起算，但是加到最后时似乎比较奇怪）
            mRv.scrollToPosition(0);//设置增加后滚动到新增位置。【这个则是从0起算】
        }

    }


    @Override
    public void onConfirmClick(int position) {
        this.clickPosition = position;
        Intent intent = new Intent(this,ItemLearningActivity.class);
        intent.putExtra("group_id",rvGroups.get(position).getId());
        intent.putExtra(ITEM_TABLE_SUFFIX, tableItemSuffix);
        intent.putExtra(GROUP_SUB_ITEM_ID_STR,rvGroups.get(position).getStrSubItemsIds());
        intent.putExtra("learning_type",rvGroups.get(position).getStateColorResId());//在最后的DB操作中，蓝色、橙色的日志生成方式不同，无法统一做“复习”传递。
        this.startActivityForResult(intent,REQUEST_CODE_LEARNING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_LEARNING:
                switch (resultCode){
                    case ItemLearningActivity.RESULT_LEARNING_SUCCEEDED:
                        if(data == null) return;
                        String newLogsStr = data.getStringExtra("newLogsStr");
                        if(newLogsStr.isEmpty()) return;//根据当前设计，60分钟内的复习不计入Log且返回空串，【灰色状态下的额外复习亦然】
                        // 所以最终接受到空串后，应直接返回，UI仍按原方式记录即可。

                        //通知adp变更显示
                        RvGroup groupWithNewLog = rvGroups.get(clickPosition);
                        GroupState newGS = new GroupState(newLogsStr);
                        groupWithNewLog.setStateText(GroupManager.getCurrentStateTimeAmountStringFromUIGroup(newGS));
                        groupWithNewLog.setStateColorResId(newGS.getColorResId());
                        groupWithNewLog.setStrGroupLogs(newLogsStr);//UI的定时更新线程需要该字段做计算。否则本条无法按时更新。

                        rvGroups.set(clickPosition,groupWithNewLog);
                        adapter.notifyItemChanged(clickPosition);
                        break;
                    case ItemLearningActivity.RESULT_LEARNING_FAILED:
                        if(data == null) return;
                        String failedStartTimeMillis = data.getStringExtra("startingTimeMills");
                        Toast.makeText(self, "Learning starting at ("+failedStartTimeMillis+") has been failed because of TimeUp.", Toast.LENGTH_SHORT).show();
                        //【下面应该生成一条失败的消息】
                    case RESULT_EXTRA_LEARNING_SUCCEEDED:
                        Toast.makeText(this, "额外学习1次，完成！", Toast.LENGTH_SHORT).show();

                }

        }
    }

    @Override
    public void onConfirmRemoveRedsClick() {
        ArrayList<Integer> redsPositions = new ArrayList<>();
        for (RvGroup r :rvGroups) {
            if(r.getStateColorResId()==R.color.colorGP_Miss_TWICE){
                redsPositions.add(rvGroups.indexOf(r));
            }
        }
        if (redsPositions.size()==0){
            Toast.makeText(self, "没有需要移除的任务分组", Toast.LENGTH_SHORT).show();
        }else {
            for (Integer i :redsPositions) {
                RvGroup rvToRemove = rvGroups.get(i);
                memoryDbHelper.removeGroupById(rvToRemove.getId(),rvToRemove.getStrSubItemsIds(),tableItemSuffix);
            }
        }
    }

    //在Rv中对单项长按删除。长按后，弹出对话框，点击确认后调用本回调，删除单项。
    @Override
    public void onDeletingConfirmClick(int position) {
        RvGroup rvGroupToRemove = rvGroups.get(position);
        memoryDbHelper.removeGroupById(rvGroupToRemove.getId(),rvGroupToRemove.getStrSubItemsIds(),tableItemSuffix);
        adapter.notifyItemRemoved(position);
    }
}

/*旧片段
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