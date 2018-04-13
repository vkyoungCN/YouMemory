package com.vkyoungcn.learningtools;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vkyoungcn.learningtools.adapter.LearningViewPrAdapter;
import com.vkyoungcn.learningtools.models.SingleItem;
import com.vkyoungcn.learningtools.sqlite.LearningEndingUpConfirmDiaFragment;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemLearningActivity extends AppCompatActivity {
    private static final String TAG = "ItemLearningActivity";
    private long groupId;//最后的结果页面需要获取分组信息
    private int timePastInMinute = 0;//流逝分钟数
    private int timeInSecond = 0;
    private String tableNameSuffix;//用来从DB获取本组所属的ITEMS
    private String groupSubItemIdsStr;//用来从DB获取本组所属的ITEMS
    private List<SingleItem> items;//数据源（未初始化）
    private Boolean prepared = false;
    private Handler handler = new LearningActivityHandler(this);

    private YouMemoryDbHelper memoryDbHelper;

    private FrameLayout fltMask;
    private ViewPager viewPager;
    private TextView timePastMin;
    private TextView timePastScd;

    private TextView tv_currentPageNum;
    private TextView tv_totalPageNum;

    public static final int MESSAGE_DB_DATE_FETCHED =1;
    public static final int MESSAGE_ONE_MINUTE_CHANGE =2;
    public static final int MESSAGE_ONE_SECOND_CHANGE =3;
    public static final int MESSAGE_TIME_UP = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: b");
        setContentView(R.layout.activity_item_learning);

        //从Intent获取参数
        groupId = getIntent().getLongExtra("group_id",0);
        tableNameSuffix = getIntent().getStringExtra("item_table_suffix");
        groupSubItemIdsStr = getIntent().getStringExtra("group_sub_item_ids_str");

        fltMask = (FrameLayout) findViewById(R.id.flt_mask_learningPage);
        timePastMin = (TextView)findViewById(R.id.tv_time_past_numMinute_Learning);
        timePastScd = (TextView)findViewById(R.id.tv_time_past_numSecond_Learning);
        tv_currentPageNum = (TextView)findViewById(R.id.currentPageNum_learningActivity);
        tv_totalPageNum = (TextView)findViewById(R.id.totalPageNum_learningActivity);//总数字需要在数据加载完成后设置，在handleMessage中处理


        viewPager = (ViewPager) findViewById(R.id.viewPager_ItemLearning);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //当页面滑动时为下方的textView设置当前页数，但是只在开始滑动后才有效果，初始进入时需要手动XML设为1
                tv_currentPageNum.setText(String.valueOf(position+1));//索引从0起需要加1
                if(position==items.size()-1){
                    //最后一张，弹出dfg。问是否完成学习：
                    // 如确认完成：向DB写数据；
                    // 如cancel，解散dfg。
                    popupEndingDiaFragment();
                }
            }
        });

        //从db查询List<SingleItem>放在另一线程
        new Thread(new PreparingRunnable()).start();         // start thread


        //后期增加：①items可选顺序随机；
        // ②增加倒计时欢迎页面；


    }

    final static class LearningActivityHandler extends Handler{
        private final WeakReference<ItemLearningActivity> activity;

        public LearningActivityHandler(ItemLearningActivity activity) {
            this.activity = new WeakReference<ItemLearningActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
//            Log.i(TAG, "handleMessage: b");
            ItemLearningActivity itemLearningActivity = activity.get();
            if(itemLearningActivity != null){
//                switch (msg.what){
//                    case 1://从DB检索到Items数据，取消遮罩，开始计时，开启ViewPager+Fragment
                        //【不知道能否直接从其他线程设置activity的成员变量数据】
                        //【但是至少不能在此直接使用Activity的成员变量，所以】
//                }
                //任务方法在activity中另行编写（否则无法使用activity的成员变量）
                // 并且是需要用刚刚get()到的activity引用来调用！,
                itemLearningActivity.handleMessage(msg);
            }

    }};

    public class PreparingRunnable implements Runnable{
        private static final String TAG = "PreparingRunnable";

        @Override
        public void run() {
//            Log.i(TAG, "run: b");
            //从DB读取数据
            memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());
            items = memoryDbHelper.getItemsByGroupSubItemIds(groupSubItemIdsStr,tableNameSuffix);

            Message message = new Message();
            message.what = MESSAGE_DB_DATE_FETCHED;

            handler.sendMessage(message);
        }
    }

    //用于计时并发送更新UI上时间值的消息
    public class TimingRunnable implements Runnable{
        private static final String TAG = "TimingRunnable";

        @Override
        public void run() {
//            Log.i(TAG, "run: ");
            int timeCount = 60;//运行59次，加到59分后改为按秒计算。
            while(timeCount > 0){
                try {
                    for (int i = 0; i < 60; i++) {
                        Thread.sleep(1000);     // sleep 1 秒

                        //消息发回UI，改变秒数1
                        Message message = new Message();
                        message.what = MESSAGE_ONE_SECOND_CHANGE;
                        handler.sendMessage(message);

                    }

                    timeCount--;
                    //消息发回UI，改变分钟数1
                    Message message = new Message();
                    message.what = MESSAGE_ONE_MINUTE_CHANGE;
                    handler.sendMessage(message);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //消息发回UI，时间到
            Message message = new Message();
            message.what = MESSAGE_TIME_UP;
            handler.sendMessage(message);


        }
    }

    void handleMessage(Message msg){
        switch (msg.what){
            case MESSAGE_DB_DATE_FETCHED:
//                Log.i(TAG, "handleMessage: case 1 b");
                fltMask.setVisibility(View.GONE);
                LearningViewPrAdapter lvAdp = new LearningViewPrAdapter(getSupportFragmentManager(),items,LearningViewPrAdapter.TYPE_INIT_LEARNING);
//                Log.i(TAG, "handleMessage: ready to set adp");
                viewPager.setAdapter(lvAdp);

                tv_totalPageNum.setText(String.valueOf(items.size()));
                prepared = true;
                //然后由启动计时器（每分钟更改一次数字）
                new Thread(new TimingRunnable()).start();
                break;

            case MESSAGE_ONE_SECOND_CHANGE:
                timeInSecond++;
                timePastScd.setText(String.format("%02d",timeInSecond%60));
                break;

            case MESSAGE_ONE_MINUTE_CHANGE:
                timePastInMinute++;
                timePastMin.setText(String.format("%02d",timePastInMinute));
                break;

            case MESSAGE_TIME_UP:
                //安排时间到后的逻辑
                //①是否续时；②强制结束，执行收尾逻辑。【完成数量大于18个可以拆分为两组】或者强制结束不做任何记录。

                break;

        }
    }

    private void popupEndingDiaFragment(){
        //弹出dfg询问是否完成本次学习
        // 其上显示“本组信息、本组学习用时等”
        // 如点击confirm键则生成LOG(注意根据之前log和时间跨度处理是一条还是两条)存入DB.

        FragmentTransaction transaction = (getFragmentManager().beginTransaction());
        Fragment prev = (getFragmentManager().findFragmentByTag("ENDING_UP"));

        if (prev != null) {
            Log.i(TAG, "inside Dialog(), inside if prev!=null branch");
            transaction.remove(prev);
        }
        【下边dfg还没有写，是最后的大功能了】
        DialogFragment dfg = LearningEndingUpConfirmDiaFragment.newInstance("0","0");

    }

}
