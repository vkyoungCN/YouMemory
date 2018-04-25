package com.vkyoungcn.learningtools;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vkyoungcn.learningtools.adapter.AllMissionRvAdapter;
import com.vkyoungcn.learningtools.models.Mission;
import com.vkyoungcn.learningtools.sqlite.YouMemoryDbHelper;

import java.util.ArrayList;

/*
* 首页。
* 上部预留横向图片式广告位（间隔滚动式）
* 下方是任务列表；点击可进入新Activity查看任务情况。
* */
public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //从数据库获取数据源：所有Mission的标题（name字段）
//        YouMemoryDbHelper memoryDbHelper = YouMemoryDbHelper.getInstance(getApplicationContext());

        RecyclerView allMissionRecyclerView = (RecyclerView) findViewById(R.id.all_missions_rv);

        ArrayList<Mission> allMissions = getIntent().getParcelableArrayListExtra("All_Missions");
        if(allMissions == null){
            Toast.makeText(this, "没有任务信息", Toast.LENGTH_SHORT).show();
            allMissions = new ArrayList<>();
        }

        allMissionRecyclerView.setHasFixedSize(true);//暂时只有固定数量的任务，可以设fix。
        allMissionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allMissionRecyclerView.setAdapter(new AllMissionRvAdapter(allMissions,this));

    }

    /*
    * 似乎只有这种方式才能禁止返回到起始页。
    * 可以直接退出程序。
    */
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }

}
