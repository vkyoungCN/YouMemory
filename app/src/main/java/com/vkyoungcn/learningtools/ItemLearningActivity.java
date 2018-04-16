package com.vkyoungcn.learningtools;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
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
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.lang.ref.WeakReference;
import java.util.List;

/*
* 进入本Activity后，在启动的新线程中处理从DB取数据的业务，
* 数据取好后，附加一条“ending伪数据”，作为结束页面；
* 在结束页面还有其他特殊逻辑：①取消页脚的页数显示；②到达最后一页（伪数据页）后计时停止，生成要存入DB的Log数据；
* ③增加完成学习的确认按钮，以及DB数据生成、存入逻辑；④允许向前滑动查看，但数据不再改变（以TextView说明这个情况）；
* （同时，取消初始设计中以Dfg收尾的计划）
* */
public class ItemLearningActivity extends AppCompatActivity implements LearningTimeUpDiaFragment.OnUserChoiceMadeListener {
    private static final String TAG = "ItemLearningActivity";
    private int groupId;//最后的结果页面需要获取分组信息
    private int learningType;//不同类型的复习，不仅加载的fg不同，最后生成学习log的格式也不同；直接使用颜色的id号判断。
    private int timePastInMinute = 0;//流逝分钟数
    private int timeInSecond = 0;
    private String tableNameSuffix;//用来从DB获取本组所属的ITEMS
    private String groupSubItemIdsStr;//用来从DB获取本组所属的ITEMS
    private List<SingleItem> items;//数据源（未初始化）
    private Boolean prepared = false;
    private Boolean learningFinished = false;//本组学习完成。完成后置true，计时线程要检测之，避免完成后重新计时（因为代码顺序靠后BUG)
    private Handler handler = new LearningActivityHandler(this);
    private Thread timingThread;//采用全局变量便于到时间后终结之。【如果已滑动到ending页则直接停止计时，避免最后timeUp消息的产生】
    private Boolean prolonged = false;//计时完成如果还没有完成复习，可以延长时间一次（暂定15分钟，暂定不影响log计时）
    private int timeCount = 60;//【调试期间临时设置为1分钟】默认执行60次for循环（for循环包含60次1秒间隔的执行，完整执行一次for需要60秒）
    private String newLogs="";//用于回传到调用act的字串，新log
    public static final int RESULT_FROM_ITEM_LEARNING = 2;

    private YouMemoryDbHelper memoryDbHelper;

    private FrameLayout fltMask;
    private TextView tv_mask;
    private ViewPager viewPager;
    private TextView timePastMin;
    private TextView timePastScd;
    private TextView totalMinutes;//应在xx分钟内完成，的数字部分。

    private TextView tv_currentPageNum;
    private TextView tv_totalPageNum;

    public static final int MESSAGE_DB_DATE_FETCHED =1;
    public static final int MESSAGE_ONE_MINUTE_CHANGE =2;
    public static final int MESSAGE_ONE_SECOND_CHANGE =3;
    public static final int MESSAGE_TIME_UP = 4;
    public static final int MESSAGE_LOGS_SAVED = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: b");
        setContentView(R.layout.activity_item_learning);

        //从Intent获取参数
        groupId = getIntent().getIntExtra("group_id",0);
//        Log.i(TAG, "onCreate: groupId = "+groupId);
        tableNameSuffix = getIntent().getStringExtra("item_table_suffix");
        groupSubItemIdsStr = getIntent().getStringExtra("group_sub_item_ids_str");
        learningType = getIntent().getIntExtra("learning_type",0);
//        Log.i(TAG, "onCreate: learningType = "+learningType);

        fltMask = (FrameLayout) findViewById(R.id.flt_mask_learningPage);
        tv_mask = (TextView)findViewById(R.id.tv_onItsWay_learningPage);
        timePastMin = (TextView)findViewById(R.id.tv_time_past_numMinute_Learning);
        timePastScd = (TextView)findViewById(R.id.tv_time_past_numSecond_Learning);
        tv_currentPageNum = (TextView)findViewById(R.id.currentPageNum_learningActivity);
        tv_totalPageNum = (TextView)findViewById(R.id.totalPageNum_learningActivity);//总数字需要在数据加载完成后设置，在handleMessage中处理
        totalMinutes = (TextView) findViewById(R.id.tv_num_itemLearningActivity);

        viewPager = (ViewPager) findViewById(R.id.viewPager_ItemLearning);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //当页面滑动时为下方的textView设置当前页数，但是只在开始滑动后才有效果，初始进入时需要手动XML设为1
                tv_currentPageNum.setText(String.valueOf(position+1));//索引从0起需要加1

