package com.vkyoungcn.learningtools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class LogoMainActivity extends AppCompatActivity {
    private static final String TAG = "LogoMainActivity";
    private Handler handler = new LogoMainActivity.FirstActivityHandler(this);//涉及弱引用，通过其发送消息。
    public static final int MESSAGE_DB_POP_AND_FETCHED = 5001;
    public static final int MESSAGE_SLEEP_DONE = 5002;
    public static final int MESSAGE_MISSION_FETCHED = 5003;
    private boolean missionFetched = false;
    private ArrayList<Mission> missions;

    private TextView logoCn;
    private TextView reTry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_main);
        logoCn = (TextView)findViewById(R.id.logo_cn);
        reTry = (TextView)findViewById(R.id.re_try_firstActivity);

        SharedPreferences sharedPreferences=getSharedPreferences("youMemorySP", MODE_PRIVATE);
        boolean isFirstLaunch=sharedPreferences.getBoolean("IS_FIRST_LAUNCH", true);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        if(isFirstLaunch){
            //第一次
            //开启新线程执行DB填充操作然，同时提示。
            //完成后跳转下一页
            Toast.makeText(this, "首次运行，正在填充数据，请稍等……", Toast.LENGTH_LONG).show();
            new Thread(new PopTheDatabaseRunnable()).start();

            editor.putBoolean("IS_FIRST_LAUNCH", false);
            editor.apply();
        }else{
            //第二次稍等0.5秒后，动画效果，再直接跳转跳转
            new Thread(new SleepHalfSecondRunnable()).start();

        }
    }

    public class PopTheDatabaseRunnable implements Runnable{
        @Override
        public void run() {
            YouMemoryDbHelper memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());//应该是在获取DB-Helper后直接触发数据填充吧。
            ArrayList<Mission> missions = (ArrayList<Mission>) memoryDbHelper.getAllMissions();

            Message message = new Message();
            message.what = MESSAGE_DB_POP_AND_FETCHED;
            message.obj = missions;

            handler.sendMessage(message);
        }
    }

    public class FetchMissionsFromDBRunnable implements Runnable{
        @Override
        public void run() {
            YouMemoryDbHelper memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());//应该是在获取DB-Helper后直接触发数据填充吧。
            ArrayList<Mission> missions = (ArrayList<Mission>) memoryDbHelper.getAllMissions();

            Message message = new Message();
            message.what = MESSAGE_MISSION_FETCHED;
            message.obj = missions;

            handler.sendMessage(message);
        }
    }

    public class SleepHalfSecondRunnable implements Runnable{
        @Override
        public void run() {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Message message = new Message();
            message.what = MESSAGE_SLEEP_DONE;

            handler.sendMessage(message);
        }
    }


    final static class FirstActivityHandler extends Handler {
        private final WeakReference<LogoMainActivity> activityWeakReference;

        private FirstActivityHandler(LogoMainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LogoMainActivity logoMainActivity = activityWeakReference.get();
            if(logoMainActivity!=null){
                logoMainActivity.handleMessage(msg);
            }
        }
    }

    void handleMessage(Message message){
        switch (message.what){
            case MESSAGE_DB_POP_AND_FETCHED:
                missions = (ArrayList<Mission>) message.obj;

                Intent intent= new Intent(this,MainActivity.class);
                intent.putParcelableArrayListExtra("All_Missions",missions);
                startActivity(intent);
                break;
            case MESSAGE_SLEEP_DONE:
                ValueAnimator LogoDisAnimator = ValueAnimator.ofFloat(30,100,100,0);
                LogoDisAnimator.setDuration(900);
                LogoDisAnimator.addUpdateListener(new LogoDisAnimatorListener());
                LogoDisAnimator.setInterpolator(new LinearInterpolator());
                LogoDisAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        new Thread(new FetchMissionsFromDBRunnable()).start();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        if(missionFetched){//前提是任务信息的拉取要在300ms内完成
                            //否则不会执行（当然也就没有mission空指针问题）

                            Intent intent= new Intent(LogoMainActivity.this,MainActivity.class);
                            intent.putParcelableArrayListExtra("All_Missions",missions);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else {
                            Toast.makeText(LogoMainActivity.this, "任务拉取失败", Toast.LENGTH_SHORT).show();
                            logoCn.setAlpha((float) 0.5);
                            reTry.setVisibility(View.VISIBLE);

                        }

                    }
                });
                LogoDisAnimator.start();
            case MESSAGE_MISSION_FETCHED:
                missionFetched = true;//标志变量
                missions = (ArrayList<Mission>) message.obj;


        }
    }

    private class LogoDisAnimatorListener implements ValueAnimator.AnimatorUpdateListener {

        public void onAnimationUpdate(ValueAnimator valueanimator) {
            float value = (Float)valueanimator.getAnimatedValue();
            logoCn.setAlpha(value/100);
        }
    }

}