                //滑动到最后一页时
                if(position==items.size()-1){
                    //最后一张【Ending伪数据页】
                    learningFinished = true;

//                    timingThread.interrupt();//先结束计时线程。
                    // （不需要用户确认完成）向DB写数据；
                    //能到这一页的，属于正常完成
                    //显示信息：学习记录保存中，请稍等……同时执行向DB写log
                    tv_mask.setText("学习记录保存中，请稍等");
                    fltMask.setVisibility(View.VISIBLE);

                    tv_currentPageNum.setText("--");//总不能显示比总数还+1.
                    //在新线程处理log的DB保存操作。
                    new Thread(new learningFinishedRunnable()).start();


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

            //在数据原的最后附加一条“伪数据”，用于学习完成后显示学习信息，处理完成业务。
            SingleItem endingItem = new SingleItem(0,"完成","","",true);
            items.add(endingItem);

            Message message = new Message();
            message.what = MESSAGE_DB_DATE_FETCHED;

            handler.sendMessage(message);
        }
    }

    public class learningFinishedRunnable implements Runnable{
        private static final String TAG = "learningFinishedRunnabl";

        @Override
        public void run() {
//            Log.i(TAG, "run: b");
            //先更新记录
            newLogs = memoryDbHelper.updateLogOfGroup(groupId,System.currentTimeMillis(),learningType);
            //完成后通知UI
            Message message = new Message();
            message.what = MESSAGE_LOGS_SAVED ;
            message.obj = newLogs;

            handler.sendMessage(message);
        }
    }

    //用于计时并发送更新UI上时间值的消息
    public class TimingRunnable implements Runnable{
        private static final String TAG = "TimingRunnable";

        @Override
        public void run() {
//            Log.i(TAG, "run: ");
            while(!learningFinished && timeCount > 0){
                try {
                    for (int i = 0; i < 60&&!learningFinished; i++) {//这样才能在学习完成而分钟数未到的情况下终止计时。
                        Thread.sleep(1000);     // sleep 1 秒；本循环执行完60次需60秒

                        //消息发回UI，改变秒数1
                        Message message = new Message();
                        message.what = MESSAGE_ONE_SECOND_CHANGE;
                        handler.sendMessage(message);

                    }

                    timeCount--;
                    if(!learningFinished) {
                        //消息发回UI，改变分钟数1（只在未完成状态下才改变数字）
                        Message message = new Message();
                        message.what = MESSAGE_ONE_MINUTE_CHANGE;
                        handler.sendMessage(message);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(timeCount==0) {
                //是由于时间耗尽而结束循环时，消息发回UI。
                Message message = new Message();
                message.what = MESSAGE_TIME_UP;
                handler.sendMessage(message);
            }

        }
    }

    void handleMessage(Message msg){
        switch (msg.what){
            case MESSAGE_DB_DATE_FETCHED:
//      Log.i(TAG, "handleMessage: case 1 b");
                if(learningFinished) return;//【发现似乎存在完成后重新计时BUG,尝试此修改。但是无效】

                fltMask.setVisibility(View.GONE);
                //下方构造参数待修改
                LearningViewPrAdapter lvAdp = new LearningViewPrAdapter(getSupportFragmentManager(),items,LearningViewPrAdapter.TYPE_INIT_LEARNING);
//                Log.i(TAG, "handleMessage: ready to set adp");
                viewPager.setAdapter(lvAdp);

                tv_totalPageNum.setText(String.valueOf(items.size()-1));//最后一页ending伪数据不能算页数。
                prepared = true;
                //然后由启动计时器（每分钟更改一次数字）
                timingThread =  new Thread(new TimingRunnable());
                timingThread.start();
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
                timingThread.interrupt();//先结束计时线程。
                //安排时间到后的逻辑：②Dfg弹窗询问：
                //a.是否续时；b.强制结束，执行收尾逻辑。【完成数量大于18个可以拆分为两组】或者强制结束不做任何记录。
                if(!prolonged){
                    //尚未延长过时间，此时可以延长一次。
                    // 弹出dfg，其中要置prolonged为true。如果用户要延长时间，则将计时计数器回调到15分，继续执行线程。
                    popUpTimeEndingDiaFragment();
                }

                //如果已延长过1次，或者是刚才弹框后用户选择不延时直接dismiss弹框，则进行下列判断
                //根据完成的项目数量，小于12只有提示未能完成-确认。大于12可以拆分生成新分组，提示用户
                // （没必要询问是否拆分，如果不拆分就只能全部标记未完成，正常人不能这么干）
                // 此时如果遇到丢失焦点应默认将学习状态（时间、数量）保存，onStop直接存入DB（没有询问环节）。
//                if()
                break;

            case MESSAGE_LOGS_SAVED:
                timingThread = null;//停止计时
                // 滑动监听的设置代码早于timingThread实例化代码的位置，所以原先的终止方式无效。
                fltMask.setVisibility(View.GONE);//取消遮盖
                //在原始activity上给出结束按钮。显示学习信息。

                //可以为返回调用方activity而设置数据了
                Intent intent = new Intent();
                intent.putExtra("newLogsStr",newLogs);
                setResult(RESULT_FROM_ITEM_LEARNING,intent);

                break;


        }
    }

    //学习结束【注意不是时间流尽，时间流尽在下方方法处理】
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
        /*【下边dfg还没有写，是最后的大功能了】
        DialogFragment dfg = LearningEndingUpConfirmDiaFragment.newInstance("0","0");*/

    }

    private void popUpTimeEndingDiaFragment(){
        //先将延时标记置为true，代表已延时一次。
        prolonged = true;
        FragmentTransaction transaction = (getFragmentManager().beginTransaction());
        Fragment prev = (getFragmentManager().findFragmentByTag("Time_up"));

        if (prev != null) {
            Log.i(TAG, "inside Dialog(), inside if prev!=null branch");
            transaction.remove(prev);
        }
        DialogFragment dfg = LearningTimeUpDiaFragment.newInstance();
        dfg.show(transaction,"Time_up");
    }


    @Override
    public void onUserMadeChoice() {
        timeCount = 15;//【调试期间暂时设1】计时时间补充15分钟
        totalMinutes.setText("75");
        timingThread.start();//再次启动计时

    }



}
